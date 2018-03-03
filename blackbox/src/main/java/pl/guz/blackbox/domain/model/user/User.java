package pl.guz.blackbox.domain.model.user;

import lombok.Value;

import java.util.UUID;

@Value
public class User {

    private UUID id;
    private String firstName;
    private String lastName;
}
