package pl.guz.blackbox.domain.model.gitrepo;

import io.reactivex.Single;
import io.vavr.Predicates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import pl.guz.blackbox.domain.model.User;
import pl.guz.blackbox.domain.model.exception.ApplicationException;
import pl.guz.blackbox.domain.model.shared.ApiResponseStatus;
import pl.guz.blackbox.domain.model.shared.Availability;

import java.util.Random;
import java.util.UUID;

import static io.vavr.API.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRepository {
    private final Availability availability;
    private final Random randomName = new Random(100);

    public Single<User> findRepoDetails(String uuid) {
//        return Single.fromCallable(() -> getUserRepositoriesCommand)
//                     .doOnSuccess(command -> log.info("Searching repo's details: {}", command))
//                     .flatMap(command -> ListenableFutureAdapter
//                             .toSingle(traceAsyncRestTemplate.getForEntity(endpoints.getSearchRepoByOwner(),
//                                                                           GithubRepositoryResponse.class,
//                                                                           command.owner(),
//                                                                           command.repositoryName())))
//                     .compose(RetryTransformer.of(availability.retryPolicy()))
//                     .observeOn(availability.scheduler())
//                     .map(HttpEntity::getBody)
//                     .doOnSuccess(body -> log.info("Got response for: {}", getUserRepositoriesCommand))
//                     .doOnError(e -> log.error("Cannot fetch repository details", e))
//                     .map(GitRepository::of)
//                     .map(GitRepository::details)
//                     .onErrorResumeNext(this::wrapException)
//                     .lift(CircuitBreakerOperator.of(availability.circuitBreaker()))
//                     .subscribeOn(availability.scheduler());
        return Single.just(new User(
                UUID.fromString(uuid),
                "Adam" + randomName.nextInt(),
                "Nowak" + randomName.nextInt()));
    }

    private Single<User> wrapException(Throwable throwable) {
        return Match(throwable).of(
                Case($(Predicates.instanceOf(HttpStatusCodeException.class)
                                 .and(e -> HttpStatus.NOT_FOUND == e.getStatusCode())), () -> Single.error(new ApplicationException(ApiResponseStatus.NOT_FOUND))),
                Case($(Predicates.instanceOf(HttpStatusCodeException.class)), () -> Single.error(new ApplicationException(ApiResponseStatus.BAD_GATEWAY))),
                Case($(), () -> Single.error(new ApplicationException(ApiResponseStatus.INTERNAL_SERVER_ERROR))));
    }
}