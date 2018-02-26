package pl.allegro.demo.domain.model.shared;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.metrics.CircuitBreakerMetrics;
import io.vavr.Predicates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.allegro.demo.domain.model.exception.ApplicationException;

import java.time.Duration;
import java.util.function.Supplier;

import static com.codahale.metrics.MetricRegistry.name;
import static io.vavr.API.*;

@Component
@RequiredArgsConstructor
public class CircuitBreakerFactory {

    private static final String PREFIX = "circuit_breaker-";

    private final MetricRegistry metricRegistry;

    public CircuitBreaker defaultCircuitBreakerOf(String circuitBreakerName) {
        return circuitBreaker(circuitBreakerName, () -> CircuitBreakerConfig.custom()
                                                                            .waitDurationInOpenState(Duration.ofSeconds(10))
                                                                            .ringBufferSizeInHalfOpenState(3)
                                                                            .ringBufferSizeInClosedState(5)
                                                                            .failureRateThreshold(50)
                                                                            .recordFailure(throwable -> Match(throwable).of(
                                                                                    Case($(Predicates.instanceOf(ApplicationException.class)), false),
                                                                                    Case($(Predicates.instanceOf(ApplicationException.class)), false),
                                                                                    Case($(HttpPredicates.is4xxStatus()), false),
                                                                                    Case($(), true)))
                                                                            .build()
                             );
    }

    public CircuitBreaker circuitBreaker(String circuitBreakerName, Supplier<CircuitBreakerConfig> circuitBreakerConfig) {
        CircuitBreaker circuitBreaker = CircuitBreaker.of(PREFIX + circuitBreakerName, circuitBreakerConfig);
        addMetrics(circuitBreaker);
        return circuitBreaker;
    }

    private void addMetrics(CircuitBreaker circuitBreaker) {
        String circuitBreakerName = circuitBreaker.getName();
        metricRegistry.register(name(circuitBreakerName, CircuitBreakerMetrics.STATE),
                                (Gauge<Integer>) () -> circuitBreaker.getState()
                                                                     .getOrder());
        metricRegistry.register(name(circuitBreakerName, CircuitBreakerMetrics.SUCCESSFUL),
                                (Gauge<Integer>) () -> circuitBreaker.getMetrics()
                                                                     .getNumberOfSuccessfulCalls());
        metricRegistry.register(name(circuitBreakerName, CircuitBreakerMetrics.FAILED),
                                (Gauge<Integer>) () -> circuitBreaker.getMetrics()
                                                                     .getNumberOfFailedCalls());
        metricRegistry.register(name(circuitBreakerName, CircuitBreakerMetrics.NOT_PERMITTED),
                                (Gauge<Long>) () -> circuitBreaker.getMetrics()
                                                                  .getNumberOfNotPermittedCalls());
        metricRegistry.register(name(circuitBreakerName, CircuitBreakerMetrics.BUFFERED),
                                (Gauge<Integer>) () -> circuitBreaker.getMetrics()
                                                                     .getNumberOfBufferedCalls());
        metricRegistry.register(name(circuitBreakerName, CircuitBreakerMetrics.BUFFERED_MAX),
                                (Gauge<Integer>) () -> circuitBreaker.getMetrics()
                                                                     .getMaxNumberOfBufferedCalls());
    }
}
