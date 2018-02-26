package pl.allegro.demo.domain.model.gitrepo;

import io.github.resilience4j.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.retry.transformer.RetryTransformer;
import io.reactivex.Single;
import io.vavr.Predicates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import pl.allegro.demo.domain.model.Endpoints;
import pl.allegro.demo.domain.model.exception.ApplicationException;
import pl.allegro.demo.domain.model.shared.ApiResponseStatus;
import pl.allegro.demo.domain.model.shared.Availability;
import pl.allegro.demo.domain.model.shared.ListenableFutureAdapter;

import static io.vavr.API.*;

@Component
@RequiredArgsConstructor
@Slf4j
class GitService {
    private final Endpoints endpoints;
    private final AsyncRestTemplate traceAsyncRestTemplate;
    private final Availability availability;

    Single<GitRepositoryDetails> findRepoDetails(GetUserRepositoriesCommand getUserRepositoriesCommand) {
        return Single.fromCallable(() -> getUserRepositoriesCommand)
                     .doOnSuccess(command -> log.info("Searching repo's details: {}", command))
                     .flatMap(command -> ListenableFutureAdapter
                             .toSingle(traceAsyncRestTemplate.getForEntity(endpoints.getSearchRepoByOwner(),
                                                                           GithubRepositoryResponse.class,
                                                                           command.owner(),
                                                                           command.repositoryName())))
                     .compose(RetryTransformer.of(availability.retryPolicy()))
                     .observeOn(availability.scheduler())
                     .map(HttpEntity::getBody)
                     .doOnSuccess(body -> log.info("Got response for: {}", getUserRepositoriesCommand))
                     .doOnError(e -> log.error("Cannot fetch repository details", e))
                     .map(GitRepository::of)
                     .map(GitRepository::details)
                     .onErrorResumeNext(this::wrapException)
                     .lift(CircuitBreakerOperator.of(availability.circuitBreaker()))
                     .subscribeOn(availability.scheduler());
    }

    private Single<GitRepositoryDetails> wrapException(Throwable throwable) {
        return Match(throwable).of(
                Case($(Predicates.instanceOf(HttpStatusCodeException.class)
                                 .and(e -> HttpStatus.NOT_FOUND == e.getStatusCode())), () -> Single.error(new ApplicationException(ApiResponseStatus.NOT_FOUND))),
                Case($(Predicates.instanceOf(HttpStatusCodeException.class)), () -> Single.error(new ApplicationException(ApiResponseStatus.BAD_GATEWAY))),
                Case($(), () -> Single.error(new ApplicationException(ApiResponseStatus.INTERNAL_SERVER_ERROR))));
    }
}