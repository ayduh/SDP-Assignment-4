package drone.loadout;

import drone.common.FileTable;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LoadoutRepository {
    private final FileTable table;

    public LoadoutRepository(Path dir) {
        this.table = new FileTable(dir.resolve("loadouts.csv"));
    }

    public List<Loadout> findAll() {
        return table.readAll().stream().map(Loadout::fromRow).collect(Collectors.toList());
    }

    public Optional<Loadout> findById(int id) {
        return findAll().stream().filter(l -> l.getId() == id).findFirst();
    }

    public Optional<Loadout> findByRef(String ref) {
        return findAll().stream().filter(l -> l.getOrderRef().equals(ref)).findFirst();
    }

    public int nextId() {
        return findAll().stream().mapToInt(Loadout::getId).max().orElse(0) + 1;
    }

    public void save(Loadout loadout) { table.upsert(loadout.toRow()); }
}
