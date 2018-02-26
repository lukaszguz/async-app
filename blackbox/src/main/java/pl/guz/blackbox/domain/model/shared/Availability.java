package pl.guz.blackbox.domain.model.shared;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.reactivex.Scheduler;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Builder
@Accessors(fluent = true)
public class Availability {
    private final CircuitBreaker circuitBreaker;
    private final Retry retryPolicy;
    private final Scheduler scheduler;
}
