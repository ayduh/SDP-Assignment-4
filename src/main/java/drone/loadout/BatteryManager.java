package drone.loadout;

import drone.common.StepFailedException;

/** Pure business logic: how much battery a trip needs, and is that enough? */
public class BatteryManager {
    public static final double MAX_USABLE_PERCENT = 90;

    /** Round trip (2 x distance) at 1.5 % per km, plus 10 % safety margin. */
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
