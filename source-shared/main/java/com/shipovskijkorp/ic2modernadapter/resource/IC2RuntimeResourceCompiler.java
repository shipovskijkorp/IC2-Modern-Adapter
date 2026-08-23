package com.shipovskijkorp.ic2modernadapter.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import com.shipovskijkorp.ic2modernadapter.content.OriginalItemModels;
import com.shipovskijkorp.ic2modernadapter.content.OriginalTranslationKeys;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantBlock;
import com.shipovskijkorp.ic2modernadapter.energy.cable.EuCableVariant;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * Compiles the original IC2 1.12 resource tree into a modern in-memory client pack.
 *
 * <p>Original textures, model JSON, sound files and other already-compatible client assets are
 * copied byte-for-byte into memory. Legacy Forge-marker blockstates are replaced with ordinary
 * modern blockstates generated from the reference content manifest and the original model mapping.
 * Nothing is written to disk and no original IC2 class is loaded.</p>
 */
public final class IC2RuntimeResourceCompiler {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final OriginalContentManifest MANIFEST = OriginalContentManifest.get();
    private static final List<String> LEGACY_BLOCKSTATE_NAMES = List.of(
            "te", "resource", "leaves", "rubber_wood", "sapling", "scaffold", "foam",
            "fence", "sheet", "glass", "wall", "mining_pipe", "reinforced_door",
            "dynamite", "refractory_bricks", "fluid");

    public static CompiledIc2ResourcePack compile(Path archivePath) throws IOException {
        try (OriginalIc2Archive archive = OriginalIc2Archive.open(archivePath)) {
            return compile(archive);
        }
    }

    static CompiledIc2ResourcePack compile(OriginalIc2Archive archive) throws IOException {
        Map<String, byte[]> resources = new LinkedHashMap<>();

        // Preserve every already-compatible client resource from the user's original archive.
        // Old Forge-marker blockstates and lang_ic2 property files are deliberately excluded; they
        // require explicit conversion before modern Minecraft can consume them.
        for (String asset : archive.listAssets()) {
            if (asset.startsWith("blockstates/") || asset.startsWith("lang_ic2/")) {
                continue;
            }

            // IC2 1.12 contains a few files that were never valid modern ResourceLocations,
            // including a stray "personal_chest (copy).json" and BuildCraft-trigger assets under
            // the mixed-case "bcTrigger" directory. Old resource loading could leave those
            // unreachable files alone; modern PackResources enumerates every exposed path and
            // rejects them before model baking. They are not part of the canonical IC2 content
            // surface, so do not publish them into the runtime pack.
            if (!CompiledIc2ResourcePack.isValidResourcePath(asset)) {
                continue;
            }
            resources.put(asset, archive.readAsset(asset));
        }

        normalizeLegacyTextureLayout(resources);
        normalizeLegacyModelTextures(resources);
        compileLanguages(archive, resources);

        Map<String, JsonObject> legacyBlockstates = loadLegacyBlockstates(archive);
        compileBlockstates(resources, legacyBlockstates);
        compileCableBlockstate(resources);
        compileBlockItemModels(resources, legacyBlockstates);
        compileStandaloneItemModels(resources);
        assignCutoutRenderTypes(resources);
        validateOutput(resources);
        return new CompiledIc2ResourcePack(archive.path(), resources);
    }

