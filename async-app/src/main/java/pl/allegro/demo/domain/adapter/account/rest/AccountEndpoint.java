package pl.allegro.demo.domain.adapter.account.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import pl.allegro.demo.domain.model.account.Account;
import pl.allegro.demo.domain.model.account.AccountService;
import pl.allegro.demo.domain.model.account.GetAccountCommand;

@RestController
@RequiredArgsConstructor
@Slf4j
class AccountEndpoint {

    private final AccountService accountService;

    @GetMapping("/accounts/{accountId}")
    DeferredResult<Account> getAccount(@PathVariable("accountId") String accountId) {
        GetAccountCommand getUserRepositoriesCommand = GetAccountCommand.of(accountId);
        DeferredResult<Account> result = new DeferredResult<>();
        accountService.findAccount(getUserRepositoriesCommand)
                      .subscribe(success -> {
                          log.info("Got account: {}", success);
                          result.setResult(success);
                      }, failure -> {
                          result.setErrorResult(failure);
                          log.error("Cannot fetch account: {}", accountId);
                      });
        return result;
    }
}
