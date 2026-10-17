package drone.landing;

import drone.common.StepFailedException;
import java.util.List;
import java.util.Optional;

/** Entry point of subsystem C (landing pad + confirmation). Knows nothing about A or B. */
public class LandingService {
    static final String EXTERNAL_PREFIX = "EXT-";

    private final LandingPadController pads;
    private final DeliveryConfirmation confirmation;
    private final LandingRepository repository;

    public LandingService(LandingPadController pads, DeliveryConfirmation confirmation, LandingRepository repository) {
        this.pads = pads;
        this.confirmation = confirmation;
        this.repository = repository;
    }

    public Landing book(String orderRef, String destination) {
        long busy = repository.findAll().stream()
                .filter(l -> l.getDestination().equals(destination) && l.getStatus() == Landing.Status.BOOKED)
                .count();
        if (!pads.hasFreePad(busy)) {
            throw new StepFailedException("No free landing pad at " + destination);
        }
        Landing landing = new Landing(repository.nextId(), orderRef, destination);
        repository.save(landing);
        return landing;
    }

    /** Confirms the delivery and returns the confirmation code. */
    public String confirm(int id) {
        Landing landing = get(id);
        landing.confirm(confirmation.codeFor(landing.getOrderRef()));
        repository.save(landing);
        return landing.getConfirmationCode();
    }

    /** Idempotent: cancels the booking only if it is still BOOKED. */
    public void cancelByRef(String orderRef) {
        repository.findByRef(orderRef).ifPresent(landing -> {
            if (landing.getStatus() == Landing.Status.BOOKED) {
                landing.cancel();
                repository.save(landing);
            }
        });
    }

    /** Simulates another operator's drone sitting on the pad. */
    public void blockPad(String destination) {
        book(EXTERNAL_PREFIX + repository.nextId(), destination);
    }

    public void freeBlockedPads(String destination) {
        for (Landing l : repository.findAll()) {
            if (l.getOrderRef().startsWith(EXTERNAL_PREFIX) && l.getDestination().equals(destination)) {
                cancelByRef(l.getOrderRef());
            }
        }
    }

    public Landing get(int id) {
        return repository.findById(id).orElseThrow(() -> new StepFailedException("No landing " + id));
    }

    public Optional<Landing> findByRef(String orderRef) { return repository.findByRef(orderRef); }

    public List<Landing> all() { return repository.findAll(); }
}
