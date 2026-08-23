package com.shipovskijkorp.ic2modernadapter.energy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.shipovskijkorp.ic2modernadapter.energy.cable.EuCableVariant;
import org.junit.jupiter.api.Test;

class EuCableVariantTest {
    @Test
    void matchesAllFourteenOriginalFiniteCableVariants() {
        EuCableVariant.validateManifestNbt();
        assertEquals(14, EuCableVariant.values().length);
        assertEquals(128, EuCableVariant.COPPER_1.capacity());
        assertEquals(32, EuCableVariant.TIN_1.capacity());
        assertEquals(512, EuCableVariant.GOLD_2.capacity());
        assertEquals(2048, EuCableVariant.IRON_3.capacity());
        assertEquals(8192, EuCableVariant.GLASS_0.capacity());
        assertEquals(8192, EuCableVariant.DETECTOR_0.capacity());
        assertEquals(8192, EuCableVariant.SPLITTER_0.capacity());
    }

    @Test
    void insulationStripsWithinTheSameCableFamily() {
        assertSame(EuCableVariant.COPPER_0, EuCableVariant.COPPER_1.withoutOneInsulationLayer());
        assertSame(EuCableVariant.GOLD_1, EuCableVariant.GOLD_2.withoutOneInsulationLayer());
        assertSame(EuCableVariant.IRON_2, EuCableVariant.IRON_3.withoutOneInsulationLayer());
        assertSame(EuCableVariant.TIN_0, EuCableVariant.TIN_1.withoutOneInsulationLayer());
        assertSame(EuCableVariant.GLASS_0, EuCableVariant.GLASS_0.withoutOneInsulationLayer());
    }

    @Test
    void preservesReferenceLossValues() {
        assertEquals(0.2, EuCableVariant.COPPER_0.loss());
        assertEquals(0.2, EuCableVariant.TIN_0.loss());
        assertEquals(0.4, EuCableVariant.GOLD_0.loss());
        assertEquals(0.8, EuCableVariant.IRON_0.loss());
        assertEquals(0.025, EuCableVariant.GLASS_0.loss());
        assertEquals(0.5, EuCableVariant.DETECTOR_0.loss());
    }
}
