package com.shipovskijkorp.ic2modernadapter.content;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OriginalTranslationKeysTest {
    @Test
    void mapsRepresentativeLegacySubtypeKeys() {
        assertEquals("ic2.te.macerator", OriginalTranslationKeys.itemDescriptionId("te", "te/macerator"));
        assertEquals("ic2.cable.copper_cable_1",
                OriginalTranslationKeys.itemDescriptionId("cable", "cable/copper_1"));
        assertEquals("ic2.cable.glass_cable",
                OriginalTranslationKeys.itemDescriptionId("cable", "cable/glass_0"));
        assertEquals("ic2.pipe.bronze_pipe_tiny",
                OriginalTranslationKeys.itemDescriptionId("pipe", "pipe/bronze_tiny"));
        assertEquals("ic2.fluid_cell",
                OriginalTranslationKeys.itemDescriptionId("fluid_cell", "fluid_cell/coolant"));
        assertEquals("ic2.painter", OriginalTranslationKeys.itemDescriptionId("painter", "painter/blank"));
    }

    @Test
    void everyRegisteredItemAndFiniteVariantHasAStableDescriptionId() {
        OriginalContentManifest manifest = OriginalContentManifest.get();
        for (String item : manifest.registries().items()) {
            String key = assertDoesNotThrow(() -> OriginalTranslationKeys.itemDescriptionId(item, null));
            assertTrue(key.startsWith("ic2."), key);
        }
        for (OriginalContentManifest.StackVariant variant : manifest.stackVariants()) {
            String key = assertDoesNotThrow(() ->
                    OriginalTranslationKeys.itemDescriptionId(variant.item(), variant.key()));
            assertTrue(key.startsWith("ic2."), key);
        }
    }

    @Test
    void invalidUserVariantDataFallsBackInsteadOfCrashing() {
        assertEquals("ic2.drill", OriginalTranslationKeys.itemDescriptionId("drill", "bogus/value"));
    }
}
