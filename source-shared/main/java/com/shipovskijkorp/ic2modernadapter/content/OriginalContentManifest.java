package com.shipovskijkorp.ic2modernadapter.content;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Canonical, executable-code-free description of the static content surface exposed by
 * IndustrialCraft 2 Experimental 2.8.222-ex112.
 *
 * <p>The manifest is generated from the reference IC2 build during development and bundled with
 * IC2 Modern Adapter. Runtime registration never depends on loading, linking, reflecting over, or
 * executing classes from the original IC2 jar.</p>
 */
public final class OriginalContentManifest {
    public static final String RESOURCE = "/ic2ma/reference/ic2-2.8.222-ex112-content.json";
    public static final int EXPECTED_BLOCKS = 33;
    public static final int EXPECTED_ITEMS = 169;
    public static final int EXPECTED_BLOCK_ITEMS = 32;
    public static final int EXPECTED_FLUIDS = 18;
    public static final int EXPECTED_MOB_EFFECTS = 1;
    public static final int EXPECTED_ENTITIES = 10;
    public static final int EXPECTED_BLOCK_ENTITIES = 113;
    public static final int EXPECTED_STACK_VARIANTS = 410;

    private static final Gson GSON = new GsonBuilder().create();
    private static final OriginalContentManifest INSTANCE = load();

    private String sourceVersion;
    private String namespace;
    private RegistryContent registries;
    private LegacyContent legacy;
    private List<StackVariant> stackVariants;
    private transient Map<String, StackVariant> stackVariantsByKey;
    private transient Map<String, List<StackVariant>> stackVariantsByItem;
    private transient Map<String, Integer> stackVariantIndexByKey;

    public static OriginalContentManifest get() {
        return INSTANCE;
    }

    public String sourceVersion() {
        return sourceVersion;
    }

    public String namespace() {
        return namespace;
    }

    public RegistryContent registries() {
        return registries;
    }

    public LegacyContent legacy() {
        return legacy;
    }

    public List<StackVariant> stackVariants() {
        return stackVariants;
    }

    public StackVariant stackVariant(String key) {
        StackVariant variant = stackVariantsByKey.get(key);
        if (variant == null) {
            throw new IllegalArgumentException("Unknown IC2 stack variant: " + key);
        }
        return variant;
    }

    /** Returns all finite legacy identities carried by the given root item. */
    public List<StackVariant> stackVariants(String itemPath) {
        return stackVariantsByItem.getOrDefault(itemPath, List.of());
    }

    /** Zero-based finite subtype index within its root item. */
    public int stackVariantIndex(String variantKey) {
        Integer index = stackVariantIndexByKey.get(variantKey);
        if (index == null) {
            throw new IllegalArgumentException("Unknown IC2 stack variant: " + variantKey);
        }
        return index;
    }

    /** Stable positive CustomModelData value used by generated legacy item-model overrides. */
    public int customModelData(String variantKey) {
        return stackVariantIndex(variantKey) + 1;
    }

    private static OriginalContentManifest load() {
        try (InputStream stream = OriginalContentManifest.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Missing bundled IC2 content manifest: " + RESOURCE);
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                OriginalContentManifest manifest = GSON.fromJson(reader, OriginalContentManifest.class);
                manifest.validate();
                return manifest;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read bundled IC2 content manifest", e);
        }
    }

