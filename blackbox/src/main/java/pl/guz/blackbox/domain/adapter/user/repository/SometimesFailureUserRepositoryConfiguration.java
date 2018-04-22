package pl.guz.blackbox.domain.adapter.user.repository;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import io.reactivex.schedulers.Schedulers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import pl.guz.blackbox.domain.model.shared.Availability;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Configuration
class SometimesFailureUserRepositoryConfiguration {

    @Autowired
    MetricRegistry registry;

    @Value("${threshold-exception}")
    Integer thresholdException;

    @Bean
    SometimesFailureUserRepository userRepository() {

        return new SometimesFailureUserRepository(Availability.builder()
                                                              .scheduler(Schedulers.from(userReposThreadPool()))
                                                              .build(),
                                                  thresholdException,
                                                  randNumberBetween1_100()
        );
    }

    private Supplier<Integer> randNumberBetween1_100() {
        final Random random = new Random();
        final int max = 100;
        return () -> random.nextInt(max - 1) + 1;
    }

    private Executor userReposThreadPool() {
        String threadPoolName = "user-repo-thread-pool";
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(200);
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(2, 10,
                                                                    300L, TimeUnit.MILLISECONDS,
                                                                    workQueue,
                                                                    new CustomizableThreadFactory(threadPoolName + "-"));
        registry.register(threadPoolName + ".queue-utilization",
                          (Gauge<Double>) () -> workQueue.size() / (double) (workQueue.size() + workQueue.remainingCapacity()));
        registry.register(threadPoolName + ".queue-size", (Gauge<Integer>) workQueue::size);
        registry.register(threadPoolName + ".activeThreads", (Gauge) executorService::getActiveCount);
        return executorService;
    }
}
