package pl.allegro.demo.domain.model.gitrepo;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;

@Value
@Accessors(fluent = true)
class GetUserRepositoriesCommand {
    private final String owner;
    private final String repositoryName;

    static GetUserRepositoriesCommand of(String owner, String repositoryName) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(repositoryName);
        return new GetUserRepositoriesCommand(owner, repositoryName);
    }
}
