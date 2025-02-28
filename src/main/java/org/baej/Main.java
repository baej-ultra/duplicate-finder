package org.baej;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        Path rootPath = Path.of("d:");

        if (!Files.isDirectory(rootPath)) {
            System.err.println("Not a directory.");
        }

        DuplicateFinder finder = new DuplicateFinder(rootPath);

        var duplicates = finder.getDuplicates();

        if (!duplicates.isEmpty()) {
            System.out.print("\nDuplicates found!\n");
            writeToFile(Path.of("./duplicates_%s.txt".formatted(
                    LocalDateTime.now().toString().replaceAll("[.:-]","_"))), duplicates);
        } else {
            System.out.println("\nDuplicates not found.");
        }
    }

    private static void writeToFile(Path location, Map<String, List<Path>> duplicates) throws IOException {
        try (BufferedWriter bw =
                     Files.newBufferedWriter(location, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            for (Map.Entry<String, List<Path>> entry : duplicates.entrySet()) {
                bw.write(entry.getKey());
                bw.newLine();
                for (Path path : entry.getValue()) {
                    bw.write(path.toAbsolutePath().toString());
                    bw.newLine();
                }
            }
        }
    }

}