    /**
     * Moves the old 1.12 texture directories onto the modern item/block atlas layout. Modern
     * Minecraft's atlases discover textures under textures/item and textures/block; IC2 1.12 used
     * the plural textures/items and textures/blocks paths. Keeping the old paths byte-for-byte is
     * therefore not enough even though the PNG files themselves are valid.
     */
    private static void normalizeLegacyTextureLayout(Map<String, byte[]> resources) {
        Map<String, byte[]> moved = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : List.copyOf(resources.entrySet())) {
            String normalized = normalizeLegacyTexturePath(entry.getKey());
            if (normalized.equals(entry.getKey())) {
                continue;
            }
            moved.put(normalized, entry.getValue());
            resources.remove(entry.getKey());
        }
        moved.forEach(resources::putIfAbsent);
    }

    static String normalizeLegacyTexturePath(String path) {
        if (path.startsWith("textures/blocks/")) {
            return "textures/block/" + path.substring("textures/blocks/".length());
        }
        if (path.startsWith("textures/items/")) {
            return "textures/item/" + path.substring("textures/items/".length());
        }
        return path;
    }

    /** Rewrites every original item/block texture reference to the modern singular atlas path. */
    private static void normalizeLegacyModelTextures(Map<String, byte[]> resources) {
        List<String> modelPaths = resources.keySet().stream()
                .filter(path -> path.startsWith("models/") && path.endsWith(".json"))
                .toList();
        for (String modelPath : modelPaths) {
            JsonObject model = parseJsonObject(resources.get(modelPath), modelPath);
            if (rewriteLegacyTextureReferences(model)) {
                putJson(resources, modelPath, model);
            }
        }
    }

    private static boolean rewriteLegacyTextureReferences(JsonElement element) {
        boolean changed = false;
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                changed |= rewriteLegacyTextureReferences(child);
            }
            return changed;
        }
        if (!element.isJsonObject()) {
            return false;
        }
        JsonObject object = element.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : List.copyOf(object.entrySet())) {
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                String oldValue = value.getAsString();
                String newValue = normalizeLegacyTextureId(oldValue);
                if (!newValue.equals(oldValue)) {
                    object.addProperty(entry.getKey(), newValue);
                    changed = true;
                }
            } else {
                changed |= rewriteLegacyTextureReferences(value);
            }
        }
        return changed;
    }

    static String normalizeLegacyTextureId(String id) {
        if (id.startsWith("ic2:blocks/")) {
            return "ic2:block/" + id.substring("ic2:blocks/".length());
        }
        if (id.startsWith("ic2:items/")) {
            return "ic2:item/" + id.substring("ic2:items/".length());
        }
        return id;
    }

    /** Converts IC2's custom UTF-8 property bundles into modern Minecraft language JSON. */
    private static void compileLanguages(OriginalIc2Archive archive, Map<String, byte[]> output) throws IOException {
        int converted = 0;
        for (String asset : archive.listAssets()) {
            if (!asset.startsWith("lang_ic2/") || !asset.endsWith(".properties")) {
                continue;
            }

            String fileName = asset.substring("lang_ic2/".length(), asset.length() - ".properties".length());
            String locale = fileName.toLowerCase(Locale.ROOT);
            Properties properties = new Properties();
            try (InputStreamReader reader = new InputStreamReader(
                    new ByteArrayInputStream(archive.readAsset(asset)), StandardCharsets.UTF_8)) {
                properties.load(reader);
            }

            JsonObject language = new JsonObject();
            properties.stringPropertyNames().stream().sorted().forEach(key ->
                    language.addProperty(qualifyLegacyLanguageKey(key), properties.getProperty(key)));

            // A few old IC2 names were generated directly in Java rather than stored in the
            // language bundle. Add compatibility keys only for those cases; ordinary content is
            // always sourced from the user's original language file.
            OriginalTranslationKeys.generatedCompatibilityTranslations()
                    .forEach((key, value) -> language.addProperty(key, value));

            putJson(output, "lang/" + locale + ".json", language);
            converted++;
        }

        if (converted == 0) {
            throw new IOException("Reference IC2 archive contains no lang_ic2/*.properties files");
        }
        requireOutput(output, "lang/en_us.json");
        validateItemTranslationCoverage(parseJsonObject(
                output.get("lang/en_us.json"), "lang/en_us.json"));
    }

    private static String qualifyLegacyLanguageKey(String key) {
        // This is the exact prefixing rule used by IC2's old Localization.loadLocalization().
        if (key.startsWith("achievement.") || key.startsWith("itemGroup.") || key.startsWith("death.")) {
            return key;
        }
        return "ic2." + key;
    }

    private static void validateItemTranslationCoverage(JsonObject enUs) {
        for (String itemPath : MANIFEST.registries().items()) {
            requireTranslation(enUs, OriginalTranslationKeys.itemDescriptionId(itemPath, null),
                    "item ic2:" + itemPath);
        }
        for (OriginalContentManifest.StackVariant variant : MANIFEST.stackVariants()) {
            requireTranslation(enUs,
                    OriginalTranslationKeys.itemDescriptionId(variant.item(), variant.key()),
                    "variant " + variant.key());
        }
    }

    private static void requireTranslation(JsonObject language, String key, String content) {
        if (!language.has(key)) {
            throw new IllegalStateException(
                    "Original IC2 en_us localization is missing " + key + " for " + content);
        }
    }

    private static Map<String, JsonObject> loadLegacyBlockstates(OriginalIc2Archive archive) throws IOException {
        Map<String, JsonObject> result = new LinkedHashMap<>();
        for (String name : LEGACY_BLOCKSTATE_NAMES) {
            String path = "blockstates/" + name + ".json";
            if (!archive.hasAsset(path)) {
                throw new IOException("Reference IC2 archive is missing " + path);
            }
            String json = new String(archive.readAsset(path), StandardCharsets.UTF_8);
            result.put(name, JsonParser.parseString(json).getAsJsonObject());
        }
        return result;
    }

    private static void compileBlockstates(
            Map<String, byte[]> output, Map<String, JsonObject> oldStates) {
        for (String blockPath : MANIFEST.registries().blocks()) {
            JsonObject modern;
            if (isFluidBlock(blockPath)) {
                modern = compileFluidBlock(blockPath, output);
            } else {
                modern = switch (blockPath) {
                    case "te" -> compileFacingVariants(blockPath, oldStates.get("te"));
                    case "leaves" -> compileLeaves(oldStates.get("leaves"), output);
                    case "rubber_wood" -> compileRubberWood(oldStates.get("rubber_wood"));
                    case "fence", "reinforced_door", "dynamite" -> normalizeDirectBlockstate(oldStates.get(blockPath));
                    case "refractory_bricks" -> compileSingleNamedVariant(
                            oldStates.get("refractory_bricks"), "refractory_bricks");
                    case "sapling" -> compileSingleNamedVariant(oldStates.get("sapling"), "normal");
                    default -> compileTypeVariants(blockPath, oldStates.get(blockPath));
                };
            }
            putJson(output, "blockstates/" + blockPath + ".json", modern);
        }
    }

    /**
     * Generates a modern multipart blockstate for the internal {@code ic2:cable} carrier block.
     * The textures remain sourced from the original IC2 archive; only the thin center/arm geometry
     * is synthesized at runtime because the 1.12 cable used a custom baked model.
     */
    static void compileCableBlockstate(Map<String, byte[]> output) {
        com.google.gson.JsonArray multipart = new com.google.gson.JsonArray();

        for (EuCableVariant cable : EuCableVariant.values()) {
            for (boolean active : List.of(false, true)) {
                String generatedRoot = "ic2ma_generated/cable/" + cable.stateVariantIndex()
                        + "/" + (active ? "active" : "idle");
                String textureStem = cable.blockModelStem(active);
                String textureId = "ic2:block/wiring/cable/" + textureStem;
                requireOutput(output, "textures/block/wiring/cable/" + textureStem + ".png");

                putJson(output,
                        "models/block/" + generatedRoot + "/center.json",
                        cablePartModel(cable.visualWidth(), null, textureId));

                JsonObject centerWhen = new JsonObject();
                centerWhen.addProperty("variant", Integer.toString(cable.stateVariantIndex()));
                centerWhen.addProperty("active", Boolean.toString(active));
                JsonObject centerApply = new JsonObject();
                centerApply.addProperty("model", "ic2:block/" + generatedRoot + "/center");
                JsonObject centerPart = new JsonObject();
                centerPart.add("when", centerWhen);
                centerPart.add("apply", centerApply);
                multipart.add(centerPart);

                for (String directionName : List.of("down", "up", "north", "south", "west", "east")) {
                    putJson(output,
                            "models/block/" + generatedRoot + "/" + directionName + ".json",
                            cablePartModel(cable.visualWidth(), directionName, textureId));

                    JsonObject when = new JsonObject();
                    when.addProperty("variant", Integer.toString(cable.stateVariantIndex()));
                    when.addProperty("active", Boolean.toString(active));
                    when.addProperty(directionName, "true");

                    JsonObject apply = new JsonObject();
                    apply.addProperty("model", "ic2:block/" + generatedRoot + "/" + directionName);

                    JsonObject part = new JsonObject();
                    part.add("when", when);
                    part.add("apply", apply);
                    multipart.add(part);
                }
            }
        }

        JsonObject blockstate = new JsonObject();
        blockstate.add("multipart", multipart);
        putJson(output, "blockstates/cable.json", blockstate);
    }

    private static JsonObject cablePartModel(float width, String directionName, String textureId) {
        double min = 8.0D - width * 8.0D;
        double max = 8.0D + width * 8.0D;

        double x1 = min;
        double y1 = min;
        double z1 = min;
        double x2 = max;
        double y2 = max;
        double z2 = max;

        if (directionName != null) {
            switch (directionName) {
                case "down" -> y1 = 0.0D;
                case "up" -> y2 = 16.0D;
                case "north" -> z1 = 0.0D;
                case "south" -> z2 = 16.0D;
                case "west" -> x1 = 0.0D;
                case "east" -> x2 = 16.0D;
                default -> throw new IllegalArgumentException("Unknown cable direction: " + directionName);
            }
            switch (directionName) {
                case "down" -> y2 = min;
                case "up" -> y1 = max;
                case "north" -> z2 = min;
                case "south" -> z1 = max;
                case "west" -> x2 = min;
                case "east" -> x1 = max;
                default -> {
                }
            }
        }

        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:block/block");
        model.addProperty("ambientocclusion", false);

        JsonObject textures = new JsonObject();
        textures.addProperty("cable", textureId);
        textures.addProperty("particle", textureId);
        model.add("textures", textures);

        JsonObject element = new JsonObject();
        element.add("from", jsonVector(x1, y1, z1));
        element.add("to", jsonVector(x2, y2, z2));

        JsonObject faces = new JsonObject();
        for (String faceName : List.of("down", "up", "north", "south", "west", "east")) {
            JsonObject face = new JsonObject();
            face.addProperty("texture", "#cable");
            faces.add(faceName, face);
        }
        element.add("faces", faces);

        com.google.gson.JsonArray elements = new com.google.gson.JsonArray();
        elements.add(element);
        model.add("elements", elements);
        return model;
    }

    private static com.google.gson.JsonArray jsonVector(double x, double y, double z) {
        com.google.gson.JsonArray vector = new com.google.gson.JsonArray();
        vector.add(x);
        vector.add(y);
        vector.add(z);
        return vector;
    }

    private static JsonObject compileTypeVariants(String blockPath, JsonObject oldState) {
        JsonObject typeMap = requireObject(requireObject(oldState, "variants"), "type");
        List<OriginalContentManifest.StackVariant> variants = MANIFEST.stackVariants(blockPath);

        JsonObject modernVariants = new JsonObject();
        if (variants.size() <= 1) {
            String sourceName = variants.isEmpty() ? firstKey(typeMap) : suffix(variants.get(0).key());
            modernVariants.add("", normalizeModelDescriptor(requireElement(typeMap, sourceName)));
        } else {
            JsonElement fallback = normalizeModelDescriptor(
                    requireElement(typeMap, suffix(variants.get(0).key())));
            for (int index = 0; index <= LegacyVariantBlock.MAX_VARIANT_INDEX; index++) {
                JsonElement descriptor = index < variants.size()
                        ? normalizeModelDescriptor(requireElement(typeMap, suffix(variants.get(index).key())))
                        : fallback.deepCopy();
                modernVariants.add("variant=" + index, descriptor);
            }
        }

        JsonObject result = new JsonObject();
        result.add("variants", modernVariants);
        return result;
    }

    private static JsonObject compileFacingVariants(String blockPath, JsonObject oldState) {
        JsonObject variantsNode = requireObject(oldState, "variants");
        JsonObject typeMap = requireObject(variantsNode, "type");
        JsonObject facingMap = requireObject(variantsNode, "facing");
        List<OriginalContentManifest.StackVariant> variants = MANIFEST.stackVariants(blockPath);

        JsonObject modernVariants = new JsonObject();
        String fallbackName = suffix(variants.get(0).key());
        JsonObject fallback = normalizeModelDescriptor(requireElement(typeMap, fallbackName)).getAsJsonObject();
        for (int index = 0; index <= LegacyVariantBlock.MAX_VARIANT_INDEX; index++) {
            String sourceName = index < variants.size() ? suffix(variants.get(index).key()) : fallbackName;
            JsonObject inactive = index < variants.size()
                    ? normalizeModelDescriptor(requireElement(typeMap, sourceName)).getAsJsonObject()
                    : fallback;
            JsonObject active = typeMap.has(sourceName + "_active")
                    ? normalizeModelDescriptor(requireElement(typeMap, sourceName + "_active")).getAsJsonObject()
                    : inactive;

            for (String facing : List.of("down", "up", "north", "south", "west", "east")) {
                JsonElement facingTransform = requireElement(facingMap, facing);
                JsonObject inactiveDescriptor = inactive.deepCopy();
                mergeModelTransform(inactiveDescriptor, facingTransform);
                modernVariants.add(
                        "variant=" + index + ",facing=" + facing + ",active=false",
                        inactiveDescriptor);

                JsonObject activeDescriptor = active.deepCopy();
                mergeModelTransform(activeDescriptor, facingTransform);
                modernVariants.add(
                        "variant=" + index + ",facing=" + facing + ",active=true",
                        activeDescriptor);
            }
        }

        JsonObject result = new JsonObject();
        result.add("variants", modernVariants);
        return result;
    }

    private static JsonObject compileLeaves(JsonObject oldState, Map<String, byte[]> output) {
        JsonObject typeMap = requireObject(requireObject(oldState, "variants"), "type");
        JsonElement originalDescriptor = normalizeModelDescriptor(requireElement(typeMap, "rubber"));
        String originalModel = modelId(originalDescriptor);

        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("parent", originalModel);
        wrapper.addProperty("render_type", "minecraft:cutout_mipped");
        putJson(output, "models/block/ic2ma_generated/leaves/rubber.json", wrapper);

        JsonObject descriptor = new JsonObject();
        descriptor.addProperty("model", "ic2:block/ic2ma_generated/leaves/rubber");
        JsonObject variants = new JsonObject();
        variants.add("", descriptor);
        JsonObject result = new JsonObject();
        result.add("variants", variants);
        return result;
    }

    private static JsonObject compileRubberWood(JsonObject oldState) {
        JsonObject stateMap = requireObject(requireObject(oldState, "variants"), "state");
        JsonObject variants = new JsonObject();
        variants.add("axis=y", normalizeModelDescriptor(requireElement(stateMap, "plain_y")));
        variants.add("axis=x", normalizeModelDescriptor(requireElement(stateMap, "plain_x")));
        variants.add("axis=z", normalizeModelDescriptor(requireElement(stateMap, "plain_z")));
        JsonObject result = new JsonObject();
        result.add("variants", variants);
        return result;
    }

    private static JsonObject compileSingleNamedVariant(JsonObject oldState, String sourceName) {
        JsonObject variants = new JsonObject();
        variants.add("", normalizeModelDescriptor(requireElement(requireObject(oldState, "variants"), sourceName)));
        JsonObject result = new JsonObject();
        result.add("variants", variants);
        return result;
    }

    private static JsonObject normalizeDirectBlockstate(JsonObject oldState) {
        JsonObject copy = oldState.deepCopy();
        copy.remove("forge_marker");
        copy.remove("defaults");
        rewriteModelReferences(copy);
        return copy;
    }

    private static JsonObject compileFluidBlock(String blockPath, Map<String, byte[]> output) {
        String modelPath = "models/block/ic2ma_generated/fluid/" + blockPath + ".json";
        JsonObject model = new JsonObject();
        model.addProperty("parent", "ic2:block/sheet_base");
        model.addProperty("render_type", "minecraft:translucent");
        JsonObject textures = new JsonObject();
        textures.addProperty("texture", "ic2:block/fluid/" + blockPath + "_still");
        model.add("textures", textures);
        putJson(output, modelPath, model);

        JsonObject variants = new JsonObject();
        JsonObject descriptor = new JsonObject();
        descriptor.addProperty("model", "ic2:block/ic2ma_generated/fluid/" + blockPath);
        variants.add("", descriptor);
        JsonObject state = new JsonObject();
        state.add("variants", variants);
        return state;
    }

    private static void compileBlockItemModels(
            Map<String, byte[]> output, Map<String, JsonObject> oldStates) {
        for (String blockPath : MANIFEST.registries().blockItems()) {
            List<OriginalContentManifest.StackVariant> variants = MANIFEST.stackVariants(blockPath);
            if (variants.size() > 1) {
                putJson(output, "models/item/" + blockPath + ".json",
                        compileVariantItemModel(blockPath, oldStates.get(blockPath), variants, output));
                continue;
            }

            // Keep a dedicated original item model when IC2 supplied one (notably the reinforced
            // door and refractory bricks). Otherwise synthesize a simple item view from the block.
            String itemModelPath = "models/item/" + blockPath + ".json";
            if (output.containsKey(itemModelPath)) {
                continue;
            }

            JsonObject itemModel = new JsonObject();
            String parent = modelForBlockItem(blockPath, oldStates, variants, output);
            itemModel.addProperty("parent", parent);
            putJson(output, itemModelPath, itemModel);
        }
    }

    private static JsonObject compileVariantItemModel(
            String blockPath,
            JsonObject oldState,
            List<OriginalContentManifest.StackVariant> variants,
            Map<String, byte[]> output) {
        JsonObject result = new JsonObject();
        JsonArray overrides = new JsonArray();

        for (int index = 0; index < variants.size(); index++) {
            String model = itemModelForLegacyVariant(
                    blockPath, oldState, suffix(variants.get(index).key()), output);
            if (index == 0) {
                result.addProperty("parent", model);
            }
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", index + 1);
            override.add("predicate", predicate);
            override.addProperty("model", model);
            overrides.add(override);
        }
        result.add("overrides", overrides);
        return result;
    }

    private static String modelForBlockItem(
            String blockPath,
            Map<String, JsonObject> oldStates,
            List<OriginalContentManifest.StackVariant> variants,
            Map<String, byte[]> output) {
        if (isFluidBlock(blockPath)) {
            return ensureWrappedBlockItemModel(blockPath, blockPath, "ic2:block/ic2ma_generated/fluid/" + blockPath, output);
        }
        if (blockPath.equals("leaves")) {
            return "ic2:block/ic2ma_generated/leaves/rubber";
        }
        if (blockPath.equals("rubber_wood")) {
            return modelId(requireElement(
                    requireObject(requireObject(oldStates.get("rubber_wood"), "variants"), "state"), "plain_y"));
        }
        if (blockPath.equals("fence")) {
            requireItemModel(output, "fence/iron", blockPath);
            return "ic2:item/fence/iron";
        }
        if (blockPath.equals("sapling")) {
            return modelId(requireElement(requireObject(oldStates.get("sapling"), "variants"), "normal"));
        }
        if (blockPath.equals("refractory_bricks")) {
            return modelId(requireElement(
                    requireObject(oldStates.get("refractory_bricks"), "variants"), "refractory_bricks"));
        }
        if (!variants.isEmpty()) {
            return itemModelForLegacyVariant(blockPath, oldStates.get(blockPath), suffix(variants.get(0).key()), output);
        }
        throw new IllegalStateException("No block item model mapping for ic2:" + blockPath);
    }

    private static String itemModelForLegacyVariant(
            String blockPath, JsonObject oldState, String variantName, Map<String, byte[]> output) {
        if (blockPath.equals("fence")) {
            requireItemModel(output, "fence/iron", blockPath + "/" + variantName);
            return "ic2:item/fence/iron";
        }
        String model = modelForLegacyVariant(blockPath, oldState, variantName);
        if (requiresWrappedBlockItemModel(blockPath)) {
            return ensureWrappedBlockItemModel(blockPath, variantName, model, output);
        }
        return model;
    }

    private static String modelForLegacyVariant(String blockPath, JsonObject oldState, String variantName) {
        JsonObject variantsNode = requireObject(oldState, "variants");
        JsonObject typeMap = requireObject(variantsNode, "type");
        return modelId(requireElement(typeMap, variantName));
    }

    private static boolean requiresWrappedBlockItemModel(String blockPath) {
        return blockPath.equals("te") || blockPath.equals("sheet") || isFluidBlock(blockPath);
    }

    private static String ensureWrappedBlockItemModel(
            String blockPath, String variantName, String parentModel, Map<String, byte[]> output) {
        String modelPath = "models/item/ic2ma_generated/block_item/" + blockPath + "/" + variantName + ".json";
        if (!output.containsKey(modelPath)) {
            JsonObject model = new JsonObject();
            model.addProperty("parent", parentModel);
            model.add("display", defaultBlockItemDisplay());
            putJson(output, modelPath, model);
        }
        return "ic2:item/ic2ma_generated/block_item/" + blockPath + "/" + variantName;
    }

    private static JsonObject defaultBlockItemDisplay() {
        JsonObject display = new JsonObject();
        display.add("thirdperson_righthand", transformJson(75, 45, 0, 0, 2.5, 0, 0.375, 0.375, 0.375));
        display.add("thirdperson_lefthand", transformJson(75, 45, 0, 0, 2.5, 0, 0.375, 0.375, 0.375));
        display.add("firstperson_righthand", transformJson(0, 45, 0, 0, 0, 0, 0.4, 0.4, 0.4));
        display.add("firstperson_lefthand", transformJson(0, 225, 0, 0, 0, 0, 0.4, 0.4, 0.4));
        display.add("gui", transformJson(30, 225, 0, 0, 0, 0, 0.625, 0.625, 0.625));
        display.add("ground", transformJson(0, 0, 0, 0, 3, 0, 0.25, 0.25, 0.25));
        display.add("fixed", transformJson(0, 0, 0, 0, 0, 0, 0.5, 0.5, 0.5));
        return display;
    }

    private static JsonObject transformJson(
            double rotX,
            double rotY,
            double rotZ,
            double transX,
            double transY,
            double transZ,
            double scaleX,
            double scaleY,
            double scaleZ) {
        JsonObject transform = new JsonObject();
        transform.add("rotation", triple(rotX, rotY, rotZ));
        transform.add("translation", triple(transX, transY, transZ));
        transform.add("scale", triple(scaleX, scaleY, scaleZ));
        return transform;
    }

    private static JsonArray triple(double x, double y, double z) {
        JsonArray values = new JsonArray();
        values.add(x);
        values.add(y);
        values.add(z);
        return values;
    }

    /**
     * Recreates the code-side item model registration that IC2 1.12 performed through
     * ModelLoader.setCustomModelResourceLocation / custom mesh definitions. Merely copying the
     * original nested JSON files is insufficient on modern Minecraft: registry id
     * ic2:advanced_batpack is looked up as models/item/advanced_batpack.json, while IC2 stored the
     * actual model at models/item/armor/advanced_batpack.json and connected the two in Java.
     */
    private static void compileStandaloneItemModels(Map<String, byte[]> output) {
        Set<String> blockItems = Set.copyOf(MANIFEST.registries().blockItems());
        Map<String, String> uniqueModelsByBasename = indexUniqueOriginalItemModels(output);

        for (String itemPath : MANIFEST.registries().items()) {
            if (blockItems.contains(itemPath)) {
                continue;
            }

            String rootPath = "models/item/" + itemPath + ".json";
            List<OriginalContentManifest.StackVariant> variants = MANIFEST.stackVariants(itemPath);
            if (!variants.isEmpty()) {
                putJson(output, rootPath, compileFiniteStandaloneItemModel(itemPath, variants, output));
                continue;
            }

            // Some root items already had a top-level model in the original resource pack.
            if (output.containsKey(rootPath)) {
                continue;
            }

            String originalModel = uniqueModelsByBasename.get(itemPath);
            if (originalModel == null) {
                originalModel = resolveFolderedRootItemModel(output, itemPath);
            }
            if (originalModel == null) {
                originalModel = OriginalItemModels.dynamicDefaultModel(itemPath);
            }
            if (originalModel == null) {
                throw new IllegalStateException("No original item model mapping for ic2:" + itemPath);
            }
            requireItemModel(output, originalModel, itemPath);
            putJson(output, rootPath, aliasItemModel(originalModel));
        }
    }

    private static JsonObject compileFiniteStandaloneItemModel(
            String itemPath,
            List<OriginalContentManifest.StackVariant> variants,
            Map<String, byte[]> output) {
        JsonObject result = new JsonObject();
        JsonArray overrides = new JsonArray();

        for (int index = 0; index < variants.size(); index++) {
            OriginalContentManifest.StackVariant variant = variants.get(index);
            String originalModel = OriginalItemModels.finiteVariantModel(itemPath, variant.key());
            requireItemModel(output, originalModel, variant.key());
            String modelId = "ic2:item/" + originalModel;
            if (index == 0) {
                result.addProperty("parent", modelId);
            }

            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", index + 1);
            override.add("predicate", predicate);
            override.addProperty("model", modelId);
            overrides.add(override);
        }
        result.add("overrides", overrides);
        return result;
    }

    private static JsonObject aliasItemModel(String originalModel) {
        JsonObject alias = new JsonObject();
        alias.addProperty("parent", "ic2:item/" + originalModel);
        return alias;
    }

    private static String resolveFolderedRootItemModel(Map<String, byte[]> output, String itemPath) {
        String match = null;
        for (String candidate : OriginalItemModels.rootModelCandidates(itemPath)) {
            if (!output.containsKey("models/item/" + candidate + ".json")) {
                continue;
            }
            if (match != null && !match.equals(candidate)) {
                throw new IllegalStateException(
                        "Ambiguous original IC2 item model mapping for ic2:" + itemPath
                                + ": " + match + " and " + candidate);
            }
            match = candidate;
        }
        return match;
    }

    private static Map<String, String> indexUniqueOriginalItemModels(Map<String, byte[]> output) {
        Map<String, String> result = new LinkedHashMap<>();
        Set<String> duplicates = new HashSet<>();
        for (String path : output.keySet()) {
            if (!path.startsWith("models/item/") || !path.endsWith(".json")) {
                continue;
            }
            String model = path.substring("models/item/".length(), path.length() - ".json".length());
            String basename = model.substring(model.lastIndexOf('/') + 1);
            String previous = result.putIfAbsent(basename, model);
            if (previous != null && !previous.equals(model)) {
                duplicates.add(basename);
            }
        }
        duplicates.forEach(result::remove);
        return result;
    }

    private static void requireItemModel(Map<String, byte[]> output, String modelPath, String content) {
        String resourcePath = "models/item/" + modelPath + ".json";
        if (!output.containsKey(resourcePath)) {
            throw new IllegalStateException(
                    "Original IC2 item model " + resourcePath + " is missing for " + content);
        }
    }

    /**
     * Reproduces the old IC2 CUTOUT decision for block models using the original PNG alpha. Fluid
     * placeholders are generated explicitly as TRANSLUCENT and rubber leaves use CUTOUT_MIPPED.
     * This keeps the runtime pack data-driven instead of maintaining a second hand-written list of
     * transparent IC2 models.
     */
    private static void assignCutoutRenderTypes(Map<String, byte[]> resources) {
        List<String> modelPaths = resources.keySet().stream()
                .filter(path -> path.startsWith("models/block/") && path.endsWith(".json"))
                .toList();
        Map<String, Boolean> alphaCache = new LinkedHashMap<>();

        for (String modelPath : modelPaths) {
            JsonObject model = parseJsonObject(resources.get(modelPath), modelPath);
            if (model.has("render_type")) {
                continue;
            }
            if (modelUsesTransparentIc2Texture(modelPath, resources, alphaCache, new HashSet<>())) {
                model.addProperty("render_type", "minecraft:cutout");
                putJson(resources, modelPath, model);
            }
        }
    }

    private static boolean modelUsesTransparentIc2Texture(
            String modelPath,
            Map<String, byte[]> resources,
            Map<String, Boolean> alphaCache,
            Set<String> visiting) {
        if (!visiting.add(modelPath)) {
            return false;
        }
        byte[] modelBytes = resources.get(modelPath);
        if (modelBytes == null) {
            return false;
        }

        JsonObject model = parseJsonObject(modelBytes, modelPath);
        JsonObject textures = model.has("textures") && model.get("textures").isJsonObject()
                ? model.getAsJsonObject("textures")
                : null;
        if (textures != null) {
            for (Map.Entry<String, JsonElement> texture : textures.entrySet()) {
                if (!texture.getValue().isJsonPrimitive()) {
                    continue;
                }
                String textureId = texture.getValue().getAsString();
                if (textureId.startsWith("#") || !textureId.startsWith("ic2:")) {
                    continue;
                }
                String texturePath = "textures/" + textureId.substring("ic2:".length()) + ".png";
                if (textureHasTransparency(texturePath, resources, alphaCache)) {
                    return true;
                }
            }
        }

        JsonElement parent = model.get("parent");
        if (parent != null && parent.isJsonPrimitive()) {
            String parentId = parent.getAsString();
            if (parentId.startsWith("ic2:block/")) {
                String parentPath = "models/block/" + parentId.substring("ic2:block/".length()) + ".json";
                return modelUsesTransparentIc2Texture(parentPath, resources, alphaCache, visiting);
            }
        }
        return false;
    }

    private static boolean textureHasTransparency(
            String texturePath, Map<String, byte[]> resources, Map<String, Boolean> cache) {
        Boolean cached = cache.get(texturePath);
        if (cached != null) {
            return cached;
        }
        byte[] png = resources.get(texturePath);
        if (png == null) {
            cache.put(texturePath, false);
            return false;
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
            if (image == null || !image.getColorModel().hasAlpha()) {
                cache.put(texturePath, false);
                return false;
            }
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if ((image.getRGB(x, y) >>> 24) != 0xFF) {
                        cache.put(texturePath, true);
                        return true;
                    }
                }
            }
            cache.put(texturePath, false);
            return false;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to inspect original IC2 texture " + texturePath, e);
        }
    }

    private static JsonObject parseJsonObject(byte[] bytes, String path) {
        try {
            return JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Invalid JSON in compiled IC2 resource " + path, e);
        }
    }

    private static JsonElement normalizeModelDescriptor(JsonElement source) {
        JsonElement copy = source.deepCopy();
        rewriteModelReferences(copy);
        if (copy.isJsonObject()) {
            copy.getAsJsonObject().remove("transform");
        }
        return copy;
    }

    private static void mergeModelTransform(JsonObject target, JsonElement source) {
        if (!source.isJsonObject()) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : source.getAsJsonObject().entrySet()) {
            if (entry.getKey().equals("transform") || entry.getKey().equals("model")) {
                continue;
            }
            target.add(entry.getKey(), entry.getValue().deepCopy());
        }
    }

    private static void rewriteModelReferences(JsonElement element) {
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                rewriteModelReferences(child);
            }
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }
        JsonObject object = element.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : List.copyOf(object.entrySet())) {
            if (entry.getKey().equals("model") && entry.getValue().isJsonPrimitive()) {
                object.addProperty("model", normalizeModelId(entry.getValue().getAsString()));
            } else {
                rewriteModelReferences(entry.getValue());
            }
        }
    }

    private static String modelId(JsonElement descriptor) {
        JsonObject normalized = normalizeModelDescriptor(descriptor).getAsJsonObject();
        JsonElement model = normalized.get("model");
        if (model == null) {
            throw new IllegalStateException("IC2 model descriptor has no model: " + descriptor);
        }
        return model.getAsString();
    }

    private static String normalizeModelId(String model) {
        if (model.startsWith("ic2:block/")) {
            return model;
        }
        if (model.startsWith("ic2:")) {
            return "ic2:block/" + model.substring("ic2:".length());
        }
        if (!model.contains(":")) {
            return "minecraft:block/" + model;
        }
        return model;
    }

    private static boolean isFluidBlock(String blockPath) {
        return MANIFEST.registries().fluidPaths().contains(blockPath);
    }

    private static String suffix(String variantKey) {
        int slash = variantKey.indexOf('/');
        return slash < 0 ? variantKey : variantKey.substring(slash + 1);
    }

    private static JsonObject requireObject(JsonObject parent, String key) {
        JsonElement element = requireElement(parent, key);
        if (!element.isJsonObject()) {
            throw new IllegalStateException("Expected IC2 JSON object '" + key + "'");
        }
        return element.getAsJsonObject();
    }

    private static JsonElement requireElement(JsonObject parent, String key) {
        if (parent == null) {
            throw new IllegalStateException("Missing source IC2 blockstate while looking for '" + key + "'");
        }
        JsonElement element = parent.get(key);
        if (element == null) {
            throw new IllegalStateException("Missing IC2 JSON entry '" + key + "' in " + parent);
        }
        return element;
    }

    private static String firstKey(JsonObject object) {
        return object.keySet().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Empty IC2 model variant map"));
    }

    private static void putJson(Map<String, byte[]> output, String path, JsonObject json) {
        output.put(path, GSON.toJson(json).getBytes(StandardCharsets.UTF_8));
    }

    private static void validateOutput(Map<String, byte[]> resources) {
        for (String resourcePath : resources.keySet()) {
            if (!CompiledIc2ResourcePack.isValidResourcePath(resourcePath)) {
                throw new IllegalStateException(
                        "Compiled IC2 runtime pack contains an invalid modern resource path: " + resourcePath);
            }
        }

        Set<String> modelIds = new java.util.LinkedHashSet<>();
        requireOutput(resources, "blockstates/cable.json");
        collectModelIds(parseJsonObject(resources.get("blockstates/cable.json"), "blockstates/cable.json"), modelIds);
        for (String block : MANIFEST.registries().blocks()) {
            String statePath = "blockstates/" + block + ".json";
            requireOutput(resources, statePath);
            collectModelIds(parseJsonObject(resources.get(statePath), statePath), modelIds);
        }
        for (String item : MANIFEST.registries().items()) {
            String itemModelPath = "models/item/" + item + ".json";
            requireOutput(resources, itemModelPath);
            modelIds.add("ic2:item/" + item);
            collectModelIds(parseJsonObject(resources.get(itemModelPath), itemModelPath), modelIds);
        }

        Set<String> validatedModels = new HashSet<>();
        for (String modelId : modelIds) {
            validateIc2Model(modelId, resources, validatedModels);
        }
        requireOutput(resources, "models/block/machine/processing/basic/macerator.json");
        requireOutput(resources, "textures/block/machine/processing/basic/macerator_front.png");
    }

    private static void collectModelIds(JsonElement element, Set<String> output) {
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                collectModelIds(child, output);
            }
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            if ("model".equals(entry.getKey()) && entry.getValue().isJsonPrimitive()) {
                output.add(entry.getValue().getAsString());
            } else {
                collectModelIds(entry.getValue(), output);
            }
        }
    }

    private static void validateIc2Model(
            String modelId, Map<String, byte[]> resources, Set<String> validatedModels) {
        String path;
        if (modelId.startsWith("ic2:block/")) {
            path = "models/block/" + modelId.substring("ic2:block/".length()) + ".json";
        } else if (modelId.startsWith("ic2:item/")) {
            path = "models/item/" + modelId.substring("ic2:item/".length()) + ".json";
        } else {
            return;
        }
        if (!validatedModels.add(path)) {
            return;
        }
        requireOutput(resources, path);
        JsonObject model = parseJsonObject(resources.get(path), path);

        JsonElement parent = model.get("parent");
        if (parent != null && parent.isJsonPrimitive()) {
            validateIc2Model(parent.getAsString(), resources, validatedModels);
        }

        JsonObject textures = model.has("textures") && model.get("textures").isJsonObject()
                ? model.getAsJsonObject("textures")
                : null;
        if (textures != null) {
            for (Map.Entry<String, JsonElement> textureEntry : textures.entrySet()) {
                JsonElement texture = textureEntry.getValue();
                if (!texture.isJsonPrimitive()) {
                    continue;
                }
                String textureId = texture.getAsString();
                if (textureId.startsWith("#") || !textureId.startsWith("ic2:")) {
                    continue;
                }
                requireOutput(resources, textureResourcePath(textureId));
            }
        }
    }

    /** Resolves an IC2 model texture id to the path exposed by the compiled modern pack. */
    static String textureResourcePath(String textureId) {
        String normalized = normalizeLegacyTextureId(textureId);
        if (normalized.startsWith("ic2:block/")) {
            return "textures/block/" + normalized.substring("ic2:block/".length()) + ".png";
        }
        if (normalized.startsWith("ic2:item/")) {
            return "textures/item/" + normalized.substring("ic2:item/".length()) + ".png";
        }
        if (normalized.startsWith("ic2:")) {
            return "textures/" + normalized.substring("ic2:".length()) + ".png";
        }
        throw new IllegalArgumentException("Not an IC2 texture id: " + textureId);
    }


    private static void requireOutput(Map<String, byte[]> resources, String path) {
        if (!resources.containsKey(path)) {
            throw new IllegalStateException("Compiled IC2 resource pack is missing " + path);
        }
    }

    private IC2RuntimeResourceCompiler() {
    }
}
