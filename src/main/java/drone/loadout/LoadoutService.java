package drone.loadout;

import drone.common.StepFailedException;
import java.util.List;
import java.util.Optional;

/** Entry point of subsystem B (battery + payload). Knows nothing about A or C. */
public class LoadoutService {
    private final BatteryManager battery;
    private final PayloadSensor sensor;
    private final LoadoutRepository repository;

    public LoadoutService(BatteryManager battery, PayloadSensor sensor, LoadoutRepository repository) {
        this.battery = battery;
        this.sensor = sensor;
        this.repository = repository;
    }

    public Loadout allocate(String orderRef, double distanceKm, double payloadKg) {
        sensor.assertAcceptable(payloadKg);
        double required = battery.requiredPercent(distanceKm);
        battery.assertEnough(required);
        Loadout loadout = new Loadout(repository.nextId(), orderRef, required, payloadKg);
        repository.save(loadout);
        return loadout;
    }

    public void commit(int id) {
        Loadout loadout = get(id);
        loadout.commit();
        repository.save(loadout);
    }

    /** Idempotent: releases the loadout only if it is still ALLOCATED. */
    public void releaseByRef(String orderRef) {
        repository.findByRef(orderRef).ifPresent(loadout -> {
            if (loadout.getStatus() == Loadout.Status.ALLOCATED) {
                loadout.release();
                repository.save(loadout);
            }
        });
    }

    public Loadout get(int id) {
        return repository.findById(id).orElseThrow(() -> new StepFailedException("No loadout " + id));
    }

    public Optional<Loadout> findByRef(String orderRef) { return repository.findByRef(orderRef); }

    public List<Loadout> all() { return repository.findAll(); }
}
