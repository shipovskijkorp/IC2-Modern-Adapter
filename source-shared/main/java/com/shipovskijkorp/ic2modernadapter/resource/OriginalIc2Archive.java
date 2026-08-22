package com.shipovskijkorp.ic2modernadapter.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Read-only view of an original IC2 jar/zip. Class files are never inspected or loaded.
 */
public final class OriginalIc2Archive implements AutoCloseable {
    private static final String ASSET_MARKER = "assets/ic2/blockstates/te.json";
    private static final String ASSET_ROOT = "assets/ic2/";

    private final Path path;
    private final ZipFile zip;
    private final String prefix;

    private OriginalIc2Archive(Path path, ZipFile zip, String prefix) {
        this.path = path;
        this.zip = zip;
        this.prefix = prefix;
    }

    public static OriginalIc2Archive open(Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        ZipFile zip = new ZipFile(path.toFile());
        try {
            String marker = zip.stream()
                    .map(ZipEntry::getName)
                    .filter(name -> name.endsWith(ASSET_MARKER))
                    .min(Comparator.comparingInt(String::length))
                    .orElseThrow(() -> new IOException(
                            "Archive does not contain the expected IC2 client resources: " + path));
            String prefix = marker.substring(0, marker.length() - ASSET_MARKER.length());
            return new OriginalIc2Archive(path, zip, prefix);
        } catch (Throwable failure) {
            try {
                zip.close();
            } catch (IOException ignored) {
            }
            throw failure;
        }
    }

    public Path path() {
        return path;
    }

    public boolean hasAsset(String relativePath) {
        return zip.getEntry(entryName(relativePath)) != null;
    }

    public byte[] readAsset(String relativePath) throws IOException {
        ZipEntry entry = zip.getEntry(entryName(relativePath));
        if (entry == null || entry.isDirectory()) {
            throw new IOException("Missing IC2 asset '" + relativePath + "' in " + path);
        }
        try (InputStream stream = zip.getInputStream(entry)) {
            return stream.readAllBytes();
        }
    }

    public List<String> listAssets() {
        String root = prefix + ASSET_ROOT;
        List<String> result = new ArrayList<>();
        zip.stream()
                .filter(entry -> !entry.isDirectory())
                .map(ZipEntry::getName)
                .filter(name -> name.startsWith(root))
                .map(name -> name.substring(root.length()))
                .sorted()
                .forEach(result::add);
        return List.copyOf(result);
    }

    private String entryName(String relativePath) {
        if (relativePath.startsWith("/") || relativePath.contains("..")) {
            throw new IllegalArgumentException("Unsafe IC2 asset path: " + relativePath);
        }
        return prefix + ASSET_ROOT + relativePath;
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }
}
