package pl.allegro.demo.domain.model.account;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.reactivex.schedulers.Schedulers;
import io.vavr.Predicates;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.web.client.AsyncRestTemplate;
import pl.allegro.demo.domain.model.BlackboxConfig;
import pl.allegro.demo.domain.model.exception.ApplicationException;
import pl.allegro.demo.domain.model.shared.Availability;
import pl.allegro.demo.domain.model.shared.CircuitBreakerFactory;
import pl.allegro.demo.domain.model.shared.HttpPredicates;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.vavr.API.*;

@Configuration
@RequiredArgsConstructor
class AccountConfiguration {
    final AsyncRestTemplate traceAsyncRestTemplate;
    final MetricRegistry registry;
    final CircuitBreakerFactory circuitBreakerFactory;
    final BlackboxConfig blackboxConfig;

    @Bean
    AccountService accountService() {
        return new AccountService(blackboxConfig,
                                  traceAsyncRestTemplate,
                                  Availability.builder()
                                              .scheduler(Schedulers.from(accountServiceThreadPool()))
                                              .circuitBreaker(
                                                      circuitBreakerFactory.circuitBreaker("account-service",
                                                                                           () -> CircuitBreakerConfig.custom()
                                                                                                                     .waitDurationInOpenState(Duration.ofSeconds(1))
                                                                                                                     .ringBufferSizeInHalfOpenState(5)
                                                                                                                     .ringBufferSizeInClosedState(100)
                                                                                                                     .failureRateThreshold(20)
                                                                                                                     .recordFailure(throwable -> Match(throwable).of(
                                                                                                                             Case($(Predicates.instanceOf(
                                                                                                                                     ApplicationException.class)), false),
                                                                                                                             Case($(HttpPredicates.is4xxStatus()), false),
                                                                                                                             Case($(), true)))
                                                                                                                     .build()
                                                                                          ))
                                              .retryPolicy(retryPolicy())
                                              .build()
        );
    }

    private Executor accountServiceThreadPool() {
        String threadPoolName = "account-service-thread-pool";
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(200);
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(2, 2,
                                                                    0L, TimeUnit.MILLISECONDS,
                                                                    workQueue,
                                                                    new CustomizableThreadFactory(threadPoolName + "-"));
        registry.register(threadPoolName + ".queue-utilization",
                          (Gauge<Double>) () -> workQueue.size() / (double) (workQueue.size() + workQueue.remainingCapacity()));
        registry.register(threadPoolName + ".queue-size", (Gauge<Integer>) workQueue::size);
        registry.register(threadPoolName + ".activeThreads", (Gauge) executorService::getActiveCount);
        return executorService;
    }

    private Retry retryPolicy() {
        return Retry.of("account-service", RetryConfig.custom()
                                                      .waitDuration(Duration.ofMillis(300))
                                                      .retryOnException(Predicates.noneOf(HttpPredicates.is4xxStatus()))
                                                      .build());
    }
}
