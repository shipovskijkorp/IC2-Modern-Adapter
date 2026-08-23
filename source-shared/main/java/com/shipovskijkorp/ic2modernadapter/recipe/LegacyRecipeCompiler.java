package com.shipovskijkorp.ic2modernadapter.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.shipovskijkorp.ic2modernadapter.resource.OriginalIc2Archive;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Compiles IC2's original crafting/furnace ini files into modern runtime datapack recipe JSON. */
public final class LegacyRecipeCompiler {
    public static final String SHAPED_INI = "config/shaped_recipes.ini";
    public static final String SHAPELESS_INI = "config/shapeless_recipes.ini";
    public static final String FURNACE_INI = "config/furnace.ini";
    public static final int EXPECTED_SHAPED = 354;
    public static final int EXPECTED_SHAPELESS_INI = 79;
    public static final int EXPECTED_FILLER = 3;
    public static final int EXPECTED_SMELTING = 27;
    public static final int EXPECTED_TOTAL = 460;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private LegacyRecipeCompiler() {
    }

    public static Result compile(OriginalIc2Archive archive) throws IOException {
        List<LegacyRecipeDefinition> shapedRecipes = LegacyIniRecipeParser.parseShaped(archive.readAsset(SHAPED_INI));
        List<LegacyRecipeDefinition> shapelessRecipes = LegacyIniRecipeParser.parseShapeless(archive.readAsset(SHAPELESS_INI));
        List<LegacyRecipeDefinition> furnaceRecipes = LegacyIniRecipeParser.parseFurnace(archive.readAsset(FURNACE_INI));
        long fillerDefinitions = shapelessRecipes.stream()
                .filter(recipe -> recipe.kind() == LegacyRecipeDefinition.Kind.FILLER)
                .count();
        if (shapedRecipes.size() != EXPECTED_SHAPED
                || shapelessRecipes.size() != EXPECTED_SHAPELESS_INI
                || fillerDefinitions != EXPECTED_FILLER
                || furnaceRecipes.size() != EXPECTED_SMELTING) {
            throw new IOException("Unexpected IC2 2.8.222 workstation recipe surface: shaped="
                    + shapedRecipes.size() + ", shapeless=" + shapelessRecipes.size()
                    + " (filler=" + fillerDefinitions + "), furnace=" + furnaceRecipes.size());
        }

        List<LegacyRecipeDefinition> recipes = new ArrayList<>(EXPECTED_TOTAL);
        recipes.addAll(shapedRecipes);
        recipes.addAll(shapelessRecipes);
        recipes.addAll(furnaceRecipes);
        if (recipes.size() != EXPECTED_TOTAL) {
            throw new IOException("Unexpected IC2 workstation recipe total: " + recipes.size());
        }

        Map<String, byte[]> serverData = new LinkedHashMap<>();
        int shaped = 0;
        int shapeless = 0;
        int filler = 0;
        int smelting = 0;
        for (int index = 0; index < recipes.size(); index++) {
            LegacyRecipeDefinition recipe = recipes.get(index);
            String kind = switch (recipe.kind()) {
                case SHAPED -> {
                    shaped++;
                    yield "legacy_crafting";
                }
                case SHAPELESS -> {
                    shapeless++;
                    yield "legacy_crafting";
                }
                case FILLER -> {
                    filler++;
                    yield "legacy_crafting";
                }
                case SMELTING -> {
                    smelting++;
                    yield "legacy_smelting";
                }
            };
            JsonObject json = new JsonObject();
            json.addProperty("type", "ic2_modern_adapter:" + kind);
            json.addProperty("payload", recipe.payload());
            byte[] bytes = GSON.toJson(json).getBytes(StandardCharsets.UTF_8);
            String file = String.format("ic2ma_original/%03d_%s.json", index, kind);

            // 1.20.x scans data/<namespace>/recipes, while 1.21.x uses singular recipe.
            // Publishing both keeps the compiler loader/version-neutral; each game version ignores
            // the directory name it does not scan.
            serverData.put("recipes/" + file, bytes);
            serverData.put("recipe/" + file, bytes);
        }
        return new Result(Map.copyOf(serverData), recipes.size(), shaped, shapeless, filler, smelting);
    }

    public record Result(
            Map<String, byte[]> resources,
            int recipes,
            int shaped,
            int shapeless,
            int filler,
            int smelting) {
    }
}
