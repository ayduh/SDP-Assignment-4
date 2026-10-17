package drone.facade;

public class SimulatedCrashException extends RuntimeException {
    public SimulatedCrashException(String message) {
        super(message);
    }
}
