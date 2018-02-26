package pl.allegro.demo.domain.model.shared

import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import spock.lang.Specification

class HttpPredicatesSpec extends Specification {

    def "should check exception contains 4xx status"() {
        expect:
        HttpPredicates.is4xxStatus().test(exception) == result

        where:
        exception                                                      | result
        new HttpClientErrorException(HttpStatus.ACCEPTED)              | false
        new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR) | false
        new RuntimeException()                                         | false
        new HttpClientErrorException(HttpStatus.FORBIDDEN)             | true
    }
}
