package drone.facade;

public class DispatchRecord {

    public enum Status { PENDING_A, PENDING_B, PENDING_C, COMPLETED, COMPENSATED, REJECTED }

    private final int id;
    private final String destination;
    private final double payloadKg;
    private int routeId;
    private int loadoutId;
    private int landingId;
    private Status status;
    private String code = "";
    private String note = "";

    public DispatchRecord(int id, String destination, double payloadKg) {
        this.id = id;
        this.destination = destination;
        this.payloadKg = payloadKg;
        this.status = Status.PENDING_A;
    }

    public static DispatchRecord fromRow(String[] r) {
        DispatchRecord d = new DispatchRecord(Integer.parseInt(r[0]), r[1], Double.parseDouble(r[2]));
        d.routeId = Integer.parseInt(r[3]);
        d.loadoutId = Integer.parseInt(r[4]);
        d.landingId = Integer.parseInt(r[5]);
        d.status = Status.valueOf(r[6]);
        d.code = r[7];
        d.note = r[8];
        return d;
    }

    public String[] toRow() {
        return new String[]{String.valueOf(id), destination, String.valueOf(payloadKg),
                String.valueOf(routeId), String.valueOf(loadoutId), String.valueOf(landingId),
                status.name(), code, note.replace(';', ',')};
    }

    public String ref() { return "D" + id; }

    public boolean isInProgress() {
        return status == Status.PENDING_A || status == Status.PENDING_B || status == Status.PENDING_C;
    }

    public int getId() { return id; }
    public String getDestination() { return destination; }
    public double getPayloadKg() { return payloadKg; }
    public int getRouteId() { return routeId; }
    public int getLoadoutId() { return loadoutId; }
    public int getLandingId() { return landingId; }
    public Status getStatus() { return status; }
    public String getCode() { return code; }
    public String getNote() { return note; }

    public void setRouteId(int routeId) { this.routeId = routeId; }
    public void setLoadoutId(int loadoutId) { this.loadoutId = loadoutId; }
    public void setLandingId(int landingId) { this.landingId = landingId; }
    public void setStatus(Status status) { this.status = status; }
    public void setCode(String code) { this.code = code; }
    public void setNote(String note) { this.note = note; }
}
