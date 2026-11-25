package com.shipovskijkorp.ic2modernadapter.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shipovskijkorp.ic2modernadapter.worldgen.SilverOreVariant;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Regression checks for worldgen/datapack identity and Industrial Legacy ore distribution. */
final class OreWorldgenParityTest {
    private static final String ROOT = "/data/ic2_modern_adapter/";

    @Test
    void oreDistributionMatchesIndustrialLegacy() {
        verifyOre("tin", 8, 28, -64, 96, "uniform", "2");
        verifyOre("lead", 8, 20, -64, 64, "uniform", "1");
        verifyOre("uranium", 6, 10, -64, 64, "uniform", "3");
        verifyOre("silver", 9, 4, -64, 32, "trapezoid", "14");
        JsonObject extra = json(ROOT + "worldgen/placed_feature/silver_ore_mountains.json");
        assertEquals("ic2_modern_adapter:silver_ore", extra.get("feature").getAsString());
        assertEquals(7, extra.getAsJsonArray("placement").get(0).getAsJsonObject().get("count").getAsInt());
    }

    @Test
    void allNewOresHaveModelsLootTagsAndTextures() {
        for (String path : new String[] {"silver_ore", "deepslate_silver_ore", "deepslate_tin_ore",
                "deepslate_lead_ore", "deepslate_uranium_ore"}) {
            json("/assets/ic2_modern_adapter/blockstates/" + path + ".json");
            json("/assets/ic2_modern_adapter/models/block/" + path + ".json");
            json("/assets/ic2_modern_adapter/models/item/" + path + ".json");
            json(ROOT + "loot_table/blocks/" + path + ".json");
            json(ROOT + "loot_tables/blocks/" + path + ".json");
            assertNotNull(OreWorldgenParityTest.class.getResourceAsStream(
                    "/assets/ic2_modern_adapter/textures/block/" + path + ".png"));
        }
        for (String metal : new String[] {"silver", "tin", "lead", "uranium"}) {
            for (String namespace : new String[] {"c", "forge"}) {
                for (String folder : new String[] {"item", "items"}) {
                    JsonObject tag = json("/data/" + namespace + "/tags/" + folder + "/ores/" + metal + ".json");
                    assertTrue(tag.getAsJsonArray("values").size() >= 1);
                    assertEquals(false, tag.get("replace").getAsBoolean());
                }
            }
        }
    }

    @Test
    void originalResourceOreDropsRetainVariantIdentityOnBothMcVersions() {
        var original = OriginalContentManifest.get().stackVariants("resource");
        for (String folder : new String[] {"loot_tables", "loot_table"}) {
            JsonArray pools = json("/data/ic2/" + folder + "/blocks/resource.json").getAsJsonArray("pools");
            assertEquals(original.size() + 1, pools.size());
            for (int i = 0; i < original.size(); i++) {
                JsonObject pool = pools.get(i).getAsJsonObject();
                JsonObject condition = pool.getAsJsonArray("conditions").get(0).getAsJsonObject();
                assertEquals(Integer.toString(i), condition.getAsJsonObject("properties").get("variant").getAsString());
                JsonObject function = pool.getAsJsonArray("entries").get(0).getAsJsonObject()
                        .getAsJsonArray("functions").get(0).getAsJsonObject();
                if ("loot_table".equals(folder)) {
                    assertEquals(original.get(i).key(), function.getAsJsonObject("components")
                            .getAsJsonObject("minecraft:custom_data").get("ic2ma_variant").getAsString());
                } else {
                    assertTrue(function.get("tag").getAsString().contains(original.get(i).key()));
                }
            }
            JsonObject silver = pools.get(SilverOreVariant.INDEX).getAsJsonObject();
            assertEquals(Integer.toString(SilverOreVariant.INDEX), silver.getAsJsonArray("conditions")
                    .get(0).getAsJsonObject().getAsJsonObject("properties").get("variant").getAsString());
            JsonObject function = silver.getAsJsonArray("entries").get(0).getAsJsonObject()
                    .getAsJsonArray("functions").get(0).getAsJsonObject();
            if ("loot_table".equals(folder)) {
                assertEquals(SilverOreVariant.KEY, function.getAsJsonObject("components")
                        .getAsJsonObject("minecraft:custom_data").get("ic2ma_variant").getAsString());
                assertEquals(SilverOreVariant.MODEL_DATA, function.getAsJsonObject("components")
                        .get("minecraft:custom_model_data").getAsInt());
            } else {
                assertTrue(function.get("tag").getAsString().contains(SilverOreVariant.KEY));
            }
        }
    }

    private static void verifyOre(String name, int size, int count, int min, int max,
                                  String distribution, String stoneVariant) {
        JsonObject config = json(ROOT + "worldgen/configured_feature/" + name + "_ore.json")
                .getAsJsonObject("config");
        assertEquals(size, config.get("size").getAsInt());
        JsonArray targets = config.getAsJsonArray("targets");
        assertEquals(2, targets.size());
        JsonObject stoneState = targets.get(0).getAsJsonObject().getAsJsonObject("state");
        assertEquals("ic2:resource", stoneState.get("Name").getAsString());
        assertEquals(stoneVariant, stoneState.getAsJsonObject("Properties").get("variant").getAsString());
        assertEquals("ic2_modern_adapter:deepslate_" + name + "_ore",
                targets.get(1).getAsJsonObject().getAsJsonObject("state").get("Name").getAsString());
        JsonArray placement = json(ROOT + "worldgen/placed_feature/" + name + "_ore.json")
                .getAsJsonArray("placement");
        assertEquals(count, placement.get(0).getAsJsonObject().get("count").getAsInt());
        JsonObject height = placement.get(2).getAsJsonObject().getAsJsonObject("height");
        assertEquals("minecraft:" + distribution, height.get("type").getAsString());
        assertEquals(min, height.getAsJsonObject("min_inclusive").get("absolute").getAsInt());
        assertEquals(max, height.getAsJsonObject("max_inclusive").get("absolute").getAsInt());
    }

    private static JsonObject json(String path) {
        try (InputStream stream = OreWorldgenParityTest.class.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing datapack resource: " + path);
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                return parsed.getAsJsonObject();
            }
        } catch (java.io.IOException exception) {
            throw new AssertionError("Failed to read " + path, exception);
        }
    }
}
