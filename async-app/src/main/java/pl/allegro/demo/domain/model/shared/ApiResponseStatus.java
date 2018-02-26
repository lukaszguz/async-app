package pl.allegro.demo.domain.model.shared;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Accessors(fluent = true)
@Getter
public enum ApiResponseStatus {
    NOT_FOUND(HttpStatus.NOT_FOUND),
    SERVICE_CURRENTLY_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY);

    private HttpStatus httpStatus;

    ApiResponseStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
