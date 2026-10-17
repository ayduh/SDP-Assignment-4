package drone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import drone.facade.DispatchRecord;
import drone.facade.InvalidOrderException;
import drone.facade.SimulatedCrashException;
import drone.facade.Stage;
import drone.landing.Landing;
import drone.loadout.Loadout;
import drone.routing.Route;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AdminFacadeTest {
    private DroneWorld world;

    @BeforeEach
    void freshWorld(@TempDir Path tmp) {
        world = DroneWorld.freshWorldIn(tmp);
    }

    @Test
    void inspectShowsAllThreeSubsystems() {
        world.delivers("CAMPUS", 2.0);
        String report = world.system().admin().inspect(1);

        assertTrue(report.contains("COMPLETED"));
        assertTrue(report.contains("A route  : CLOSED"));
        assertTrue(report.contains("B loadout: COMMITTED"));
        assertTrue(report.contains("C landing: CONFIRMED"));
    }

    @Test
    void inspectShowsNoneForStepsThatNeverRan() {
        world.deliveryFails("CAMPUS", 9.0);
        String report = world.system().admin().inspect(1);

        assertTrue(report.contains("A route  : RELEASED"));
        assertTrue(report.contains("B loadout: none"));
        assertTrue(report.contains("note"));
    }

    @Test
    void inspectUnknownDispatchFails() {
        assertThrows(InvalidOrderException.class, () -> world.system().admin().inspect(42));
    }

    @Test
    void manualCompensationUndoesInProgressDispatch() {
        world.crashAfter(Stage.B);
        assertThrows(SimulatedCrashException.class, () -> world.delivers("MALL", 1.0));

        world.system().admin().compensate(1);

        world.dispatchIs(1, DispatchRecord.Status.COMPENSATED)
                .routeOf("D1", Route.Status.RELEASED)
                .loadoutOf("D1", Loadout.Status.RELEASED);
    }

    @Test
    void completedDispatchCannotBeCompensated() {
        world.delivers("MALL", 1.0);
        assertThrows(InvalidOrderException.class, () -> world.system().admin().compensate(1));
    }

    @Test
    void recoveryWithNothingPendingDoesNothing() {
        world.delivers("MALL", 1.0);
        assertEquals(0, world.system().admin().recoverAll());
    }

    @Test
    void freeingPadsAllowsDeliveryAgain() {
        world.padBlockedAt("CAMPUS");
        world.deliveryFails("CAMPUS", 1.0);

        world.system().admin().freePads("CAMPUS");

        world.delivers("CAMPUS", 1.0);
        world.dispatchIs(2, DispatchRecord.Status.COMPLETED).landingOf("D2", Landing.Status.CONFIRMED);
    }
}
