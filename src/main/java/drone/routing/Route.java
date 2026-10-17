package drone.routing;

import drone.common.StepFailedException;

/** Entity of subsystem A. RESERVED -> CLOSED or RESERVED -> RELEASED, nothing else. */
public class Route {
    public enum Status { RESERVED, CLOSED, RELEASED }

    private final int id;
    private final String orderRef;
    private final String destination;
    private final double distanceKm;
    private Status status;

    public Route(int id, String orderRef, String destination, double distanceKm) {
        this(id, orderRef, destination, distanceKm, Status.RESERVED);
    }

    private Route(int id, String orderRef, String destination, double distanceKm, Status status) {
        this.id = id;
        this.orderRef = orderRef;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.status = status;
    }

    public static Route fromRow(String[] r) {
        return new Route(Integer.parseInt(r[0]), r[1], r[2], Double.parseDouble(r[3]), Status.valueOf(r[4]));
    }

    public String[] toRow() {
        return new String[]{String.valueOf(id), orderRef, destination, String.valueOf(distanceKm), status.name()};
    }

    public void close() { moveTo(Status.CLOSED); }

    public void release() { moveTo(Status.RELEASED); }

    private void moveTo(Status next) {
        if (status != Status.RESERVED) {
            throw new StepFailedException("Route " + id + " is " + status + ", cannot become " + next);
        }
        status = next;
    }

    public int getId() { return id; }
    public String getOrderRef() { return orderRef; }
    public String getDestination() { return destination; }
    public double getDistanceKm() { return distanceKm; }
    public Status getStatus() { return status; }
}
