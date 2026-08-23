package com.shipovskijkorp.ic2modernadapter.development;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Central list of item identities that are still considered unfinished.
 *
 * <p>The list deliberately uses full registry IDs rather than Java classes. Legacy roots such as
 * {@code ic2:te} normally mark all of their NBT/meta variants at once, while
 * {@code completedVariants} can exempt individual finished subtypes without incorrectly marking
 * every sibling as production-ready.</p>
 */
public final class InDevContent {
    public static final String RESOURCE = "/ic2ma/development/in-dev-content.json";

    private static final Gson GSON = new GsonBuilder().create();
    private static final InDevManifest MANIFEST = load();
    private static final Set<String> ITEM_IDS = Set.copyOf(MANIFEST.items);
    private static final Set<String> COMPLETED_VARIANTS = Set.copyOf(MANIFEST.completedVariants);

    public static boolean isItem(String namespace, String path) {
        return ITEM_IDS.contains(namespace + ":" + path);
    }

    /**
     * Variant-aware development state for legacy root items such as {@code ic2:te}.
     * A completed subtype may leave development while unfinished siblings keep the root marker.
     */
    public static boolean isItem(String namespace, String path, String variantKey) {
        return isItem(namespace, path)
                && (variantKey == null || !COMPLETED_VARIANTS.contains(variantKey));
    }

    public static boolean isItem(String registryId) {
        return ITEM_IDS.contains(registryId);
    }

    public static Set<String> items() {
        return ITEM_IDS;
    }

    public static Set<String> completedVariants() {
        return COMPLETED_VARIANTS;
    }

    private static InDevManifest load() {
        try (InputStream stream = InDevContent.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Missing bundled in-dev content list: " + RESOURCE);
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                InDevManifest manifest = GSON.fromJson(reader, InDevManifest.class);
                validate(manifest);
                return manifest;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read bundled in-dev content list", e);
        }
    }

    private static void validate(InDevManifest manifest) {
        Objects.requireNonNull(manifest, "in-dev manifest");
        Objects.requireNonNull(manifest.items, "in-dev items");
        if (manifest.completedVariants == null) {
            manifest.completedVariants = List.of();
        }

        Set<String> unique = new LinkedHashSet<>();
        for (String id : manifest.items) {
            if (id == null || id.isBlank()) {
                throw new IllegalStateException("Blank item ID in " + RESOURCE);
            }
            int separator = id.indexOf(':');
            if (separator <= 0 || separator == id.length() - 1 || separator != id.lastIndexOf(':')) {
                throw new IllegalStateException("Invalid item registry ID in " + RESOURCE + ": " + id);
            }
            if (!id.equals(id.toLowerCase())) {
                throw new IllegalStateException("In-dev item registry ID must be lowercase: " + id);
            }
            if (!unique.add(id)) {
                throw new IllegalStateException("Duplicate in-dev item registry ID: " + id);
            }
        }

        OriginalContentManifest content = OriginalContentManifest.get();
        Set<String> registered = new LinkedHashSet<>();
        for (String path : content.registries().items()) {
            registered.add(content.namespace() + ":" + path);
        }
        Set<String> unknown = new LinkedHashSet<>(unique);
        unknown.removeAll(registered);
        if (!unknown.isEmpty()) {
            throw new IllegalStateException("In-dev list references unregistered IC2 items: " + unknown);
        }

        Set<String> variantKeys = new LinkedHashSet<>();
        for (OriginalContentManifest.StackVariant variant : content.stackVariants()) {
            variantKeys.add(variant.key());
        }
        Set<String> completed = new LinkedHashSet<>();
        for (String variantKey : manifest.completedVariants) {
            if (variantKey == null || variantKey.isBlank()) {
                throw new IllegalStateException("Blank completed variant in " + RESOURCE);
            }
            if (!completed.add(variantKey)) {
                throw new IllegalStateException("Duplicate completed variant: " + variantKey);
            }
            if (!variantKeys.contains(variantKey)) {
                throw new IllegalStateException("Completed variant is not registered by IC2MA: " + variantKey);
            }
            OriginalContentManifest.StackVariant variant = content.stackVariant(variantKey);
            String rootId = content.namespace() + ":" + variant.item();
            if (!unique.contains(rootId)) {
                throw new IllegalStateException(
                        "Completed variant " + variantKey + " belongs to non-development root item " + rootId);
            }
        }
    }

    private static final class InDevManifest {
        private List<String> items;
        private List<String> completedVariants;
    }

    private InDevContent() {
    }
}
