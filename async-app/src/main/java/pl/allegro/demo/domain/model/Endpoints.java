package pl.allegro.demo.domain.model;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration
@RefreshScope
@ConfigurationProperties("git-hub_endpoints")
@Data
public class Endpoints {
    @NotEmpty
    @NotNull
    private String searchRepoByOwner;
}
