package isj01.limits.guard.exceptions;

public class AmountLimitExceededException extends RuntimeException {

    public AmountLimitExceededException(String message) {
        super(message);
    }

}
