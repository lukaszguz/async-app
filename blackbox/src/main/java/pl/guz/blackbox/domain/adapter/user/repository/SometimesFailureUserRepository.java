package pl.guz.blackbox.domain.adapter.user.repository;

import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;
import pl.guz.blackbox.domain.model.exception.ApplicationException;
import pl.guz.blackbox.domain.model.shared.ApiResponseStatus;
import pl.guz.blackbox.domain.model.shared.Availability;
import pl.guz.blackbox.domain.model.user.User;
import pl.guz.blackbox.domain.model.user.UserRepository;

import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
class SometimesFailureUserRepository implements UserRepository {
    private final Availability availability;
    private final Integer thresholdException;
    private final Integer range;
    private final Supplier<Integer> integerSupplier;
    private final Random randomName = new Random();

    SometimesFailureUserRepository(Availability availability, Integer thresholdException, Supplier<Integer> integerSupplier) {
        this.availability = availability;
        this.thresholdException = thresholdException;
        this.integerSupplier = integerSupplier;
        this.range = countRange();
    }

    public Maybe<User> load(String uuid) {
        return Maybe.defer(() -> exceptionOrValue(uuid))
                .subscribeOn(availability.scheduler());
    }

    private Maybe<User> exceptionOrValue(String userId) {
        if (integerSupplier.get() <= range) {
            return Maybe.fromCallable(() -> user(userId));
        }
        return Maybe.error(new ApplicationException(ApiResponseStatus.INTERNAL_SERVER_ERROR));
    }

    private User user(String userId) {
        return new User(
                UUID.fromString(userId),
                "Adam" + randomName.nextInt(),
                "Nowak" + randomName.nextInt()
        );
    }

    private Integer countRange() {
        return (thresholdException - 100) * -1;
    }
}