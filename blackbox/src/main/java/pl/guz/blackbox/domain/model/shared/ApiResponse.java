package pl.guz.blackbox.domain.model.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@AllArgsConstructor
@Getter
public class ApiResponse<T> {
    @Setter
    private ApiResponseStatus status;
    private String message;
    private T data;
    private String traceId;

    public ApiResponse(ApiResponseStatus status, String message, String traceId) {
        this.status = status;
        this.message = message;
        this.traceId = traceId;
    }
}