package drone.facade;

import drone.landing.LandingService;
import drone.loadout.LoadoutService;
import drone.routing.RouteService;

/** Undoes a partially finished chain in reverse order: C, then B, then A. Shared by both facades. */
public class ChainCompensator {
    private final RouteService routes;
    private final LoadoutService loadouts;
    private final LandingService landings;
    private final DispatchLog log;

    public ChainCompensator(RouteService routes, LoadoutService loadouts, LandingService landings, DispatchLog log) {
        this.routes = routes;
        this.loadouts = loadouts;
        this.landings = landings;
        this.log = log;
    }

    /** @param lastCommitted the last step that may have committed (B failed -> A, C failed -> B). */
    public void compensate(DispatchRecord record, Stage lastCommitted, String reason) {
        String ref = record.ref();
        if (lastCommitted == Stage.C) {
            landings.cancelByRef(ref);
        }
        if (lastCommitted == Stage.C || lastCommitted == Stage.B) {
            loadouts.releaseByRef(ref);
        }
        routes.releaseByRef(ref);
        record.setStatus(DispatchRecord.Status.COMPENSATED);
        record.setNote(reason);
        log.save(record);
    }
}
