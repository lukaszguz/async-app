package pl.allegro.demo.domain.model.gitrepo;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.reactivex.schedulers.Schedulers;
import io.vavr.Predicates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.web.client.AsyncRestTemplate;
import pl.allegro.demo.domain.model.Endpoints;
import pl.allegro.demo.domain.model.shared.Availability;
import pl.allegro.demo.domain.model.shared.CircuitBreakerFactory;
import pl.allegro.demo.domain.model.shared.HttpPredicates;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
class GitRepositoriesConfiguration {

    @Autowired
    AsyncRestTemplate traceAsyncRestTemplate;
    @Autowired
    MetricRegistry registry;
    @Autowired
    CircuitBreakerFactory circuitBreakerFactory;
    @Autowired
    Endpoints endpoints;

    @Bean
    GitService gitService() {
        return new GitService(endpoints,
                              traceAsyncRestTemplate,
                              Availability.builder()
                                          .scheduler(Schedulers.from(userReposThreadPool()))
                                          .circuitBreaker(circuitBreakerFactory.defaultCircuitBreakerOf("user-repo"))
                                          .retryPolicy(retryPolicy())
                                          .build()
        );
    }

    private Executor userReposThreadPool() {
        String threadPoolName = "user-repo-thread-pool";
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(200);
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(10, 10,
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
        return Retry.of("user-repo", RetryConfig.custom()
                                                .waitDuration(Duration.ofMillis(300))
                                                .retryOnException(Predicates.noneOf(HttpPredicates.is4xxStatus()))
                                                .build());
    }
}
