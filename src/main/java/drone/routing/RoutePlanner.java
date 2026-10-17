package drone.routing;

import drone.common.StepFailedException;
import java.util.Map;

/** Pure business logic: distance from the hub (0,0) to a known destination, in km. */
public class RoutePlanner {
    private static final Map<String, double[]> PLACES = Map.of(
            "CAMPUS", new double[]{3, 4},
            "MALL", new double[]{6, 8},
            "HOSPITAL", new double[]{0, 12},
            "AIRPORT", new double[]{40, 30});

    public double distanceKm(String destination) {
        double[] p = PLACES.get(destination);
        if (p == null) {
            throw new StepFailedException("Unknown destination: " + destination);
        }
        return Math.hypot(p[0], p[1]);
    }
}
