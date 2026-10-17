package drone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import drone.facade.DispatchFailedException;
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

class DispatchFacadeTest {
    private DroneWorld world;

    @BeforeEach
    void freshWorld(@TempDir Path tmp) {
        world = DroneWorld.freshWorldIn(tmp);
    }

    @Test
    void happyPathCompletesAllThreeSteps() {
        DispatchRecord d = world.delivers("campus", 2.0);

        assertEquals("DLV-D1", d.getCode());
        world.dispatchIs(1, DispatchRecord.Status.COMPLETED)
                .routeOf("D1", Route.Status.CLOSED)
                .loadoutOf("D1", Loadout.Status.COMMITTED)
                .landingOf("D1", Landing.Status.CONFIRMED);
    }

    @Test
    void whenBFailsOnlyARouteIsUndone() {
        DispatchFailedException e = world.deliveryFails("CAMPUS", 8.0);

        assertEquals(Stage.B, e.getFailedStage());
        world.dispatchIs(1, DispatchRecord.Status.COMPENSATED)
                .routeOf("D1", Route.Status.RELEASED)
                .noLoadoutFor("D1")
                .noLandingFor("D1");
    }

    @Test
    void whenCFailsLoadoutAndRouteAreUndone() {
        world.padBlockedAt("CAMPUS");
        DispatchFailedException e = world.deliveryFails("CAMPUS", 2.0);

        assertEquals(Stage.C, e.getFailedStage());
        world.dispatchIs(1, DispatchRecord.Status.COMPENSATED)
                .routeOf("D1", Route.Status.RELEASED)
                .loadoutOf("D1", Loadout.Status.RELEASED)
                .noLandingFor("D1");
    }

    @Test
    void whenWeatherIsBadNothingIsCommitted() {
        world.windIs(70);
        DispatchFailedException e = world.deliveryFails("CAMPUS", 2.0);

        assertEquals(Stage.A, e.getFailedStage());
        world.dispatchIs(1, DispatchRecord.Status.REJECTED)
                .noRouteFor("D1").noLoadoutFor("D1").noLandingFor("D1");
    }

    @Test
    void unknownDestinationIsRejectedAtStepA() {
        world.deliveryFails("MARS", 1.0);
        world.dispatchIs(1, DispatchRecord.Status.REJECTED);
    }

    @Test
    void invalidInputIsRejectedBeforeAnythingIsSaved() {
        assertThrows(InvalidOrderException.class, () -> world.delivers("  ", 2.0));
        assertThrows(InvalidOrderException.class, () -> world.delivers(null, 2.0));
        assertThrows(InvalidOrderException.class, () -> world.delivers("CAMPUS", 0));
        assertEquals(0, world.system().admin().list().size());
    }

    @Test
    void completedDispatchSurvivesRestart() {
        world.delivers("MALL", 1.0);
        world.restart();

        world.dispatchIs(1, DispatchRecord.Status.COMPLETED).landingOf("D1", Landing.Status.CONFIRMED);
        assertEquals("D2", world.delivers("MALL", 1.0).ref());
    }

    @Test
    void crashAfterBIsRecoveredFromPersistedStatus() {
        world.crashAfter(Stage.B);
        assertThrows(SimulatedCrashException.class, () -> world.delivers("CAMPUS", 2.0));

        world.restart();
        world.dispatchIs(1, DispatchRecord.Status.PENDING_C)
                .routeOf("D1", Route.Status.RESERVED)
                .loadoutOf("D1", Loadout.Status.ALLOCATED);

        assertEquals(1, world.system().admin().recoverAll());
        world.dispatchIs(1, DispatchRecord.Status.COMPENSATED)
                .routeOf("D1", Route.Status.RELEASED)
                .loadoutOf("D1", Loadout.Status.RELEASED);
    }

    @Test
    void crashAfterAIsRecoveredFromPersistedStatus() {
        world.crashAfter(Stage.A);
        assertThrows(SimulatedCrashException.class, () -> world.delivers("CAMPUS", 2.0));

        world.restart();
        world.dispatchIs(1, DispatchRecord.Status.PENDING_B).routeOf("D1", Route.Status.RESERVED);

        assertEquals(1, world.system().admin().recoverAll());
        world.dispatchIs(1, DispatchRecord.Status.COMPENSATED)
                .routeOf("D1", Route.Status.RELEASED).noLoadoutFor("D1");
    }
}
