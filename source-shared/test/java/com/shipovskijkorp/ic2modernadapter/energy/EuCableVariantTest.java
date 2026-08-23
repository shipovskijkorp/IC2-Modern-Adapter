package com.shipovskijkorp.ic2modernadapter.energy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertSame(EuCableVariant.COPPER_1, EuCableVariant.COPPER_0.withOneInsulationLayer());
        assertSame(EuCableVariant.GOLD_2, EuCableVariant.GOLD_1.withOneInsulationLayer());
        assertSame(EuCableVariant.IRON_3, EuCableVariant.IRON_3.withOneInsulationLayer());
    }

    @Test
    void resolvesOriginalVariantKeysAndVisualGeometry() {
        assertSame(EuCableVariant.COPPER_0, EuCableVariant.fromVariantKey("cable/copper_0"));
        assertSame(EuCableVariant.SPLITTER_0, EuCableVariant.fromVariantKey("cable/splitter_0"));
        assertNull(EuCableVariant.fromVariantKey("te/generator"));

        assertEquals(0.25F, EuCableVariant.COPPER_0.visualWidth());
        assertEquals(0.375F, EuCableVariant.COPPER_1.visualWidth());
        assertEquals(0.4375F, EuCableVariant.GOLD_2.visualWidth());
        assertEquals(0.75F, EuCableVariant.IRON_3.visualWidth());
        assertEquals("copper_cable_1_black", EuCableVariant.COPPER_1.blockModelStem(false));
        assertEquals("glass_cable_black", EuCableVariant.GLASS_0.blockModelStem(false));
        assertEquals("detector_cable_active", EuCableVariant.DETECTOR_0.blockModelStem(true));
        assertEquals("splitter_cable_active", EuCableVariant.SPLITTER_0.blockModelStem(true));
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
