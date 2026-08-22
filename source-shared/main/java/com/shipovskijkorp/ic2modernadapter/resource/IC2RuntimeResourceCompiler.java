package com.shipovskijkorp.ic2modernadapter.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import com.shipovskijkorp.ic2modernadapter.content.OriginalTranslationKeys;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantBlock;
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

        compileLanguages(archive, resources);

        Map<String, JsonObject> legacyBlockstates = loadLegacyBlockstates(archive);
        compileBlockstates(resources, legacyBlockstates);
        compileBlockItemModels(resources, legacyBlockstates);
        assignCutoutRenderTypes(resources);
        validateOutput(resources);
        return new CompiledIc2ResourcePack(archive.path(), resources);
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
        JsonObject fallback = normalizeModelDescriptor(
                requireElement(typeMap, suffix(variants.get(0).key()))).getAsJsonObject();
        for (int index = 0; index <= LegacyVariantBlock.MAX_VARIANT_INDEX; index++) {
            JsonObject base = index < variants.size()
                    ? normalizeModelDescriptor(requireElement(typeMap, suffix(variants.get(index).key())))
                            .getAsJsonObject()
                    : fallback;
            for (String facing : List.of("down", "up", "north", "south", "west", "east")) {
                JsonObject descriptor = base.deepCopy();
                mergeModelTransform(descriptor, requireElement(facingMap, facing));
                modernVariants.add("variant=" + index + ",facing=" + facing, descriptor);
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
        model.addProperty("parent", "minecraft:block/cube_all");
        model.addProperty("render_type", "minecraft:translucent");
        JsonObject textures = new JsonObject();
        textures.addProperty("all", "ic2:blocks/fluid/" + blockPath + "_still");
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
                        compileVariantItemModel(blockPath, oldStates.get(blockPath), variants));
                continue;
            }

            // Keep a dedicated original item model when IC2 supplied one (notably the reinforced
            // door and refractory bricks). Otherwise synthesize a simple item view from the block.
            String itemModelPath = "models/item/" + blockPath + ".json";
            if (output.containsKey(itemModelPath)) {
                continue;
            }

            JsonObject itemModel = new JsonObject();
            String parent = modelForBlockItem(blockPath, oldStates, variants);
            itemModel.addProperty("parent", parent);
            putJson(output, itemModelPath, itemModel);
        }
    }

    private static JsonObject compileVariantItemModel(
            String blockPath,
            JsonObject oldState,
            List<OriginalContentManifest.StackVariant> variants) {
        JsonObject result = new JsonObject();
        JsonArray overrides = new JsonArray();

        for (int index = 0; index < variants.size(); index++) {
            String model = modelForLegacyVariant(blockPath, oldState, suffix(variants.get(index).key()));
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
            List<OriginalContentManifest.StackVariant> variants) {
        if (isFluidBlock(blockPath)) {
            return "ic2:block/ic2ma_generated/fluid/" + blockPath;
        }
        if (blockPath.equals("leaves")) {
            return "ic2:block/ic2ma_generated/leaves/rubber";
        }
        if (blockPath.equals("rubber_wood")) {
            return modelId(requireElement(
                    requireObject(requireObject(oldStates.get("rubber_wood"), "variants"), "state"), "plain_y"));
        }
        if (blockPath.equals("fence")) {
            return "ic2:block/fence/iron_post";
        }
        if (blockPath.equals("sapling")) {
            return modelId(requireElement(requireObject(oldStates.get("sapling"), "variants"), "normal"));
        }
        if (blockPath.equals("refractory_bricks")) {
            return modelId(requireElement(
                    requireObject(oldStates.get("refractory_bricks"), "variants"), "refractory_bricks"));
        }
        if (!variants.isEmpty()) {
            return modelForLegacyVariant(blockPath, oldStates.get(blockPath), suffix(variants.get(0).key()));
        }
        throw new IllegalStateException("No block item model mapping for ic2:" + blockPath);
    }

    private static String modelForLegacyVariant(String blockPath, JsonObject oldState, String variantName) {
        JsonObject variantsNode = requireObject(oldState, "variants");
        JsonObject typeMap = requireObject(variantsNode, "type");
        return modelId(requireElement(typeMap, variantName));
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
        for (String block : MANIFEST.registries().blocks()) {
            String statePath = "blockstates/" + block + ".json";
            requireOutput(resources, statePath);
            collectModelIds(parseJsonObject(resources.get(statePath), statePath), modelIds);
        }
        for (String blockItem : MANIFEST.registries().blockItems()) {
            String itemModelPath = "models/item/" + blockItem + ".json";
            requireOutput(resources, itemModelPath);
            collectModelIds(parseJsonObject(resources.get(itemModelPath), itemModelPath), modelIds);
        }

        // Dynamite was not an ItemBlock in IC2 1.12, but the placeholder item is intentionally
        // placeable during this visual-only milestone. Its original 2D item model is still used.
        requireOutput(resources, "models/item/dynamite.json");

        Set<String> validatedModels = new HashSet<>();
        for (String modelId : modelIds) {
            validateIc2Model(modelId, resources, validatedModels);
        }
        requireOutput(resources, "models/block/machine/processing/basic/macerator.json");
        requireOutput(resources, "textures/blocks/machine/processing/basic/macerator_front.png");
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
                requireOutput(resources,
                        "textures/" + textureId.substring("ic2:".length()) + ".png");
            }
        }
    }


    private static void requireOutput(Map<String, byte[]> resources, String path) {
        if (!resources.containsKey(path)) {
            throw new IllegalStateException("Compiled IC2 resource pack is missing " + path);
        }
    }

    private IC2RuntimeResourceCompiler() {
    }
}
