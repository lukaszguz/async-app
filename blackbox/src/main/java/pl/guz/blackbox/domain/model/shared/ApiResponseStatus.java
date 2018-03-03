package pl.guz.blackbox.domain.model.shared;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Accessors(fluent = true)
@Getter
public enum ApiResponseStatus {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);
    private HttpStatus httpStatus;

    ApiResponseStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
