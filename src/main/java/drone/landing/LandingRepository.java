package drone.landing;

import drone.common.FileTable;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Persistence gateway of subsystem C (data/landings.csv). */
public class LandingRepository {
    private final FileTable table;

    public LandingRepository(Path dir) {
        this.table = new FileTable(dir.resolve("landings.csv"));
    }

    public List<Landing> findAll() {
        return table.readAll().stream().map(Landing::fromRow).collect(Collectors.toList());
    }

    public Optional<Landing> findById(int id) {
        return findAll().stream().filter(l -> l.getId() == id).findFirst();
    }

    public Optional<Landing> findByRef(String ref) {
        return findAll().stream().filter(l -> l.getOrderRef().equals(ref)).findFirst();
    }

    public int nextId() {
        return findAll().stream().mapToInt(Landing::getId).max().orElse(0) + 1;
    }

    public void save(Landing landing) { table.upsert(landing.toRow()); }
}
