package pl.allegro.demo.domain.infrastructure.monitoring;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.lang.Nullable;
import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteHierarchicalNameMapper;
import io.micrometer.graphite.GraphiteMeterRegistry;
import io.micrometer.graphite.GraphiteNamingConvention;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.Normalizer;
import java.util.regex.Pattern;

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
                (id, convention) -> appName + "." + graphiteHierarchicalNameMapper.toHierarchicalName(id, new GraphiteNamingConvention())
        );
    }

    class GraphiteNamingConvention implements NamingConvention {
        private final Pattern blacklistedChars = Pattern.compile("[{}(),=\\[\\]/]");

        @Override
        public String name(String name, Meter.Type type, @Nullable String baseUnit) {
            return format(name);
        }

        @Override
        public String tagKey(String key) {
            return format(key);
        }

        @Override
        public String tagValue(String value) {
            return format(value);
        }

        private String format(String name) {
            String sanitized = Normalizer.normalize(name, Normalizer.Form.NFKD);
            return blacklistedChars.matcher(sanitized)
                                   .replaceAll("_");
        }
    }
}
