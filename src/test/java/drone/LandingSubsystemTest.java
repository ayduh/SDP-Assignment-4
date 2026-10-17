package drone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import drone.common.StepFailedException;
import drone.landing.DeliveryConfirmation;
import drone.landing.Landing;
import drone.landing.LandingPadController;
import drone.landing.LandingRepository;
import drone.landing.LandingService;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LandingSubsystemTest {
    private LandingService service;

    @BeforeEach
    void setUp(@TempDir Path tmp) {
        service = new LandingService(new LandingPadController(), new DeliveryConfirmation(), new LandingRepository(tmp));
    }

    @Test
    void secondBookingOnSamePadFails() {
        service.book("X1", "CAMPUS");
        assertThrows(StepFailedException.class, () -> service.book("X2", "CAMPUS"));
    }

    @Test
    void differentDestinationsHaveOwnPads() {
        service.book("X1", "CAMPUS");
        assertEquals(Landing.Status.BOOKED, service.book("X2", "MALL").getStatus());
    }

    @Test
    void confirmingGivesCodeAndFreesPad() {
        Landing l = service.book("X1", "CAMPUS");
        assertEquals("DLV-X1", service.confirm(l.getId()));
        assertEquals(Landing.Status.BOOKED, service.book("X2", "CAMPUS").getStatus());
    }

    @Test
    void cancelledLandingCannotBeConfirmed() {
        Landing l = service.book("X1", "CAMPUS");
        service.cancelByRef("X1");
        service.cancelByRef("X1");
        assertThrows(StepFailedException.class, () -> service.confirm(l.getId()));
    }

    @Test
    void blockedPadCanBeFreedAgain() {
        service.blockPad("CAMPUS");
        assertThrows(StepFailedException.class, () -> service.book("X1", "CAMPUS"));
        service.freeBlockedPads("CAMPUS");
        assertEquals(Landing.Status.BOOKED, service.book("X1", "CAMPUS").getStatus());
        assertEquals(2, service.all().size());
    }

    @Test
    void missingLandingIsReported() {
        assertThrows(StepFailedException.class, () -> service.get(7));
    }
}
