package com.shipovskijkorp.ic2modernadapter.resource;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/** Immutable in-memory client + server-data pack compiled from a user-provided original IC2 archive. */
public final class CompiledIc2ResourcePack {
    public static final String NAMESPACE = "ic2";
    private static final Pattern MODERN_RESOURCE_PATH = Pattern.compile("[a-z0-9/._-]+");

    private final Path sourceArchive;
    private final Map<String, byte[]> clientResources;
    private final Map<String, byte[]> serverDataResources;

    CompiledIc2ResourcePack(
            Path sourceArchive,
            Map<String, byte[]> clientResources,
            Map<String, byte[]> serverDataResources) {
        this.sourceArchive = sourceArchive;
        this.clientResources = immutableValidatedCopy(clientResources);
        this.serverDataResources = immutableValidatedCopy(serverDataResources);
    }

    /** Backward-compatible constructor used by focused unit tests. */
    CompiledIc2ResourcePack(Path sourceArchive, Map<String, byte[]> clientResources) {
        this(sourceArchive, clientResources, Map.of());
    }

    private static Map<String, byte[]> immutableValidatedCopy(Map<String, byte[]> source) {
        Map<String, byte[]> copy = new LinkedHashMap<>();
        source.forEach((path, bytes) -> {
            // Defense in depth: a legacy archive may contain paths tolerated by 1.12 but invalid
            // as modern ResourceLocations. Never publish such paths through PackResources.
            if (isValidResourcePath(path)) {
                copy.put(path, bytes.clone());
            }
        });
        return Collections.unmodifiableMap(copy);
    }

    public Path sourceArchive() {
        return sourceArchive;
    }

    /** Client-side assets under assets/ic2. Kept as resources() for existing callers/tests. */
    public Map<String, byte[]> resources() {
        return clientResources;
    }

    public Map<String, byte[]> clientResources() {
        return clientResources;
    }

    /** Server datapack resources under data/ic2. */
    public Map<String, byte[]> serverDataResources() {
        return serverDataResources;
    }

    public Optional<byte[]> resource(String path) {
        return clientResource(path);
    }

    public Optional<byte[]> clientResource(String path) {
        byte[] bytes = clientResources.get(path);
        return bytes == null ? Optional.empty() : Optional.of(bytes.clone());
    }

    public Optional<byte[]> serverDataResource(String path) {
        byte[] bytes = serverDataResources.get(path);
        return bytes == null ? Optional.empty() : Optional.of(bytes.clone());
    }

    public int size() {
        return clientResources.size() + serverDataResources.size();
    }

    public int clientSize() {
        return clientResources.size();
    }

    public int serverDataSize() {
        return serverDataResources.size();
    }

    public static boolean isValidResourcePath(String path) {
        return path != null && !path.isEmpty() && MODERN_RESOURCE_PATH.matcher(path).matches();
    }
}
