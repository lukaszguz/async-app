package pl.allegro.demo.domain.model.shared

import com.codahale.metrics.Metric
import com.codahale.metrics.MetricRegistry
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import spock.lang.Specification

import java.time.Duration

class CircuitBreakerFactorySpec extends Specification {

    private MetricRegistry metricRegistry = Mock(MetricRegistry)
    private CircuitBreakerFactory circuitBreakerFactory = new CircuitBreakerFactory(metricRegistry)

    def "should create new default circuit breaker and add metrics to it"() {
        given:
        String name = 'test'

        when:
        CircuitBreaker circuitBreaker = circuitBreakerFactory.defaultCircuitBreakerOf(name)

        then:
        1 * metricRegistry.register('circuit_breaker-test.state', _ as Metric)
        1 * metricRegistry.register('circuit_breaker-test.buffered', _ as Metric)
        1 * metricRegistry.register('circuit_breaker-test.failed', _ as Metric)
        1 * metricRegistry.register('circuit_breaker-test.buffered_max', _ as Metric)
        1 * metricRegistry.register('circuit_breaker-test.successful', _ as Metric)
        1 * metricRegistry.register('circuit_breaker-test.not_permitted', _ as Metric)

        expect:
        circuitBreaker.name == "circuit_breaker-$name"
        circuitBreaker.getCircuitBreakerConfig().waitDurationInOpenState == Duration.ofSeconds(10)
        circuitBreaker.getCircuitBreakerConfig().ringBufferSizeInHalfOpenState == 3 as int
        circuitBreaker.getCircuitBreakerConfig().ringBufferSizeInClosedState == 5 as int
    }

    def "should create new circuit breaker and add metrics to it"() {
        given:
        String name = 'test'
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.ofDefaults()

        when:
        CircuitBreaker circuitBreaker = circuitBreakerFactory.circuitBreaker(name, { circuitBreakerConfig })

        then:
        1 * metricRegistry.register('circuit_breaker-test.state', _ as Metric)
        1 * metricRegistry.register('circuit_breaker-test.buffered', _ as Metric)
        1 * metricRegistry.register('circuit_breaker-test.failed', _ as Metric)
        1 * metricRegistry.register('circuit_breaker-test.buffered_max', _ as Metric)
        1 * metricRegistry.register('circuit_breaker-test.successful', _ as Metric)
        1 * metricRegistry.register('circuit_breaker-test.not_permitted', _ as Metric)

        expect:
        circuitBreaker.name == "circuit_breaker-$name"
        circuitBreaker.getCircuitBreakerConfig() == circuitBreakerConfig
    }
}
