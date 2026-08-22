package com.shipovskijkorp.ic2modernadapter.resource;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/** Immutable in-memory client resource pack compiled from a user-provided original IC2 archive. */
public final class CompiledIc2ResourcePack {
    public static final String NAMESPACE = "ic2";
    private static final Pattern MODERN_RESOURCE_PATH = Pattern.compile("[a-z0-9/._-]+");

    private final Path sourceArchive;
    private final Map<String, byte[]> resources;

    CompiledIc2ResourcePack(Path sourceArchive, Map<String, byte[]> resources) {
        this.sourceArchive = sourceArchive;
        Map<String, byte[]> copy = new LinkedHashMap<>();
        resources.forEach((path, bytes) -> {
            // Defense in depth: a legacy archive may contain paths that were tolerated or simply
            // unreachable in 1.12 but cannot be represented by a modern ResourceLocation. Never
            // allow those paths to escape through PackResources even if an upstream converter
            // accidentally passes one through.
            if (isValidResourcePath(path)) {
                copy.put(path, bytes.clone());
            }
        });
        this.resources = Collections.unmodifiableMap(copy);
    }

    public Path sourceArchive() {
        return sourceArchive;
    }

    public Map<String, byte[]> resources() {
        return resources;
    }

    public Optional<byte[]> resource(String path) {
        byte[] bytes = resources.get(path);
        return bytes == null ? Optional.empty() : Optional.of(bytes.clone());
    }

    public int size() {
        return resources.size();
    }

    public static boolean isValidResourcePath(String path) {
        return path != null && !path.isEmpty() && MODERN_RESOURCE_PATH.matcher(path).matches();
    }
}
