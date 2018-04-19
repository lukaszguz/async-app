package pl.allegro.demo.domain.model.account;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;
import java.util.UUID;

@Value
@Accessors(fluent = true)
public class GetAccountCommand {
    private final UUID accountId;

    public static GetAccountCommand of(String accountId) {
        Objects.requireNonNull(accountId);
        return new GetAccountCommand(UUID.fromString(accountId));
    }
}
