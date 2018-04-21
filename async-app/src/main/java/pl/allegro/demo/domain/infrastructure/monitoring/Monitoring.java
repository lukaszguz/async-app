package pl.allegro.demo.domain.infrastructure.monitoring;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
class Monitoring {
    @Autowired
    ObjectProvider<Collection<PublicMetrics>> publicMetrics;

    @Bean
    GraphiteReporter graphiteReporter(MetricRegistry metricRegistry) {
        Graphite graphite = new Graphite(new InetSocketAddress("localhost", 2003));
        final GraphiteReporter reporter = GraphiteReporter.forRegistry(metricRegistry)
                                                          .prefixedWith("async-app")
                                                          .convertRatesTo(TimeUnit.MILLISECONDS)
                                                          .convertDurationsTo(TimeUnit.MILLISECONDS)
                                                          .build(graphite);
        reporter.start(1, TimeUnit.SECONDS);
        return reporter;
    }
}
