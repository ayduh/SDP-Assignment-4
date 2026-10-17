package drone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import drone.app.DroneSystem;
import drone.facade.DispatchFailedException;
import drone.facade.DispatchRecord;
import drone.facade.Stage;
import drone.landing.Landing;
import drone.loadout.Loadout;
import drone.routing.Route;
import java.nio.file.Path;

/** Small domain-specific testing language: tests read like sentences about drones. */
final class DroneWorld {
    private final Path dir;
    private DroneSystem system;

    private DroneWorld(Path dir) {
        this.dir = dir;
        this.system = new DroneSystem(dir);
    }

    static DroneWorld freshWorldIn(Path dir) { return new DroneWorld(dir); }

    DroneSystem system() { return system; }

    DroneWorld windIs(double kmh) { system.admin().setWind(kmh); return this; }

    DroneWorld padBlockedAt(String destination) { system.admin().blockPad(destination); return this; }

    DroneWorld crashAfter(Stage stage) { system.dispatch().crashAfter(stage); return this; }

    /** Simulates stopping and restarting the application: everything is re-read from disk. */
    DroneWorld restart() { system = new DroneSystem(dir); return this; }

    DispatchRecord delivers(String destination, double kg) { return system.dispatch().dispatch(destination, kg); }

    DispatchFailedException deliveryFails(String destination, double kg) {
        return assertThrows(DispatchFailedException.class, () -> system.dispatch().dispatch(destination, kg));
    }

    DroneWorld dispatchIs(int id, DispatchRecord.Status expected) {
        assertEquals(expected, system.admin().list().stream().filter(d -> d.getId() == id)
                .findFirst().orElseThrow().getStatus());
        return this;
    }

    DroneWorld routeOf(String ref, Route.Status expected) {
        assertEquals(expected, system.routes().findByRef(ref).orElseThrow().getStatus());
        return this;
    }

    DroneWorld loadoutOf(String ref, Loadout.Status expected) {
        assertEquals(expected, system.loadouts().findByRef(ref).orElseThrow().getStatus());
        return this;
    }

    DroneWorld landingOf(String ref, Landing.Status expected) {
        assertEquals(expected, system.landings().findByRef(ref).orElseThrow().getStatus());
        return this;
    }

    DroneWorld noRouteFor(String ref) { assertTrue(system.routes().findByRef(ref).isEmpty()); return this; }

    DroneWorld noLoadoutFor(String ref) { assertTrue(system.loadouts().findByRef(ref).isEmpty()); return this; }

    DroneWorld noLandingFor(String ref) { assertTrue(system.landings().findByRef(ref).isEmpty()); return this; }
}
