package drone.loadout;

import drone.common.StepFailedException;

public class PayloadSensor {
    public static final double MAX_KG = 5.0;

    public void assertAcceptable(double kg) {
        if (kg <= 0 || kg > MAX_KG) {
            throw new StepFailedException("Payload " + kg + " kg is outside 0 - " + MAX_KG + " kg");
        }
    }
}
