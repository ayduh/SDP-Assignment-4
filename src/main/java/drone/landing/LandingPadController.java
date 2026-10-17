package drone.landing;

public class LandingPadController {
    public static final int PADS_PER_SITE = 1;

    public boolean hasFreePad(long activeBookingsAtSite) {
        return activeBookingsAtSite < PADS_PER_SITE;
    }
}
