package drone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import drone.common.StepFailedException;
import drone.loadout.BatteryManager;
import drone.loadout.Loadout;
import drone.loadout.LoadoutRepository;
import drone.loadout.LoadoutService;
import drone.loadout.PayloadSensor;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Subsystem B used completely on its own. */
class LoadoutSubsystemTest {
    private LoadoutService service;

    @BeforeEach
    void setUp(@TempDir Path tmp) {
        service = new LoadoutService(new BatteryManager(), new PayloadSensor(), new LoadoutRepository(tmp));
    }

    @Test
    void allocatesBatteryForTrip() {
        Loadout l = service.allocate("X1", 5.0, 2.0);
        assertEquals(25.0, l.getBatteryPercent(), 0.001);
        assertEquals(Loadout.Status.ALLOCATED, l.getStatus());
    }

    @Test
    void heavyPayloadIsRejected() {
        assertThrows(StepFailedException.class, () -> service.allocate("X1", 5.0, 8.0));
        assertEquals(0, service.all().size());
    }

    @Test
    void tooLongTripIsRejected() {
        assertThrows(StepFailedException.class, () -> service.allocate("X1", 50.0, 1.0));
    }

    @Test
    void commitThenReleaseIsIllegal() {
        Loadout l = service.allocate("X1", 5.0, 1.0);
        service.commit(l.getId());
        assertThrows(StepFailedException.class, () -> service.get(l.getId()).release());
        service.releaseByRef("X1"); // no-op because already COMMITTED
        assertEquals(Loadout.Status.COMMITTED, service.get(l.getId()).getStatus());
    }

    @Test
    void releaseIsIdempotent() {
        service.allocate("X1", 5.0, 1.0);
        service.releaseByRef("X1");
        service.releaseByRef("X1");
        assertEquals(Loadout.Status.RELEASED, service.findByRef("X1").orElseThrow().getStatus());
    }

    @Test
    void missingLoadoutIsReported() {
        assertThrows(StepFailedException.class, () -> service.get(5));
    }
}
