package drone.facade;

import drone.landing.Landing;
import drone.landing.LandingService;
import drone.loadout.Loadout;
import drone.loadout.LoadoutService;
import drone.routing.Route;
import drone.routing.RouteService;
import java.util.List;
import java.util.Optional;

public class AdminFacade {
    private final RouteService routes;
    private final LoadoutService loadouts;
    private final LandingService landings;
    private final DispatchLog log;
    private final ChainCompensator compensator;

    public AdminFacade(RouteService routes, LoadoutService loadouts, LandingService landings,
                       DispatchLog log, ChainCompensator compensator) {
        this.routes = routes;
        this.loadouts = loadouts;
        this.landings = landings;
        this.log = log;
        this.compensator = compensator;
    }

    public List<DispatchRecord> list() { return log.findAll(); }

    public String inspect(int dispatchId) {
        DispatchRecord d = find(dispatchId);
        Optional<Route> r = routes.findByRef(d.ref());
        Optional<Loadout> l = loadouts.findByRef(d.ref());
        Optional<Landing> p = landings.findByRef(d.ref());
        return "Dispatch " + d.ref() + " -> " + d.getDestination() + ", " + d.getPayloadKg()
                + " kg, status " + d.getStatus() + (d.getCode().isEmpty() ? "" : ", code " + d.getCode()) + "\n"
                + "  A route  : " + (r.isPresent() ? r.get().getStatus() + " (" + r.get().getDistanceKm() + " km)" : "none") + "\n"
                + "  B loadout: " + (l.isPresent() ? l.get().getStatus() + " (battery " + l.get().getBatteryPercent() + " %)" : "none") + "\n"
                + "  C landing: " + (p.isPresent() ? p.get().getStatus() : "none") + "\n"
                + (d.getNote().isEmpty() ? "" : "  note     : " + d.getNote() + "\n");
    }

    public void compensate(int dispatchId) {
        DispatchRecord d = find(dispatchId);
        if (!d.isInProgress()) {
            throw new InvalidOrderException("Dispatch " + d.ref() + " is " + d.getStatus() + ", nothing to compensate");
        }
        compensator.compensate(d, Stage.C, "manual compensation");
    }

    public int recoverAll() {
        int count = 0;
        for (DispatchRecord d : log.findAll()) {
            if (d.isInProgress()) {
                compensator.compensate(d, Stage.C, "recovered after restart from " + d.getStatus());
                count++;
            }
        }
        return count;
    }

    public void setWind(double kmh) { routes.setWind(kmh); }

    public void blockPad(String destination) { landings.blockPad(destination.trim().toUpperCase()); }

    public void freePads(String destination) { landings.freeBlockedPads(destination.trim().toUpperCase()); }

    private DispatchRecord find(int id) {
        return log.findById(id).orElseThrow(() -> new InvalidOrderException("No dispatch with id " + id));
    }
}
