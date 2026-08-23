package com.shipovskijkorp.ic2modernadapter.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shipovskijkorp.ic2modernadapter.energy.cable.EuCableVariant;
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
    void leavesNonIc2TextureIdsUntouched() {
        assertEquals("minecraft:block/stone",
                IC2RuntimeResourceCompiler.normalizeLegacyTextureId("minecraft:block/stone"));
        assertEquals("#layer0", IC2RuntimeResourceCompiler.normalizeLegacyTextureId("#layer0"));
    }
}
