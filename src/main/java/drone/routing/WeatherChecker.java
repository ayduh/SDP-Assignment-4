package drone.routing;

/** Pure business logic: is the wind low enough to fly? */
public class WeatherChecker {
    public static final double MAX_WIND_KMH = 40;
    private double windKmh = 10;

    public void setWindKmh(double windKmh) { this.windKmh = windKmh; }

    public double getWindKmh() { return windKmh; }

    public boolean isFlyable() { return windKmh <= MAX_WIND_KMH; }
}
