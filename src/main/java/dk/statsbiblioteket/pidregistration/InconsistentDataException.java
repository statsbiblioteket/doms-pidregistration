package dk.statsbiblioteket.pidregistration;

/**
 * Exception signifying inconsistent data.
 */
public class InconsistentDataException extends RuntimeException {
    public InconsistentDataException(String message) {
        super(message);
    }

    public InconsistentDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
