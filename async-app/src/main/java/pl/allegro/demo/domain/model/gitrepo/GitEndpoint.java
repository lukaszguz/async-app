package pl.allegro.demo.domain.model.gitrepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequiredArgsConstructor
@Slf4j
class GitEndpoint {

    private final GitService gitService;

    @GetMapping("/repositories/{owner}/{repository-name}")
    DeferredResult<GitRepositoryDetails> getUserRepositories(@PathVariable("owner") String owner, @PathVariable("repository-name") String repositoryName) {
        log.info("#GET_REPOSITORY_DETAILS_REQUEST request owner: {}, repository name: {}", owner, repositoryName);
        GetUserRepositoriesCommand getUserRepositoriesCommand = GetUserRepositoriesCommand.of(owner, repositoryName);
        DeferredResult<GitRepositoryDetails> result = new DeferredResult<>();
        gitService.findRepoDetails(getUserRepositoriesCommand)
                  .subscribe(success -> {
                      log.info("#GET_REPOSITORY_DETAILS_SUCCESS");
                      result.setResult(success);
                  }, failure -> {
                      result.setErrorResult(failure);
                      log.info("#GET_REPOSITORY_DETAILS_FAILURE");
                  });
        return result;
    }
}
