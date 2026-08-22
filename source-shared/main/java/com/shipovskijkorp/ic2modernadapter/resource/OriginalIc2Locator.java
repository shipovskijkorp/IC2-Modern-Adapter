package com.shipovskijkorp.ic2modernadapter.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/** Locates the separately supplied original IC2 archive outside the mods directory. */
public final class OriginalIc2Locator {
    public static final String ORIGINAL_DIRECTORY = "ic2-original";

    public static Path originalDirectory(Path gameDirectory) {
        return gameDirectory.resolve(ORIGINAL_DIRECTORY);
    }

    public static Path locate(Path gameDirectory) throws IOException {
        Path directory = originalDirectory(gameDirectory);
        Files.createDirectories(directory);

        List<Path> candidates;
        try (Stream<Path> files = Files.list(directory)) {
            candidates = files
                    .filter(Files::isRegularFile)
                    .filter(OriginalIc2Locator::isArchive)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }

        for (Path candidate : candidates) {
            try (OriginalIc2Archive ignored = OriginalIc2Archive.open(candidate)) {
                return candidate;
            } catch (IOException ignored) {
                // Not a supported IC2 resource archive; continue looking.
            }
        }

        throw new IOException(
                "No supported original IndustrialCraft 2 archive was found in " + directory
                        + ". Place industrialcraft-2-2.8.222-ex112.jar there (not in mods/)."
        );
    }

    private static boolean isArchive(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".jar") || name.endsWith(".zip");
    }

    private OriginalIc2Locator() {
    }
}
