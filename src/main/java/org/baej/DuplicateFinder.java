package org.baej;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class DuplicateFinder extends SimpleFileVisitor<Path> {

    Set<Path> paths = new HashSet<>();

    public DuplicateFinder(Path startDir) {
        try {
            Files.walkFileTree(startDir, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a map where the key is a checksum and the value is a list of paths.
     *
     * @return a {@code Map} with the checksum as the {@code key} and a list of Path objects as the {@code value}
     */
    public Map<String, List<Path>> getDuplicates() {
        // Get candidates based on file size
        List<List<Path>> candidates = paths.stream()
                .collect(Collectors.groupingBy(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .values()
                .stream()
                .filter(entry -> entry.size() >= 2)
                .toList();

        // Get checksums of all candidates and filter to find duplicates
        Map<String, List<Path>> duplicates = candidates.stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(path -> {
                    try {
                        return getChecksum(path);
                    } catch (NoSuchAlgorithmException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() >= 2)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return duplicates;
    }

    private String getChecksum(Path path) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        ByteBuffer buffer = ByteBuffer.allocate(4096);

        // Read file and update the message digest
        try (FileChannel fc = FileChannel.open(path)) {
            while (fc.read(buffer) != -1) {
                buffer.flip();
                md.update(buffer);
                buffer.clear();
            }
        }

        // Convert byte array to hex string
        var digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        paths.add(file);

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.out.println("Could not access " + file);

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        System.out.println("Checking directory... " + dir.toAbsolutePath());

        return FileVisitResult.CONTINUE;
    }
}
