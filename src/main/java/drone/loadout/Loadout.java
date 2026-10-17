package drone.loadout;

import drone.common.StepFailedException;

/** Entity of subsystem B. ALLOCATED -> COMMITTED or ALLOCATED -> RELEASED, nothing else. */
public class Loadout {
    public enum Status { ALLOCATED, COMMITTED, RELEASED }

    private final int id;
    private final String orderRef;
    private final double batteryPercent;
    private final double payloadKg;
    private Status status;

    public Loadout(int id, String orderRef, double batteryPercent, double payloadKg) {
        this(id, orderRef, batteryPercent, payloadKg, Status.ALLOCATED);
    }

    private Loadout(int id, String orderRef, double batteryPercent, double payloadKg, Status status) {
        this.id = id;
        this.orderRef = orderRef;
        this.batteryPercent = batteryPercent;
        this.payloadKg = payloadKg;
        this.status = status;
    }

    public static Loadout fromRow(String[] r) {
        return new Loadout(Integer.parseInt(r[0]), r[1], Double.parseDouble(r[2]),
                Double.parseDouble(r[3]), Status.valueOf(r[4]));
    }

    public String[] toRow() {
        return new String[]{String.valueOf(id), orderRef, String.valueOf(batteryPercent),
                String.valueOf(payloadKg), status.name()};
    }

    public void commit() { moveTo(Status.COMMITTED); }

    public void release() { moveTo(Status.RELEASED); }

    private void moveTo(Status next) {
        if (status != Status.ALLOCATED) {
            throw new StepFailedException("Loadout " + id + " is " + status + ", cannot become " + next);
        }
        status = next;
    }

    public int getId() { return id; }
    public String getOrderRef() { return orderRef; }
    public double getBatteryPercent() { return batteryPercent; }
    public double getPayloadKg() { return payloadKg; }
    public Status getStatus() { return status; }
}
