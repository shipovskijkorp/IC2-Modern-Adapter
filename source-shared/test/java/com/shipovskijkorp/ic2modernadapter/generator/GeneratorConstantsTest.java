package com.shipovskijkorp.ic2modernadapter.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import com.shipovskijkorp.ic2modernadapter.energy.util.EuUtil;
import org.junit.jupiter.api.Test;

class GeneratorConstantsTest {
    @Test
    void matchesIc2ExperimentalBasicGenerator() {
        assertEquals("te/generator", GeneratorConstants.VARIANT_KEY);
        assertEquals(2, GeneratorConstants.VARIANT_INDEX);
        assertEquals(3, GeneratorConstants.LEGACY_META);
        assertEquals(1, GeneratorConstants.TIER);
        assertEquals(32L, EuUtil.powerFromTier(GeneratorConstants.TIER));
        assertEquals(4_000L, GeneratorConstants.CAPACITY_EU);
        assertEquals(10L, GeneratorConstants.PRODUCTION_EU_PER_TICK);
        assertEquals(4, GeneratorConstants.FUEL_DIVISOR);
    }

    @Test
    void generatorVariantKeepsOriginalTeIdentity() {
        OriginalContentManifest.StackVariant generator =
                OriginalContentManifest.get().stackVariant(GeneratorConstants.VARIANT_KEY);
        assertEquals("te", generator.item());
        assertEquals(GeneratorConstants.VARIANT_INDEX,
                OriginalContentManifest.get().stackVariantIndex(GeneratorConstants.VARIANT_KEY));
        assertEquals(GeneratorConstants.LEGACY_META, generator.legacyMeta());
    }
}
