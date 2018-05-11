package pl.guz.blackbox.domain.adapter.user.repository;

import io.reactivex.Maybe;
import lombok.extern.slf4j.Slf4j;
import pl.guz.blackbox.domain.model.user.User;
import pl.guz.blackbox.domain.model.user.UserRepository;

import java.util.Random;
import java.util.UUID;

@Slf4j
class SimpleUserRepository implements UserRepository {
    private final Random randomName = new Random();

    public Maybe<User> load(String uuid) {
        return Maybe.fromCallable(() -> user(uuid));
    }

    private User user(String userId) {
        return new User(
                UUID.fromString(userId),
                "Adam" + randomName.nextInt(),
                "Nowak" + randomName.nextInt()
        );
    }
}