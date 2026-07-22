package isj01.limits.guard.exceptions;

public class LimitExceededException extends RuntimeException {

    public LimitExceededException(String message) {
        super(message);
    }

}
