package drone.facade;

/** Bad client input, detected before anything is persisted. */
public class InvalidOrderException extends RuntimeException {
    public InvalidOrderException(String message) {
        super(message);
    }
}
