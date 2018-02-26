package pl.allegro.demo.domain.model.gitrepo;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@AllArgsConstructor
@EqualsAndHashCode
class GitRepository {

    private final Long id;
    private String fullName;
    private String description;
    private String cloneUrl;
    private Integer stars;
    private LocalDateTime createdAt;

    static GitRepository of(GithubRepositoryResponse githubRepositoryResponse) {
        return new GitRepository(githubRepositoryResponse.id(),
                                 githubRepositoryResponse.fullName(),
                                 githubRepositoryResponse.description(),
                                 githubRepositoryResponse.cloneUrl(),
                                 githubRepositoryResponse.stars(),
                                 githubRepositoryResponse.createdAt()
        );
    }

    GitRepositoryDetails details() {
        return new GitRepositoryDetails(fullName, description, cloneUrl, stars, createdAt);
    }
}
