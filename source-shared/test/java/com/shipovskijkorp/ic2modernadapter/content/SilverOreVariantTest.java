package com.shipovskijkorp.ic2modernadapter.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.shipovskijkorp.ic2modernadapter.worldgen.SilverOreVariant;
import org.junit.jupiter.api.Test;

/** The extension must never shift existing 2.8.222 resource subtype IDs. */
class SilverOreVariantTest {
    @Test
    void preservesOriginalIndicesAndAddsSilverAtTheEnd() {
        var originals = OriginalContentManifest.get().stackVariants("resource");
        assertEquals(14, originals.size());
        assertEquals(15, SilverOreVariant.variantCount("resource", originals.size()));
        assertEquals(14, SilverOreVariant.variantCount("cable", 14));
        for (int index = 0; index < originals.size(); index++) {
            assertEquals(originals.get(index).key(), SilverOreVariant.keyAt("resource", index));
        }
        assertEquals("resource/silver_ore", SilverOreVariant.keyAt("resource", 14));
        assertNull(SilverOreVariant.keyAt("resource", 15));
        assertEquals("ic2.resource.silver_ore", OriginalTranslationKeys.itemDescriptionId(
                "resource", SilverOreVariant.KEY));
    }
}
