package pl.guz.blackbox.domain.model.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpPredicates {

    public static Predicate<Throwable> is4xxStatus() {
        return e -> {
            if (e instanceof HttpStatusCodeException) {
                HttpStatusCodeException statusCodeException = (HttpStatusCodeException) e;
                return statusCodeException.getStatusCode()
                                          .is4xxClientError();
            }
            return false;
        };
    }
}
