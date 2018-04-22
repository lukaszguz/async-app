package pl.guz.blackbox.domain.adapter.user.repository

import io.reactivex.schedulers.Schedulers
import pl.guz.blackbox.domain.model.shared.Availability
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier

class SometimesFailureUserRepositorySpec extends Specification {

    private Availability availability = Availability.builder()
            .scheduler(Schedulers.single())
            .build()
    private Integer thresholdException = 10

    def "should correct iteration"() {
        given:
        SometimesFailureUserRepository repository = new SometimesFailureUserRepository(availability, thresholdException, new IntegerSupplier(thresholdException))
        UUID userId = UUID.randomUUID()
        AtomicInteger positiveCounter = new AtomicInteger(0)
        AtomicInteger failureCounter = new AtomicInteger(0)

        expect:
        1.upto(2, {
            1.upto(90, {
                assert repository.load(userId.toString())
                        .blockingGet()
                positiveCounter.incrementAndGet()
            })
            1.upto(10, {
                try {
                    repository.load(userId.toString())
                            .blockingGet()
                } catch (Exception e) {
                    failureCounter.incrementAndGet()
                }
            })
            assert positiveCounter.get() == 90
            assert failureCounter.get() == 10

            positiveCounter.set(0)
            failureCounter.set(0)
        })
    }
}

class IntegerSupplier implements Supplier<Integer> {
    private int thresholdException
    private final int init = 1
    private final int max = 100
    private int counter = init

    IntegerSupplier(int thresholdException) {
        this.thresholdException = thresholdException
    }

    @Override
    Integer get() {
        int old = counter
        if (old > max) {
            counter = init
        }
        return counter++
    }
}
