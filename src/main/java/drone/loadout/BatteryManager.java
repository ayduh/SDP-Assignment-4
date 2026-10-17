package drone.loadout;

import drone.common.StepFailedException;

public class BatteryManager {
    public static final double MAX_USABLE_PERCENT = 90;

    public double requiredPercent(double distanceKm) {
        return 10 + 3 * distanceKm;
    }

    public void assertEnough(double requiredPercent) {
        if (requiredPercent > MAX_USABLE_PERCENT) {
            throw new StepFailedException("Trip needs " + requiredPercent + " % battery, only "
                    + MAX_USABLE_PERCENT + " % is usable");
        }
    }
}
