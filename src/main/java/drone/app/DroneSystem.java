package drone.app;

import drone.facade.AdminFacade;
import drone.facade.ChainCompensator;
import drone.facade.DispatchFacade;
import drone.facade.DispatchLog;
import drone.landing.DeliveryConfirmation;
import drone.landing.LandingPadController;
import drone.landing.LandingRepository;
import drone.landing.LandingService;
import drone.loadout.BatteryManager;
import drone.loadout.LoadoutRepository;
import drone.loadout.LoadoutService;
import drone.loadout.PayloadSensor;
import drone.routing.RoutePlanner;
import drone.routing.RouteRepository;
import drone.routing.RouteService;
import drone.routing.WeatherChecker;
import java.nio.file.Path;

public class DroneSystem {
    private final RouteService routes;
    private final LoadoutService loadouts;
    private final LandingService landings;
    private final DispatchFacade dispatchFacade;
    private final AdminFacade adminFacade;

    public DroneSystem(Path dataDir) {
        routes = new RouteService(new RoutePlanner(), new WeatherChecker(), new RouteRepository(dataDir));
        loadouts = new LoadoutService(new BatteryManager(), new PayloadSensor(), new LoadoutRepository(dataDir));
        landings = new LandingService(new LandingPadController(), new DeliveryConfirmation(), new LandingRepository(dataDir));
        DispatchLog log = new DispatchLog(dataDir);
        ChainCompensator compensator = new ChainCompensator(routes, loadouts, landings, log);
        dispatchFacade = new DispatchFacade(routes, loadouts, landings, log, compensator);
        adminFacade = new AdminFacade(routes, loadouts, landings, log, compensator);
    }

    public DispatchFacade dispatch() { return dispatchFacade; }

    public AdminFacade admin() { return adminFacade; }

    public RouteService routes() { return routes; }

    public LoadoutService loadouts() { return loadouts; }

    public LandingService landings() { return landings; }
}
