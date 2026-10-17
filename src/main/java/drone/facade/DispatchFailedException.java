package drone.facade;

public class DispatchFailedException extends RuntimeException {
    private final DispatchRecord record;
    private final Stage failedStage;

    public DispatchFailedException(DispatchRecord record, Stage failedStage, String message) {
        super(message);
        this.record = record;
        this.failedStage = failedStage;
    }

    public DispatchRecord getRecord() { return record; }

    public Stage getFailedStage() { return failedStage; }
}
