package drone.routing;

import drone.common.StepFailedException;
import java.util.List;
import java.util.Optional;

public class RouteService {
    private final RoutePlanner planner;
    private final WeatherChecker weather;
    private final RouteRepository repository;

    public RouteService(RoutePlanner planner, WeatherChecker weather, RouteRepository repository) {
        this.planner = planner;
        this.weather = weather;
        this.repository = repository;
    }

    public Route reserve(String orderRef, String destination) {
        if (!weather.isFlyable()) {
            throw new StepFailedException("Wind " + weather.getWindKmh() + " km/h is above the "
                    + WeatherChecker.MAX_WIND_KMH + " km/h limit");
        }
        Route route = new Route(repository.nextId(), orderRef, destination, planner.distanceKm(destination));
        repository.save(route);
        return route;
    }

    public void close(int id) {
        Route route = get(id);
        route.close();
        repository.save(route);
    }

    public void releaseByRef(String orderRef) {
        repository.findByRef(orderRef).ifPresent(route -> {
            if (route.getStatus() == Route.Status.RESERVED) {
                route.release();
                repository.save(route);
            }
        });
    }

    public Route get(int id) {
        return repository.findById(id).orElseThrow(() -> new StepFailedException("No route " + id));
    }

    public Optional<Route> findByRef(String orderRef) { return repository.findByRef(orderRef); }

    public List<Route> all() { return repository.findAll(); }

    public void setWind(double kmh) { weather.setWindKmh(kmh); }
}
