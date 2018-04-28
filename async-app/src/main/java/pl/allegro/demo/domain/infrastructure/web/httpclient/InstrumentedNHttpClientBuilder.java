package pl.allegro.demo.domain.infrastructure.web.httpclient;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.concurrent.Future;

import static java.util.Objects.*;

public class InstrumentedNHttpClientBuilder extends HttpAsyncClientBuilder {
    private final MeterRegistry meterRegistry;
    private final String name;
    private final HttpClientMetricNameStrategy metricNameStrategy;

    public InstrumentedNHttpClientBuilder(MeterRegistry meterRegistry, HttpClientMetricNameStrategy metricNameStrategy,
                                          String name) {
        super();
        this.meterRegistry = meterRegistry;
        this.metricNameStrategy = metricNameStrategy;
        this.name = name;
    }

    public InstrumentedNHttpClientBuilder(MeterRegistry meterRegistry) {
        this(meterRegistry, HttpClientMetricNameStrategies.METHOD_ONLY, null);
    }

    public InstrumentedNHttpClientBuilder(MeterRegistry meterRegistry, HttpClientMetricNameStrategy metricNameStrategy) {
        this(meterRegistry, metricNameStrategy, null);
    }

    public InstrumentedNHttpClientBuilder(MeterRegistry meterRegistry, String name) {
        this(meterRegistry, HttpClientMetricNameStrategies.METHOD_ONLY, name);
    }

    private Timer timer(HttpRequest request) {
        return meterRegistry.timer(metricNameStrategy.getNameFor(name, request));
    }

    @Override
    public CloseableHttpAsyncClient build() {
        final CloseableHttpAsyncClient ac = super.build();
        return new CloseableHttpAsyncClient() {

            @Override
            public boolean isRunning() {
                return ac.isRunning();
            }

            @Override
            public void start() {
                ac.start();
            }

            @Override
            public <T> Future<T> execute(HttpAsyncRequestProducer requestProducer, HttpAsyncResponseConsumer<T> responseConsumer, HttpContext context, FutureCallback<T> callback) {
                final Timer.Sample sample = Timer.start(meterRegistry);
                final Timer timer;
                try {
                    timer = timer(requestProducer.generateRequest());
                } catch (IOException | HttpException ex) {
                    throw new RuntimeException(ex);
                }
                return ac.execute(requestProducer, responseConsumer, context,
                                  new TimingFutureCallback<>(callback, sample, timer));
            }

            @Override
            public void close() throws IOException {
                ac.close();
            }
        };
    }

    private static class TimingFutureCallback<T> implements FutureCallback<T> {
        private final FutureCallback<T> callback;
        private final Timer.Sample sample;
        private final Timer timer;

        private TimingFutureCallback(FutureCallback<T> callback,
                                     Timer.Sample timerContext,
                                     Timer timer) {
            this.callback = callback;
            this.sample = requireNonNull(timerContext, "timerContext");
            this.timer = timer;
        }

        @Override
        public void completed(T result) {
            sample.stop(timer);
            if (callback != null) {
                callback.completed(result);
            }
        }

        @Override
        public void failed(Exception ex) {
            sample.stop(timer);
            if (callback != null) {
                callback.failed(ex);
            }
        }

        @Override
        public void cancelled() {
            sample.stop(timer);
            if (callback != null) {
                callback.cancelled();
            }
        }
    }
}

