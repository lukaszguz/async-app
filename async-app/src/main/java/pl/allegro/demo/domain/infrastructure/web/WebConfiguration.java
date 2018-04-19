package pl.allegro.demo.domain.infrastructure.web;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpasyncclient.InstrumentedNHttpClientBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.ExceptionMessageErrorParser;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.web.HttpSpanInjector;
import org.springframework.cloud.sleuth.instrument.web.HttpTraceKeysInjector;
import org.springframework.cloud.sleuth.instrument.web.client.TraceAsyncClientHttpRequestFactoryWrapper;
import org.springframework.cloud.sleuth.instrument.web.client.TraceAsyncRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.AsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.web.client.AsyncRestTemplate;

@Configuration
class WebConfiguration {

    @Autowired
    Tracer tracer;
    @Autowired
    HttpTraceKeysInjector httpTraceKeysInjector;
    @Autowired
    HttpSpanInjector spanInjector;
    @Autowired
    MetricRegistry metricRegistry;

    @Bean
    public AsyncRestTemplate traceAsyncRestTemplate(TraceAsyncClientHttpRequestFactoryWrapper customHttpRequestFactoryWrapper) {
        return new TraceAsyncRestTemplate(customHttpRequestFactoryWrapper, tracer, new ExceptionMessageErrorParser());
    }

    @Bean
    @Primary
    public TraceAsyncClientHttpRequestFactoryWrapper customHttpRequestFactoryWrapper() {
        return new TraceAsyncClientHttpRequestFactoryWrapper(tracer,
                                                             spanInjector,
                                                             asyncClientFactory(),
                                                             httpTraceKeysInjector);
    }

    @Bean
    AsyncClientHttpRequestFactory asyncClientFactory() {
        return new HttpComponentsAsyncClientHttpRequestFactory(closeableLoadBalancedHttpAsyncClient());
    }

    @Bean
    CloseableHttpAsyncClient closeableLoadBalancedHttpAsyncClient() {
        IOReactorConfig reactorConfig = IOReactorConfig.custom()
                                                       .setConnectTimeout(300)
                                                       .setSoReuseAddress(true)
                                                       .setSoTimeout(500)
                                                       .build();
        return new InstrumentedNHttpClientBuilder(metricRegistry)
                .setDefaultIOReactorConfig(reactorConfig)
                .setMaxConnPerRoute(100)
                .setMaxConnTotal(200)
                .build();
    }
}
