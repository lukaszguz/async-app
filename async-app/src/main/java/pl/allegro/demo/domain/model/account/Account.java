package pl.allegro.demo.domain.model.account;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;
import pl.allegro.demo.serializer.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.UUID;

@Value(staticConstructor = "of")
public class Account {
    private UUID id;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime validOn;
    private User user;
}
