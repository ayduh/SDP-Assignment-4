package drone.common;

public class StepFailedException extends RuntimeException {
    public StepFailedException(String message) {
        super(message);
    }
}
