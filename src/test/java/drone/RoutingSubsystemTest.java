package drone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import drone.common.StepFailedException;
import drone.routing.Route;
import drone.routing.RoutePlanner;
import drone.routing.RouteRepository;
import drone.routing.RouteService;
import drone.routing.WeatherChecker;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Subsystem A used completely on its own (no B, no C, no facade). */
class RoutingSubsystemTest {
    private RouteService service;
    private Path dir;

    @BeforeEach
    void setUp(@TempDir Path tmp) {
        dir = tmp;
        service = new RouteService(new RoutePlanner(), new WeatherChecker(), new RouteRepository(dir));
    }

    @Test
    void reservesRouteWithDistance() {
        Route r = service.reserve("X1", "CAMPUS");
        assertEquals(5.0, r.getDistanceKm(), 0.001);
        assertEquals(Route.Status.RESERVED, r.getStatus());
    }

    @Test
    void routeSurvivesReopeningTheStore() {
        service.reserve("X1", "MALL");
        RouteService reopened = new RouteService(new RoutePlanner(), new WeatherChecker(), new RouteRepository(dir));
        assertEquals(10.0, reopened.findByRef("X1").orElseThrow().getDistanceKm(), 0.001);
    }

    @Test
    void strongWindRejectsReservation() {
        service.setWind(60);
        assertThrows(StepFailedException.class, () -> service.reserve("X1", "CAMPUS"));
        assertEquals(0, service.all().size());
    }

    @Test
    void unknownDestinationIsRejected() {
        assertThrows(StepFailedException.class, () -> service.reserve("X1", "MARS"));
    }

    @Test
    void closedRouteCannotBeReleased() {
        Route r = service.reserve("X1", "CAMPUS");
        service.close(r.getId());
        assertThrows(StepFailedException.class, () -> service.get(r.getId()).release());
        service.releaseByRef("X1"); // idempotent no-op
        assertEquals(Route.Status.CLOSED, service.get(r.getId()).getStatus());
    }

    @Test
    void releaseIsIdempotent() {
        service.reserve("X1", "CAMPUS");
        service.releaseByRef("X1");
        service.releaseByRef("X1");
        assertEquals(Route.Status.RELEASED, service.findByRef("X1").orElseThrow().getStatus());
    }

    @Test
    void missingRouteIsReported() {
        assertThrows(StepFailedException.class, () -> service.get(99));
    }
}
