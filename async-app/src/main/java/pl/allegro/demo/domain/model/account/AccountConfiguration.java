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
import pl.allegro.demo.domain.model.shared.HttpPredicates;
import pl.allegro.demo.domain.model.shared.MonitoringThreadPool;

import java.time.Duration;
import java.util.concurrent.Executor;

import static io.vavr.API.*;

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
                                              .scheduler(Schedulers.from(accountServiceThreadPool()))
                                              .circuitBreaker(circuitBreaker())
                                              .retryPolicy(retryPolicy())
                                              .build()
        );
    }

    private Executor accountServiceThreadPool() {
        return MonitoringThreadPool.threadPool("account-service-thread-pool",
                                               200,
                                               2,
                                               2,
                                               100);
    }

    private Retry retryPolicy() {
        return Retry.of("account-service", RetryConfig.custom()
                                                      .waitDuration(Duration.ofMillis(100))
                                                      .maxAttempts(5)
                                                      .retryOnException(Predicates.noneOf(HttpPredicates.is4xxStatus()))
                                                      .build());
    }

    private CircuitBreaker circuitBreaker() {
        return CircuitBreakerFactory.circuitBreaker("account-service",
                                                    () -> CircuitBreakerConfig.custom()
                                                                              .waitDurationInOpenState(Duration.ofSeconds(1))
                                                                              .ringBufferSizeInHalfOpenState(5)
                                                                              .ringBufferSizeInClosedState(100)
                                                                              .failureRateThreshold(30)
                                                                              .recordFailure(throwable -> Match(throwable).of(
                                                                                      Case($(Predicates.instanceOf(
                                                                                              ApplicationException.class)), false),
                                                                                      Case($(HttpPredicates.is4xxStatus()), false),
                                                                                      Case($(), true)))
                                                                              .build()
                                                   );
    }
}
