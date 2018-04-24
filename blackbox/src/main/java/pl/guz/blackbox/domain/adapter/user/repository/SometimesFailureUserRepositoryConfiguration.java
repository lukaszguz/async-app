package pl.guz.blackbox.domain.adapter.user.repository;

import io.reactivex.schedulers.Schedulers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.guz.blackbox.domain.model.shared.Availability;
import pl.guz.blackbox.domain.model.shared.MonitoringThreadPool;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Configuration
class SometimesFailureUserRepositoryConfiguration {

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
        return MonitoringThreadPool.threadPool(
                "user-repo-thread-pool",
                200,
                2,
                10,
                300);
    }
}