    private void validate() {
        Objects.requireNonNull(sourceVersion, "sourceVersion");
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(registries, "registries");
        Objects.requireNonNull(legacy, "legacy");
        Objects.requireNonNull(stackVariants, "stackVariants");

        if (!"2.8.222-ex112".equals(sourceVersion)) {
            throw new IllegalStateException("Unexpected reference IC2 version: " + sourceVersion);
        }
        if (!"ic2".equals(namespace)) {
            throw new IllegalStateException("Unexpected reference namespace: " + namespace);
        }

        registries.validate();
        legacy.validate();
        requireSize("blocks", registries.blocks, EXPECTED_BLOCKS);
        requireSize("items", registries.items, EXPECTED_ITEMS);
        requireSize("blockItems", registries.blockItems, EXPECTED_BLOCK_ITEMS);
        requireSize("fluids", registries.fluids, EXPECTED_FLUIDS);
        requireSize("mobEffects", registries.mobEffects, EXPECTED_MOB_EFFECTS);
        requireSize("entities", registries.entities, EXPECTED_ENTITIES);
        requireSize("blockEntities", registries.blockEntities, EXPECTED_BLOCK_ENTITIES);

        Set<String> itemIds = new HashSet<>(registries.items);
        Set<String> blockIds = new HashSet<>(registries.blocks);
        for (String path : registries.blockItems) {
            if (!itemIds.contains(path) || !blockIds.contains(path)) {
                throw new IllegalStateException("Block item is missing its block/item registry identity: " + path);
            }
        }

        if (stackVariants.size() != EXPECTED_STACK_VARIANTS) {
            throw new IllegalStateException(
                    "Unexpected stack variant count: " + stackVariants.size()
                            + " (expected " + EXPECTED_STACK_VARIANTS + ")");
        }

        Map<String, StackVariant> byKey = new LinkedHashMap<>();
        Map<String, List<StackVariant>> byItem = new LinkedHashMap<>();
        for (StackVariant variant : stackVariants) {
            variant.validate(itemIds);
            StackVariant old = byKey.put(variant.key, variant);
            if (old != null) {
                throw new IllegalStateException("Duplicate IC2 stack variant key: " + variant.key);
            }
            byItem.computeIfAbsent(variant.item, ignored -> new java.util.ArrayList<>()).add(variant);
        }
        stackVariantsByKey = Map.copyOf(byKey);
        Map<String, List<StackVariant>> immutableByItem = new LinkedHashMap<>();
        Map<String, Integer> byVariantIndex = new LinkedHashMap<>();
        byItem.forEach((item, variants) -> {
            immutableByItem.put(item, List.copyOf(variants));
            for (int index = 0; index < variants.size(); index++) {
                byVariantIndex.put(variants.get(index).key, index);
            }
        });
        stackVariantsByItem = Map.copyOf(immutableByItem);
        stackVariantIndexByKey = Map.copyOf(byVariantIndex);
    }

    private static void requireSize(String name, List<?> list, int expected) {
        if (list.size() != expected) {
            throw new IllegalStateException(
                    "Unexpected " + name + " count: " + list.size() + " (expected " + expected + ")");
        }
    }

    private static void validatePaths(String name, List<String> paths) {
        Set<String> unique = new HashSet<>();
        for (String path : paths) {
            if (path == null || path.isBlank() || path.indexOf(':') >= 0 || !path.equals(path.toLowerCase())) {
                throw new IllegalStateException("Invalid " + name + " registry path: " + path);
            }
            if (!unique.add(path)) {
                throw new IllegalStateException("Duplicate " + name + " registry path: " + path);
            }
        }
    }

    public static final class RegistryContent {
        private List<String> blocks;
        private List<String> items;
        private List<String> blockItems;
        private List<FluidIdentity> fluids;
        private List<String> mobEffects;
        private List<String> entities;
        private List<String> blockEntities;

        public List<String> blocks() {
            return blocks;
        }

        public List<String> items() {
            return items;
        }

        public List<String> blockItems() {
            return blockItems;
        }

        public List<FluidIdentity> fluids() {
            return fluids;
        }

        public List<String> fluidPaths() {
            return fluids.stream().map(FluidIdentity::path).toList();
        }

        public List<String> mobEffects() {
            return mobEffects;
        }

        public List<String> entities() {
            return entities;
        }

        public List<String> blockEntities() {
            return blockEntities;
        }

