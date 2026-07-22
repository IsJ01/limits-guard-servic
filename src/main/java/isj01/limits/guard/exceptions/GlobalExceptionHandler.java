package isj01.limits.guard.exceptions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("details", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConcurrentTransactionException.class)
    public ResponseEntity<ErrorDto> handleConcurrentError(ConcurrentTransactionException e) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorDto(HttpStatus.CONFLICT.value(), e.getMessage()));
    }

    @ExceptionHandler(AmountLimitExceededException.class)
    public ResponseEntity<ErrorDto> handleRateLimit(AmountLimitExceededException e) {
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_CONTENT)
            .body(new ErrorDto(HttpStatus.UNPROCESSABLE_CONTENT.value(), e.getMessage()));
    }

    @ExceptionHandler(LimitExceededException.class)
    public ResponseEntity<ErrorDto> handleLimitExceeded(LimitExceededException e) {
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(new ErrorDto(HttpStatus.TOO_MANY_REQUESTS.value(), e.getMessage()));
    }

    public record ErrorDto(int status, String message, LocalDateTime timestamp) {
        public ErrorDto(int status, String message) {
            this(status, message, LocalDateTime.now());
        }
    }

}
