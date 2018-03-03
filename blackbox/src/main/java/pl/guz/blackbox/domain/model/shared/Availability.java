package pl.guz.blackbox.domain.model.shared;

import io.reactivex.Scheduler;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Builder
@Accessors(fluent = true)
public class Availability {
    private final Scheduler scheduler;
}
