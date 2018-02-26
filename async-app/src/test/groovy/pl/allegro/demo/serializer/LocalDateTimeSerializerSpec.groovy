package pl.allegro.demo.serializer

import com.fasterxml.jackson.core.JsonGenerator
import spock.lang.Specification

import java.time.LocalDateTime

class LocalDateTimeSerializerSpec extends Specification {

    private LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer()

    def "should serialize date time to string"() {
        given:
        LocalDateTime dateTime = LocalDateTime.of(2017, 12, 14, 13, 49, 16)
        JsonGenerator jsonGenerator = Mock(JsonGenerator)

        when:
        localDateTimeSerializer.serialize(dateTime, jsonGenerator, null)

        then:
        1 * jsonGenerator.writeString('2017-12-14T13:49:16')
    }
}
