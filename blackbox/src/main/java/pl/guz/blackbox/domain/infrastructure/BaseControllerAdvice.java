package pl.guz.blackbox.domain.infrastructure;

import io.github.resilience4j.circuitbreaker.CircuitBreakerOpenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.guz.blackbox.domain.model.exception.ApplicationException;
import pl.guz.blackbox.domain.model.shared.ApiResponse;
import pl.guz.blackbox.domain.model.shared.ApiResponseStatus;

import java.util.UUID;

@RestControllerAdvice(basePackages = "pl.allegro.demo.domain.model")
@Slf4j
class BaseControllerAdvice {

    @ExceptionHandler(CircuitBreakerOpenException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ResponseBody
    public ApiResponse handleException(CircuitBreakerOpenException ex) {
        final String uuidException = UUID.randomUUID()
                                         .toString();
        log.error("ERROR - " + uuidException + ", message: " + ex.getMessage(), ex);
        return new ApiResponse(ApiResponseStatus.SERVICE_CURRENTLY_UNAVAILABLE, "Service is overloaded", uuidException);
    }

    @ExceptionHandler(ApplicationException.class)
    @ResponseBody
    public ResponseEntity handleException(ApplicationException ex) {
        final String uuidException = UUID.randomUUID()
                                         .toString();
        log.error("ERROR - " + uuidException + ", message: " + ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatus()
                                       .httpStatus())
                             .body(new ApiResponse(ex.getStatus(), ex.getMessage(), uuidException));
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity handleException(Exception ex) {
        final String uuidException = UUID.randomUUID()
                                         .toString();
        log.error("ERROR - " + uuidException + ", message: " + ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(new ApiResponse(ApiResponseStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), uuidException));
    }
}
