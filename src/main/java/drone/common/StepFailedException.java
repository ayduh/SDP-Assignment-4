package drone.common;

/** Thrown by any subsystem when one of its own steps cannot be done. */
public class StepFailedException extends RuntimeException {
    public StepFailedException(String message) {
        super(message);
    }
}
