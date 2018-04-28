package pl.allegro.demo.domain.model.shared;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.metrics.CircuitBreakerMetrics;
import io.micrometer.core.instrument.Metrics;
import io.vavr.Predicates;
import pl.allegro.demo.domain.model.exception.ApplicationException;

import java.time.Duration;
import java.util.function.Supplier;

import static com.codahale.metrics.MetricRegistry.name;
import static io.vavr.API.*;

public class CircuitBreakerFactory {

    private static final String PREFIX = "circuit_breaker-";

    public static CircuitBreaker defaultCircuitBreakerOf(String circuitBreakerName) {
        return circuitBreaker(circuitBreakerName, () -> CircuitBreakerConfig.custom()
                                                                            .waitDurationInOpenState(Duration.ofSeconds(10))
                                                                            .ringBufferSizeInHalfOpenState(10)
                                                                            .ringBufferSizeInClosedState(100)
                                                                            .failureRateThreshold(50)
                                                                            .recordFailure(throwable -> Match(throwable).of(
                                                                                    Case($(Predicates.instanceOf(ApplicationException.class)), false),
                                                                                    Case($(HttpPredicates.is4xxStatus()), false),
                                                                                    Case($(), true)))
                                                                            .build()
                             );
    }

    public static CircuitBreaker circuitBreaker(String circuitBreakerName, Supplier<CircuitBreakerConfig> circuitBreakerConfig) {
        CircuitBreaker circuitBreaker = CircuitBreaker.of(PREFIX + circuitBreakerName, circuitBreakerConfig);
        addMetrics(circuitBreaker);
        return circuitBreaker;
    }

    private static void addMetrics(CircuitBreaker circuitBreaker) {
        String circuitBreakerName = circuitBreaker.getName();
        Metrics.gauge(name(circuitBreakerName, CircuitBreakerMetrics.STATE),
                      circuitBreaker,
                      cb -> cb.getState()
                              .getOrder());
        Metrics.gauge(name(circuitBreakerName, CircuitBreakerMetrics.SUCCESSFUL), circuitBreaker, cb -> cb.getMetrics()
                                                                                                          .getNumberOfSuccessfulCalls());
        Metrics.gauge(name(circuitBreakerName, CircuitBreakerMetrics.FAILED), circuitBreaker, cb -> cb.getMetrics()
                                                                                                      .getNumberOfFailedCalls());
        Metrics.gauge(name(circuitBreakerName, CircuitBreakerMetrics.NOT_PERMITTED), circuitBreaker, cb -> cb.getMetrics()
                                                                                                             .getNumberOfNotPermittedCalls());
        Metrics.gauge(name(circuitBreakerName, CircuitBreakerMetrics.BUFFERED), circuitBreaker, cb -> cb.getMetrics()
                                                                                                        .getNumberOfBufferedCalls());
        Metrics.gauge(name(circuitBreakerName, CircuitBreakerMetrics.BUFFERED_MAX), circuitBreaker, cb -> cb.getMetrics()
                                                                                                            .getMaxNumberOfBufferedCalls());
    }
}
