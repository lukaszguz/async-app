package pl.guz.blackbox.domain.infrastructure.monitoring;

import io.micrometer.core.instrument.Clock;
import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteHierarchicalNameMapper;
import io.micrometer.graphite.GraphiteMeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class Monitoring {
    @Value("${spring.application.name}")
    String appName;

    @Bean
    public GraphiteMeterRegistry graphiteMeterRegistry(GraphiteConfig config, Clock clock) {
        GraphiteHierarchicalNameMapper graphiteHierarchicalNameMapper = new GraphiteHierarchicalNameMapper(config.tagsAsPrefix());
        return new GraphiteMeterRegistry(
                config,
                clock,
                (id, convention) -> appName + "." + graphiteHierarchicalNameMapper.toHierarchicalName(id, convention)
        );
    }
}
