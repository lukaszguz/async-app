package pl.allegro.demo.domain.model.gitrepo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;
import pl.allegro.demo.deserializer.LocalDateTimeDeserializer;
import pl.allegro.demo.serializer.LocalDateTimeSerializer;

import java.time.LocalDateTime;

@Value
class GitRepositoryDetails {

    private final String fullName;
    private final String description;
    private final String cloneUrl;
    private final Integer stars;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime createdAt;

    public GitRepositoryDetails(@JsonProperty("full_name") String fullName,
                                @JsonProperty("description") String description,
                                @JsonProperty("clone_url") String cloneUrl,
                                @JsonProperty("stargazers_count") Integer stars,
                                @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                @JsonProperty("created_at") LocalDateTime createdAt) {
        this.fullName = fullName;
        this.description = description;
        this.cloneUrl = cloneUrl;
        this.stars = stars;
        this.createdAt = createdAt;
    }
}
