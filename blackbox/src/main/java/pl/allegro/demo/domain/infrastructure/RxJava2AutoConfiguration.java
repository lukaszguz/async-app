package pl.allegro.demo.domain.infrastructure;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.SchedulerRunnableIntrospection;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} that enables support for RxJava2 via {@link RxJavaPlugins}.
 * https://github.com/spring-cloud/spring-cloud-sleuth/pull/775
 */
@Configuration
@AutoConfigureAfter(TraceAutoConfiguration.class)
@ConditionalOnBean(Tracer.class)
@ConditionalOnClass(SchedulerRunnableIntrospection.class)
@ConditionalOnProperty(value = "spring.sleuth.rxjava2.schedulers.hook.enabled", matchIfMissing = true)
@EnableConfigurationProperties(SleuthRxJava2SchedulersProperties.class)
public class RxJava2AutoConfiguration {

    @Bean
    SleuthRxJava2SchedulersHandler sleuthRxJava2SchedulersHook(Tracer tracer,
                                                               TraceKeys traceKeys,
                                                               SleuthRxJava2SchedulersProperties sleuthRxJava2SchedulersProperties) {
        return new SleuthRxJava2SchedulersHandler(tracer, traceKeys,
                                                  Arrays.asList(sleuthRxJava2SchedulersProperties.getIgnoredthreads()));
    }
}
