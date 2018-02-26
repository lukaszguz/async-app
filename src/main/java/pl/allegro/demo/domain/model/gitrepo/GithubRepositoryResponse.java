package pl.allegro.demo.domain.model.gitrepo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;
import lombok.experimental.Accessors;
import pl.allegro.demo.deserializer.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

@Value
@Accessors(fluent = true)
public class GithubRepositoryResponse {

    private final Long id;
    private final String fullName;
    private final String description;
    private final String cloneUrl;
    private final Integer stars;
    private final LocalDateTime createdAt;

    public GithubRepositoryResponse(@JsonProperty("id") Long id,
                                    @JsonProperty("full_name") String fullName,
                                    @JsonProperty("description") String description,
                                    @JsonProperty("clone_url") String cloneUrl,
                                    @JsonProperty("stargazers_count") Integer stars,
                                    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                    @JsonProperty("created_at") LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.description = description;
        this.cloneUrl = cloneUrl;
        this.stars = stars;
        this.createdAt = createdAt;
    }
}
