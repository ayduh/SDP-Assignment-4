package drone.landing;

import drone.common.StepFailedException;

public class Landing {
    public enum Status { BOOKED, CONFIRMED, CANCELLED }

    private final int id;
    private final String orderRef;
    private final String destination;
    private Status status;
    private String confirmationCode;

    public Landing(int id, String orderRef, String destination) {
        this(id, orderRef, destination, Status.BOOKED, "");
    }

    private Landing(int id, String orderRef, String destination, Status status, String code) {
        this.id = id;
        this.orderRef = orderRef;
        this.destination = destination;
        this.status = status;
        this.confirmationCode = code;
    }

    public static Landing fromRow(String[] r) {
        return new Landing(Integer.parseInt(r[0]), r[1], r[2], Status.valueOf(r[3]), r[4]);
    }

    public String[] toRow() {
        return new String[]{String.valueOf(id), orderRef, destination, status.name(), confirmationCode};
    }

    public void confirm(String code) {
        moveTo(Status.CONFIRMED);
        this.confirmationCode = code;
    }

    public void cancel() { moveTo(Status.CANCELLED); }

    private void moveTo(Status next) {
        if (status != Status.BOOKED) {
            throw new StepFailedException("Landing " + id + " is " + status + ", cannot become " + next);
        }
        status = next;
    }

    public int getId() { return id; }
    public String getOrderRef() { return orderRef; }
    public String getDestination() { return destination; }
    public Status getStatus() { return status; }
    public String getConfirmationCode() { return confirmationCode; }
}
