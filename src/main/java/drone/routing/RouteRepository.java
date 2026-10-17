package drone.routing;

import drone.common.FileTable;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RouteRepository {
    private final FileTable table;

    public RouteRepository(Path dir) {
        this.table = new FileTable(dir.resolve("routes.csv"));
    }

    public List<Route> findAll() {
        return table.readAll().stream().map(Route::fromRow).collect(Collectors.toList());
    }

    public Optional<Route> findById(int id) {
        return findAll().stream().filter(r -> r.getId() == id).findFirst();
    }

    public Optional<Route> findByRef(String ref) {
        return findAll().stream().filter(r -> r.getOrderRef().equals(ref)).findFirst();
    }

    public int nextId() {
        return findAll().stream().mapToInt(Route::getId).max().orElse(0) + 1;
    }

    public void save(Route route) { table.upsert(route.toRow()); }
}
