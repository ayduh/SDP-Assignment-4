package drone.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Tiny CSV-like file ("a;b;c" per line). Every read/write goes to disk. */
public class FileTable {
    private final Path file;

    public FileTable(Path file) {
        this.file = file.toAbsolutePath();
        try {
            Files.createDirectories(this.file.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<String[]> readAll() {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(file)) {
            return rows;
        }
        try {
            for (String line : Files.readAllLines(file)) {
                if (!line.isBlank()) {
                    rows.add(line.split(";", -1));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return rows;
    }

    /** Insert the row, or replace the existing row with the same first column (the id). */
    public void upsert(String[] row) {
        List<String[]> rows = readAll();
        boolean replaced = false;
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i)[0].equals(row[0])) {
                rows.set(i, row);
                replaced = true;
            }
        }
        if (!replaced) {
            rows.add(row);
        }
        List<String> lines = new ArrayList<>();
        for (String[] r : rows) {
            lines.add(String.join(";", r));
        }
        try {
            Files.write(file, lines);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
