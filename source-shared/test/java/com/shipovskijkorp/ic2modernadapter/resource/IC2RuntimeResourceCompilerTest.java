package com.shipovskijkorp.ic2modernadapter.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shipovskijkorp.ic2modernadapter.energy.cable.EuCableVariant;
import com.shipovskijkorp.ic2modernadapter.content.OriginalVisualColors;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IC2RuntimeResourceCompilerTest {
    @Test
    void rewritesLegacyPluralTextureIdsToModernAtlasPaths() {
        assertEquals("ic2:block/fence_iron",
                IC2RuntimeResourceCompiler.normalizeLegacyTextureId("ic2:blocks/fence_iron"));
        assertEquals("ic2:item/armor/advanced_batpack",
                IC2RuntimeResourceCompiler.normalizeLegacyTextureId("ic2:items/armor/advanced_batpack"));
    }

    @Test
    void mapsLegacyTextureFilesToModernAtlasDirectories() {
        assertEquals("textures/block/fluid/uu_matter_still.png",
                IC2RuntimeResourceCompiler.normalizeLegacyTexturePath(
                        "textures/blocks/fluid/uu_matter_still.png"));
        assertEquals("textures/item/armor/advanced_batpack.png",
                IC2RuntimeResourceCompiler.normalizeLegacyTexturePath(
                        "textures/items/armor/advanced_batpack.png"));
    }

    @Test
    void resolvesLegacyAndModernTextureIdsAgainstCompiledPaths() {
        assertEquals("textures/block/fluid/uu_matter_still.png",
                IC2RuntimeResourceCompiler.textureResourcePath(
                        "ic2:blocks/fluid/uu_matter_still"));
        assertEquals("textures/block/fluid/uu_matter_still.png",
                IC2RuntimeResourceCompiler.textureResourcePath(
                        "ic2:block/fluid/uu_matter_still"));
        assertEquals("textures/item/armor/advanced_batpack.png",
                IC2RuntimeResourceCompiler.textureResourcePath(
                        "ic2:items/armor/advanced_batpack"));
    }

    @Test
    void generatesCableMultipartModelsFromOriginalTextures() {
        Map<String, byte[]> output = new LinkedHashMap<>();
        for (EuCableVariant cable : EuCableVariant.values()) {
            for (boolean active : java.util.List.of(false, true)) {
                output.put(
                        "textures/block/wiring/cable/" + cable.blockModelStem(active) + ".png",
                        new byte[] {1});
            }
        }

        IC2RuntimeResourceCompiler.compileCableBlockstate(output);

        JsonObject blockstate = JsonParser.parseString(new String(
                        output.get("blockstates/cable.json"), StandardCharsets.UTF_8))
                .getAsJsonObject();
        assertFalse(blockstate.has("variants"));
        assertTrue(blockstate.has("multipart"));
        assertEquals(EuCableVariant.values().length * 2 * 7,
                blockstate.getAsJsonArray("multipart").size());
        assertTrue(output.containsKey(
                "models/block/ic2ma_generated/cable/0/idle/center.json"));
        assertTrue(output.containsKey(
                "models/block/ic2ma_generated/cable/13/active/east.json"));
    }

    @Test
    void publishesSilverAsAnIc2ResourceBlockModel() throws java.io.IOException {
        Map<String, byte[]> output = new LinkedHashMap<>();
        IC2RuntimeResourceCompiler.compileSilverOreAssets(output);
        assertTrue(output.get("textures/block/ic2ma_generated/silver_ore.png").length > 0);
        JsonObject model = JsonParser.parseString(new String(
                output.get("models/block/ic2ma_generated/silver_ore.json"), StandardCharsets.UTF_8))
                .getAsJsonObject();
        assertEquals("ic2:block/ic2ma_generated/silver_ore",
                model.getAsJsonObject("textures").get("all").getAsString());
    }

    @Test
    void generatesAllRubberWoodResinStates() {
        JsonObject stateMap = new JsonObject();
        for (String axis : java.util.List.of("x", "y", "z")) {
            JsonObject descriptor = new JsonObject();
            descriptor.addProperty("model", "ic2:resource/rubber_wood");
            stateMap.add("plain_" + axis, descriptor);
        }
        for (String fill : java.util.List.of("dry", "wet")) {
            for (String direction : java.util.List.of("north", "south", "west", "east")) {
                JsonObject descriptor = new JsonObject();
                descriptor.addProperty("model", "ic2:resource/rubber_wood_" + fill);
                descriptor.addProperty("y", switch (direction) { case "east" -> 90; case "south" -> 180; case "west" -> 270; default -> 0; });
                stateMap.add(fill + "_" + direction, descriptor);
            }
        }
        JsonObject variants = new JsonObject(); variants.add("state", stateMap);
        JsonObject original = new JsonObject(); original.add("variants", variants);
        JsonObject output = IC2RuntimeResourceCompiler.compileRubberWood(original).getAsJsonObject("variants");
        assertEquals(11, output.size());
        assertTrue(output.has("axis=y,resin=plain"));
        assertEquals("ic2:block/resource/rubber_wood_wet", output.getAsJsonObject("resin=wet_east").get("model").getAsString());
        assertEquals(90, output.getAsJsonObject("resin=wet_east").get("y").getAsInt());
        assertEquals(270, output.getAsJsonObject("resin=dry_west").get("y").getAsInt());
    }

    @Test
    void normalizesVanillaLegacyTextureIds() {
        assertEquals("minecraft:block/white_wool",
                IC2RuntimeResourceCompiler.normalizeLegacyTextureId("blocks/wool_colored_white"));
        assertEquals("minecraft:block/white_wool",
                IC2RuntimeResourceCompiler.normalizeLegacyTextureId("minecraft:blocks/wool_colored_white"));
        assertEquals("minecraft:block/water_still",
                IC2RuntimeResourceCompiler.normalizeLegacyTextureId("minecraft:blocks/water_still"));
        assertEquals("minecraft:block/cauldron_side",
                IC2RuntimeResourceCompiler.normalizeLegacyTextureId("blocks/cauldron_side"));
    }

    @Test
    void repairsSheetTexturesWithoutChangingModelParents() {
        Map<String, byte[]> output = new LinkedHashMap<>();
        output.put("models/item/resource/water_sheet.json", """
                {"parent":"ic2:item/default","textures":{"layer0":"minecraft:blocks/water_still"}}
                """.getBytes(StandardCharsets.UTF_8));
        output.put("models/item/resource/lava_sheet.json", """
                {"parent":"ic2:item/default","textures":{"layer0":"minecraft:blocks/lava_still"}}
                """.getBytes(StandardCharsets.UTF_8));
        output.put("models/block/wool_sheet.json", """
                {"parent":"ic2:block/sheet_base","textures":{"texture":"blocks/wool_colored_white"}}
                """.getBytes(StandardCharsets.UTF_8));
        output.put("models/block/sheet_base.json", """
                {"textures":{"particle":"#texture"},"elements":[{"from":[0,0,0],"to":[16,2,16]}]}
                """.getBytes(StandardCharsets.UTF_8));
        byte[] originalGeometry = output.get("models/block/sheet_base.json");
        IC2RuntimeResourceCompiler.normalizeLegacyModelTextures(output);
        for (String liquid : java.util.List.of("water", "lava")) {
            JsonObject sheet = JsonParser.parseString(new String(
                    output.get("models/item/resource/" + liquid + "_sheet.json"), StandardCharsets.UTF_8))
                    .getAsJsonObject();
            assertEquals("minecraft:block/" + liquid + "_flow",
                    sheet.getAsJsonObject("textures").get("layer0").getAsString());
            assertEquals("ic2:item/default", sheet.get("parent").getAsString());
            assertFalse(sheet.has("elements"));
        }
        JsonObject wool = JsonParser.parseString(new String(
                output.get("models/block/wool_sheet.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        assertEquals("minecraft:block/white_wool", wool.getAsJsonObject("textures").get("texture").getAsString());
        assertEquals("ic2:block/sheet_base", wool.get("parent").getAsString());
        org.junit.jupiter.api.Assertions.assertArrayEquals(originalGeometry, output.get("models/block/sheet_base.json"));
    }

    @Test
    void tintsOnlyTheWaterSheetLayer() {
        assertEquals(0xFF3F76E4, OriginalVisualColors.miscResource("misc_resource/water_sheet", 0));
        assertEquals(OriginalVisualColors.WHITE, OriginalVisualColors.miscResource("misc_resource/water_sheet", 1));
        assertEquals(OriginalVisualColors.WHITE, OriginalVisualColors.miscResource("misc_resource/lava_sheet", 0));
        assertEquals(OriginalVisualColors.WHITE, OriginalVisualColors.miscResource("misc_resource/iridium_ore", 0));
        assertEquals(OriginalVisualColors.WHITE, OriginalVisualColors.miscResource(null, 0));
    }

    @Test
    void leavesModernAndVariableTextureIdsUntouched() {
        assertEquals("minecraft:block/stone",
                IC2RuntimeResourceCompiler.normalizeLegacyTextureId("minecraft:block/stone"));
        assertEquals("#layer0", IC2RuntimeResourceCompiler.normalizeLegacyTextureId("#layer0"));
    }
}
