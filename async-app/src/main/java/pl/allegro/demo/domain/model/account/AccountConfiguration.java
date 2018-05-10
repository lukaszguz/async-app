package pl.allegro.demo.domain.model.account;

import com.codahale.metrics.MetricRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.reactivex.schedulers.Schedulers;
import io.vavr.Predicates;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.AsyncRestTemplate;
import pl.allegro.demo.domain.model.BlackboxConfig;
import pl.allegro.demo.domain.model.exception.ApplicationException;
import pl.allegro.demo.domain.model.shared.Availability;
import pl.allegro.demo.domain.model.shared.CircuitBreakerFactory;
import pl.allegro.demo.domain.model.shared.MonitoringThreadPool;

import java.time.Duration;
import java.util.concurrent.Executor;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
import static pl.allegro.demo.domain.model.shared.HttpPredicates.is4xxStatus;

@Configuration
@RequiredArgsConstructor
class AccountConfiguration {
    final AsyncRestTemplate traceAsyncRestTemplate;
    final MetricRegistry registry;
    final BlackboxConfig blackboxConfig;

    @Bean
    AccountService accountService() {
        return new AccountService(blackboxConfig,
                traceAsyncRestTemplate,
                Availability.builder()
                        .retryPolicy(retryPolicy())
                        .circuitBreaker(circuitBreaker())
                        .scheduler(Schedulers.from(accountServiceThreadPool()))
                        .build()
        );
    }

    private Retry retryPolicy() {
        return Retry.of("account-service", RetryConfig.custom()
                .waitDuration(Duration.ofMillis(200))
                .maxAttempts(5)
                .retryOnException(Predicates.noneOf(is4xxStatus()))
                .build());
    }

    private CircuitBreaker circuitBreaker() {

        return CircuitBreakerFactory.circuitBreaker("account-service",
                () -> CircuitBreakerConfig.custom()
                        .waitDurationInOpenState(Duration.ofSeconds(2))
                        .ringBufferSizeInHalfOpenState(5)
                        .ringBufferSizeInClosedState(100)
                        .failureRateThreshold(50)
                        .recordFailure(throwable -> Match(throwable).of(
                                Case($(instanceOf(ApplicationException.class)), false),
                                Case($(is4xxStatus()), false),
                                Case($(), true)))
                        .build()
        );
    }

    private Executor accountServiceThreadPool() {
        return MonitoringThreadPool.threadPool("account-service-thread-pool",
                200,
                2,
                4,
                500);
    }

}
