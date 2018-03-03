package pl.guz.blackbox.domain.model.user;

import io.reactivex.Maybe;

public interface UserRepository {

    Maybe<User> load(String uuid);
}
