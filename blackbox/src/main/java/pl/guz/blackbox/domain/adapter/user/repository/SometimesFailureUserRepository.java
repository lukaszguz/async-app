package pl.guz.blackbox.domain.adapter.user.repository;

import io.reactivex.Maybe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.guz.blackbox.domain.model.exception.ApplicationException;
import pl.guz.blackbox.domain.model.shared.ApiResponseStatus;
import pl.guz.blackbox.domain.model.shared.Availability;
import pl.guz.blackbox.domain.model.user.User;
import pl.guz.blackbox.domain.model.user.UserRepository;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Slf4j
class SometimesFailureUserRepository implements UserRepository {
    private final Availability availability;
    private final Random randomName = new Random();
    private final AtomicInteger counter = new AtomicInteger(0);

    public Maybe<User> load(String uuid) {
        return Maybe.fromCallable(counter::incrementAndGet)
                    .flatMap(count -> choose(count, uuid))
                    .subscribeOn(availability.scheduler());
    }

    private Maybe<User> choose(Integer count, String userId) {
        if (count > 10) {
            counter.set(1);
            count = 1;
        }
        if (count == 9 || count == 10) {
            return Maybe.error(new ApplicationException(ApiResponseStatus.INTERNAL_SERVER_ERROR));
        }
        return Maybe.fromCallable(() -> user(userId));
    }

    private User user(String userId) {
        return new User(
                UUID.fromString(userId),
                "Adam" + randomName.nextInt(),
                "Nowak" + randomName.nextInt()
        );
    }
}