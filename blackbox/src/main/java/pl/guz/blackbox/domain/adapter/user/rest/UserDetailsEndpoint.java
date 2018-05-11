package pl.guz.blackbox.domain.adapter.user.rest;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import pl.guz.blackbox.domain.model.user.User;
import pl.guz.blackbox.domain.model.user.UserRepository;

import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Slf4j
class UserDetailsEndpoint {

    private final UserRepository sometimesFailureUserRepository;
    private final UserRepository simpleUserRepository;

    @GetMapping("/async/users/{uuid}")
    DeferredResult<User> asyncGetUserRepositories(@PathVariable("uuid") String uuid) {
        log.info("#GET_USER_DETAILS: {}", uuid);
        DeferredResult<User> result = new DeferredResult<>();
        sometimesFailureUserRepository.load(uuid)
                .subscribe(success -> {
                    log.info("#GET_USER_DETAILS_SUCCESS");
                    result.setResult(success);
                }, failure -> {
                    result.setErrorResult(failure);
                    log.info("#GET_USER_DETAILS_FAILURE");
                });
        return result;
    }

    @GetMapping("/users/{uuid}")
    @SneakyThrows
    User getUserRepositories(@PathVariable("uuid") String uuid) {
        TimeUnit.MILLISECONDS.sleep(100);
        return simpleUserRepository.load(uuid)
                .blockingGet();
    }
}
