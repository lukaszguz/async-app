package pl.allegro.demo.deserializer

import com.fasterxml.jackson.core.JsonParser
import spock.lang.Specification

import java.time.LocalDateTime

class LocalDateTimeDeserializerSpec extends Specification {

    private LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer()

    def "should parse string to LocalDateTime"() {
        given:
        String stringDate = '2017-12-14T13:49:16Z'
        JsonParser jsonParser = Mock(JsonParser) {
            readValueAs(String.class) >> stringDate
        }

        expect:
        localDateTimeDeserializer.deserialize(jsonParser, null) == LocalDateTime.of(2017, 12, 14, 13, 49, 16)
    }
}
