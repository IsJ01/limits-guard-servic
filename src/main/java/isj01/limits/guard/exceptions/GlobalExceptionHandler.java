package isj01.limits.guard.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConcurrentTransactionException.class)
    public ResponseEntity<String> handleConcurrentError(ConcurrentTransactionException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(AmountLimitExceededException.class)
    public ResponseEntity<String> handleRateLimit(AmountLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(e.getMessage());
    }

    @ExceptionHandler(LimitExceededException.class)
    public ResponseEntity<String> handleLimitExceeded(LimitExceededException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
    }
}
