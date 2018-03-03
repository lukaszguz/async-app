package pl.guz.blackbox.domain.adapter.user.repository

import io.reactivex.schedulers.Schedulers
import pl.guz.blackbox.domain.model.shared.Availability
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger

class SometimesFailureUserRepositorySpec extends Specification {

    private Availability availability = Availability.builder()
            .scheduler(Schedulers.single())
            .build()
    private SometimesFailureUserRepository repository = new SometimesFailureUserRepository(availability)

    def "should 8 times return user and 2 times return failure"() {
        given:
        UUID userId = UUID.randomUUID()

        expect:
        1.upto(8, {
            assert repository.load(userId.toString())
                    .blockingGet()
        })

        when: 'first failure time'
        assert repository.load(userId.toString())
                .blockingGet()
        then:
        thrown(Exception)

        when: 'second failure time'
        assert repository.load(userId.toString())
                .blockingGet()
        then:
        thrown(Exception)

        and: 'next correct'
        assert repository.load(userId.toString())
                .blockingGet()
    }

    def "should correct iteration"() {
        given:
        SometimesFailureUserRepository repository = new SometimesFailureUserRepository(availability)
        UUID userId = UUID.randomUUID()
        AtomicInteger positiveCounter = new AtomicInteger(0)
        AtomicInteger failureCounter = new AtomicInteger(0)

        expect:
        1.upto(2, {
            1.upto(8, {
                assert repository.load(userId.toString())
                        .blockingGet()
                positiveCounter.incrementAndGet()
            })
            1.upto(2, {
                try {
                    repository.load(userId.toString())
                            .blockingGet()
                } catch (Exception e) {
                    failureCounter.incrementAndGet()
                }
            })
            assert positiveCounter.get() == 8
            assert failureCounter.get() == 2

            positiveCounter.set(0)
            failureCounter.set(0)
        })

    }
}
