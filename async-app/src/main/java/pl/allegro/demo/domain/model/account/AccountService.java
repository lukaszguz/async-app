package pl.allegro.demo.domain.model.account;

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
import pl.allegro.demo.domain.model.BlackboxConfig;
import pl.allegro.demo.domain.model.exception.ApplicationException;
import pl.allegro.demo.domain.model.shared.ApiResponseStatus;
import pl.allegro.demo.domain.model.shared.Availability;
import pl.allegro.demo.domain.model.shared.ListenableFutureAdapter;

import java.time.LocalDateTime;

import static io.vavr.API.*;

@RequiredArgsConstructor
@Slf4j
public class AccountService {
    private final BlackboxConfig blackboxConfig;
    private final AsyncRestTemplate traceAsyncRestTemplate;
    private final Availability availability;

    public Single<Account> findAccount(GetAccountCommand getAccountCommand) {
        return Single.fromCallable(() -> getAccountCommand)
                     .doOnSuccess(command -> log.info("Searching account: {}", command))
                     .flatMap(command -> ListenableFutureAdapter
                             .toSingle(traceAsyncRestTemplate.getForEntity(blackboxConfig.getGetUserUrl(),
                                                                           User.class,
                                                                           command.accountId())))
                     .compose(RetryTransformer.of(availability.retryPolicy()))
                     .observeOn(availability.scheduler())
                     .map(HttpEntity::getBody)
                     .doOnSuccess(body -> log.info("Got response for: {}", getAccountCommand))
                     .doOnError(e -> log.error("Cannot fetch account", e))
                     .map(user -> Account.of(getAccountCommand.accountId(),
                                             LocalDateTime.now()
                                                          .plusDays(2),
                                             user))
                     .lift(CircuitBreakerOperator.of(availability.circuitBreaker()))
                     .onErrorResumeNext(this::wrapException)
                     .subscribeOn(availability.scheduler());
    }

    private Single<Account> wrapException(Throwable throwable) {
        return Match(throwable).of(
                Case($(Predicates.instanceOf(HttpStatusCodeException.class)
                                 .and(e -> HttpStatus.NOT_FOUND == e.getStatusCode())), () -> Single.error(new ApplicationException(ApiResponseStatus.NOT_FOUND))),
                Case($(Predicates.instanceOf(HttpStatusCodeException.class)), () -> Single.error(new ApplicationException(ApiResponseStatus.BAD_GATEWAY))),
                Case($(), () -> Single.error(new ApplicationException(ApiResponseStatus.INTERNAL_SERVER_ERROR))));
    }
}
