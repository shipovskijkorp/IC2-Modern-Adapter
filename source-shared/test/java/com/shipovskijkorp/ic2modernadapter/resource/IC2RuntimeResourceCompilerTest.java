package com.shipovskijkorp.ic2modernadapter.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void leavesNonIc2TextureIdsUntouched() {
        assertEquals("minecraft:block/stone",
                IC2RuntimeResourceCompiler.normalizeLegacyTextureId("minecraft:block/stone"));
        assertEquals("#layer0", IC2RuntimeResourceCompiler.normalizeLegacyTextureId("#layer0"));
    }
}