        private void validate() {
            Objects.requireNonNull(blocks, "registries.blocks");
            Objects.requireNonNull(items, "registries.items");
            Objects.requireNonNull(blockItems, "registries.blockItems");
            Objects.requireNonNull(fluids, "registries.fluids");
            Objects.requireNonNull(mobEffects, "registries.mobEffects");
            Objects.requireNonNull(entities, "registries.entities");
            Objects.requireNonNull(blockEntities, "registries.blockEntities");
            validatePaths("block", blocks);
            validatePaths("item", items);
            validatePaths("block item", blockItems);
            validatePaths("fluid", fluidPaths());
            validatePaths("mob effect", mobEffects);
            validatePaths("entity", entities);
            validatePaths("block entity", blockEntities);
            for (FluidIdentity fluid : fluids) {
                fluid.validate();
            }
        }
    }

    public static final class FluidIdentity {
        private String path;
        private String legacyName;

        public String path() {
            return path;
        }

        public String legacyName() {
            return legacyName;
        }

        private void validate() {
            Objects.requireNonNull(path, "fluid.path");
            Objects.requireNonNull(legacyName, "fluid.legacyName");
            if (!legacyName.equals("ic2" + path)) {
                throw new IllegalStateException(
                        "Unexpected legacy Forge fluid name for " + path + ": " + legacyName);
            }
        }
    }

    public static final class LegacyContent {
        private List<String> declaredButUnregisteredItems;
        private List<String> tileEntityMigrationAliases;
        private List<String> devOnlyEntities;

        public List<String> declaredButUnregisteredItems() {
            return declaredButUnregisteredItems;
        }

        public List<String> tileEntityMigrationAliases() {
            return tileEntityMigrationAliases;
        }

        public List<String> devOnlyEntities() {
            return devOnlyEntities;
        }

        private void validate() {
            Objects.requireNonNull(declaredButUnregisteredItems, "legacy.declaredButUnregisteredItems");
            Objects.requireNonNull(tileEntityMigrationAliases, "legacy.tileEntityMigrationAliases");
            Objects.requireNonNull(devOnlyEntities, "legacy.devOnlyEntities");
            validatePaths("declared-but-unregistered item", declaredButUnregisteredItems);
            validatePaths("development-only entity", devOnlyEntities);
            Set<String> aliases = new HashSet<>();
            for (String alias : tileEntityMigrationAliases) {
                if (alias == null || alias.isBlank() || !aliases.add(alias)) {
                    throw new IllegalStateException("Invalid/duplicate legacy tile entity alias: " + alias);
                }
            }
        }
    }

    public static final class StackVariant {
        private String key;
        private String item;
        private int legacyMeta = -1;
        private boolean creativeVisible = true;
        private List<NbtEntry> nbt;

        public String key() {
            return key;
        }

        public String item() {
            return item;
        }

        public int legacyMeta() {
            return legacyMeta;
        }

        public boolean creativeVisible() {
            return creativeVisible;
        }

        public List<NbtEntry> nbt() {
            return nbt;
        }

        private void validate(Set<String> itemIds) {
            Objects.requireNonNull(key, "variant.key");
            Objects.requireNonNull(item, "variant.item");
            Objects.requireNonNull(nbt, "variant.nbt");
            if (!itemIds.contains(item)) {
                throw new IllegalStateException("Stack variant references unregistered item " + item + ": " + key);
            }
            if (!key.startsWith(item + "/")) {
                throw new IllegalStateException("Stack variant key/item mismatch: " + key + " -> " + item);
            }
            for (NbtEntry entry : nbt) {
                entry.validate();
            }
        }
    }

    public static final class NbtEntry {
        private String path;
        private String type;
        private String value;

        public String path() {
            return path;
        }

        public String type() {
            return type;
        }

        public String value() {
            return value;
        }

        private void validate() {
            Objects.requireNonNull(path, "nbt.path");
            Objects.requireNonNull(type, "nbt.type");
            Objects.requireNonNull(value, "nbt.value");
            if (path.isBlank()) {
                throw new IllegalStateException("Empty NBT path in content manifest");
            }
            if (!Set.of("byte", "int", "string").contains(type)) {
                throw new IllegalStateException("Unsupported reference NBT type: " + type);
            }
        }
    }
}
