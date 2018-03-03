package pl.guz.blackbox.domain.adapter.user.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import pl.guz.blackbox.domain.model.user.User;
import pl.guz.blackbox.domain.model.user.UserRepository;

@RestController
@RequiredArgsConstructor
@Slf4j
class UserDetailsEndpoint {

    private final UserRepository userRepository;

    @GetMapping("/users/{uuid}")
    DeferredResult<User> getUserRepositories(@PathVariable("uuid") String uuid) {
        log.info("#GET_USER_DETAILS: {}", uuid);
        DeferredResult<User> result = new DeferredResult<>();
        userRepository.load(uuid)
                      .subscribe(success -> {
                          log.info("#GET_USER_DETAILS_SUCCESS");
                          result.setResult(success);
                      }, failure -> {
                          result.setErrorResult(failure);
                          log.info("#GET_USER_DETAILS_FAILURE");
                      });
        return result;
    }
}
