package pl.guz.blackbox.domain.model.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import pl.guz.blackbox.domain.model.shared.ApiResponseStatus;

import java.util.function.Supplier;

@EqualsAndHashCode(callSuper = true)
@Getter
@ToString(callSuper = true)
public class ApplicationException extends RuntimeException {

    private ApiResponseStatus status;
    private Object data;

    public ApplicationException() {
    }

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(ApiResponseStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ApplicationException(ApiResponseStatus status) {
        super(status.name());
        this.status = status;
    }

    public ApplicationException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public static Supplier<RuntimeException> ofStatus(ApiResponseStatus apiResponseStatus) {
        return () -> new ApplicationException(apiResponseStatus);
    }
}