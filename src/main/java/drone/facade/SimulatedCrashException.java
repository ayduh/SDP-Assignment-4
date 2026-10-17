package drone.facade;

/** Used by the demo and tests to "kill" the application in the middle of the chain. */
public class SimulatedCrashException extends RuntimeException {
    public SimulatedCrashException(String message) {
        super(message);
    }
}
