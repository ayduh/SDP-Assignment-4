package drone.facade;

import drone.common.StepFailedException;
import drone.landing.Landing;
import drone.landing.LandingService;
import drone.loadout.Loadout;
import drone.loadout.LoadoutService;
import drone.routing.Route;
import drone.routing.RouteService;

public class DispatchFacade {
    private final RouteService routes;
    private final LoadoutService loadouts;
    private final LandingService landings;
    private final DispatchLog log;
    private final ChainCompensator compensator;
    private Stage crashAfter;

    public DispatchFacade(RouteService routes, LoadoutService loadouts, LandingService landings,
                          DispatchLog log, ChainCompensator compensator) {
        this.routes = routes;
        this.loadouts = loadouts;
        this.landings = landings;
        this.log = log;
        this.compensator = compensator;
    }

    public void crashAfter(Stage stage) { this.crashAfter = stage; }

    public DispatchRecord dispatch(String destination, double payloadKg) {
        if (destination == null || destination.isBlank()) {
            throw new InvalidOrderException("Destination is required");
        }
        if (payloadKg <= 0) {
            throw new InvalidOrderException("Payload must be positive");
        }
        DispatchRecord record = log.create(destination.trim().toUpperCase(), payloadKg);
        String ref = record.ref();

        Route route;
        try {
            route = routes.reserve(ref, record.getDestination());
        } catch (StepFailedException e) {
            record.setStatus(DispatchRecord.Status.REJECTED);
            record.setNote("A failed: " + e.getMessage());
            log.save(record);
            throw new DispatchFailedException(record, Stage.A, "Step A failed, nothing to undo: " + e.getMessage());
        }
        record.setRouteId(route.getId());
        record.setStatus(DispatchRecord.Status.PENDING_B);
        log.save(record);
        crashPoint(Stage.A);

        Loadout loadout;
        try {
            loadout = loadouts.allocate(ref, route.getDistanceKm(), record.getPayloadKg());
        } catch (StepFailedException e) {
            compensator.compensate(record, Stage.A, "B failed: " + e.getMessage());
            throw new DispatchFailedException(record, Stage.B, "Step B failed, route released: " + e.getMessage());
        }
        record.setLoadoutId(loadout.getId());
        record.setStatus(DispatchRecord.Status.PENDING_C);
        log.save(record);
        crashPoint(Stage.B);

        Landing landing;
        try {
            landing = landings.book(ref, record.getDestination());
        } catch (StepFailedException e) {
            compensator.compensate(record, Stage.B, "C failed: " + e.getMessage());
            throw new DispatchFailedException(record, Stage.C,
                    "Step C failed, loadout and route released: " + e.getMessage());
        }
        record.setLandingId(landing.getId());

        routes.close(route.getId());
        loadouts.commit(loadout.getId());
        record.setCode(landings.confirm(landing.getId()));
        record.setStatus(DispatchRecord.Status.COMPLETED);
        log.save(record);
        return record;
    }

    private void crashPoint(Stage stage) {
        if (crashAfter == stage) {
            crashAfter = null;
            throw new SimulatedCrashException("Application crashed after step " + stage);
        }
    }
}
