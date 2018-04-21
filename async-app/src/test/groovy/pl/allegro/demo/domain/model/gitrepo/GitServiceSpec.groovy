//package pl.allegro.demo.domain.model.gitrepo
//
//import io.github.resilience4j.circuitbreaker.CircuitBreaker
//import io.github.resilience4j.retry.Retry
//import io.reactivex.schedulers.Schedulers
//import org.springframework.http.HttpStatus
//import org.springframework.http.ResponseEntity
//import org.springframework.scheduling.annotation.AsyncResult
//import org.springframework.web.client.AsyncRestTemplate
//import org.springframework.web.client.HttpClientErrorException
//import pl.allegro.demo.domain.model.Endpoints
//import pl.allegro.demo.domain.model.shared.ApiResponseStatus
//import pl.allegro.demo.domain.model.shared.Availability
//import spock.lang.Specification
//
//import java.time.LocalDateTime
//
//class GitServiceSpec extends Specification {
//
//    private Endpoints endpoints = new Endpoints(searchRepoByOwner: 'https://api.github.com/repos/{owner}/{repo}')
//    private AsyncRestTemplate asyncRestTemplate = Mock(AsyncRestTemplate)
//    private CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults('test')
//    private Retry retry = Retry.ofDefaults('test')
//    private GitService gitService = new GitService(
//            endpoints,
//            asyncRestTemplate,
//            Availability.builder()
//                    .scheduler(Schedulers.single())
//                    .circuitBreaker(circuitBreaker)
//                    .retryPolicy(retry)
//                    .build())
//
//    def "should return repository details"() {
//        given:
//        GetUserRepositoriesCommand command = GetUserRepositoriesCommand.of('owner', 'repositoryName')
//
//        when:
//        gitService.findRepoDetails(command)
//                .test()
//                .await()
//                .assertValue(new GitRepositoryDetails('Full name', 'desc', 'http://clone', 3, LocalDateTime.of(2018, 1, 1, 12, 0)))
//                .assertNoErrors()
//
//        then:
//        asyncRestTemplate.getForEntity("https://api.github.com/repos/{owner}/{repo}", GithubRepositoryResponse.class, 'owner', 'repositoryName') >> {
//            return new AsyncResult<ResponseEntity<GithubRepositoryResponse>>(ResponseEntity.ok(fixtureGithubRepositoryResponse()))
//        }
//    }
//
//    def "should wrap exceptions"() {
//        given:
//        GetUserRepositoriesCommand command = GetUserRepositoriesCommand.of('owner', 'repositoryName')
//
//        when:
//        gitService.findRepoDetails(command)
//                .test()
//                .await()
//                .assertError({ e -> e.status == expectedExceptionStatus })
//
//        then:
//        asyncRestTemplate.getForEntity("https://api.github.com/repos/{owner}/{repo}", GithubRepositoryResponse.class, 'owner', 'repositoryName') >> {
//            throw exception
//        }
//
//        where:
//        exception << [new HttpClientErrorException(HttpStatus.NOT_FOUND), new HttpClientErrorException(HttpStatus.CONFLICT), new RuntimeException()]
//        expectedExceptionStatus << [ApiResponseStatus.NOT_FOUND, ApiResponseStatus.BAD_GATEWAY, ApiResponseStatus.INTERNAL_SERVER_ERROR]
//    }
//
//    private GithubRepositoryResponse fixtureGithubRepositoryResponse() {
//        return new GithubRepositoryResponse(1, 'Full name', 'desc', 'http://clone', 3, LocalDateTime.of(2018, 1, 1, 12, 0))
//    }
//}
