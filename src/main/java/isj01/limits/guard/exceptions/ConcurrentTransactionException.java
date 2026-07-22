package isj01.limits.guard.exceptions;

public class ConcurrentTransactionException extends RuntimeException {

    public ConcurrentTransactionException(String message) {
        super(message);
    }

}
