package pl.allegro.demo.domain.model.gitrepo

import spock.lang.Specification

import java.time.LocalDateTime

class GitRepositorySpec extends Specification {

    def "should create new object base on github response"() {
        given:
        GithubRepositoryResponse response = new GithubRepositoryResponse(1, 'Full name', 'desc', 'http://clone', 3, LocalDateTime.of(2018, 1, 1, 12, 0))

        expect:
        GitRepository.of(response) == new GitRepository(1, 'Full name', 'desc', 'http://clone', 3, LocalDateTime.of(2018, 1, 1, 12, 0))
    }

    def "should get details about git repository"() {
        given:
        GitRepository gitRepository = new GitRepository(1, 'Full name', 'desc', 'http://clone', 3, LocalDateTime.of(2018, 1, 1, 12, 0))

        expect:
        gitRepository.details() == new GitRepositoryDetails('Full name', 'desc', 'http://clone', 3, LocalDateTime.of(2018, 1, 1, 12, 0))
    }
}
