package drone.facade;

import drone.common.FileTable;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Persists the progress of every dispatch (data/dispatches.csv). */
public class DispatchLog {
    private final FileTable table;

    public DispatchLog(Path dir) {
        this.table = new FileTable(dir.resolve("dispatches.csv"));
    }

    public DispatchRecord create(String destination, double payloadKg) {
        int next = findAll().stream().mapToInt(DispatchRecord::getId).max().orElse(0) + 1;
        DispatchRecord record = new DispatchRecord(next, destination, payloadKg);
        save(record);
        return record;
    }

    public void save(DispatchRecord record) { table.upsert(record.toRow()); }

    public List<DispatchRecord> findAll() {
        return table.readAll().stream().map(DispatchRecord::fromRow).collect(Collectors.toList());
    }

    public Optional<DispatchRecord> findById(int id) {
        return findAll().stream().filter(r -> r.getId() == id).findFirst();
    }
}
