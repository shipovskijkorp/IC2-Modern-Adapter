package com.shipovskijkorp.ic2modernadapter.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class MachineSpecTest {
    @Test
    void matchesCanonicalImplementedMachines() {
        assertMachine(MachineSpec.CANNER, "canner", "te/canner", 41, 42,
                4L, 200, 800L, 1, MachineSpec.ProgressStyle.CANNING, MachineSpec.Kind.CANNER);
        assertMachine(MachineSpec.COMPRESSOR, "compressor", "te/compressor", 42, 43,
                2L, 300, 600L, 1, MachineSpec.ProgressStyle.TRIANGLE, MachineSpec.Kind.STANDARD);
        assertMachine(MachineSpec.EXTRACTOR, "extractor", "te/extractor", 44, 45,
                2L, 300, 600L, 1, MachineSpec.ProgressStyle.DROP, MachineSpec.Kind.STANDARD);
        assertMachine(MachineSpec.MACERATOR, "macerator", "te/macerator", 46, 47,
                2L, 300, 600L, 1, MachineSpec.ProgressStyle.CRUSH, MachineSpec.Kind.STANDARD);
        assertMachine(MachineSpec.METAL_FORMER, "metal_former", "te/metal_former", 54, 55,
                10L, 200, 2_000L, 1, MachineSpec.ProgressStyle.METAL_FORMER, MachineSpec.Kind.METAL_FORMER);
        assertMachine(MachineSpec.ORE_WASHING_PLANT, "ore_washing_plant", "te/ore_washing_plant", 55, 56,
                16L, 500, 8_000L, 1, MachineSpec.ProgressStyle.ORE_WASHING, MachineSpec.Kind.ORE_WASHING);
        assertMachine(MachineSpec.SOLID_CANNER, "solid_canner", "te/solid_canner", 48, 49,
                2L, 200, 400L, 1, MachineSpec.ProgressStyle.CANNING, MachineSpec.Kind.SOLID_CANNER);
        assertMachine(MachineSpec.THERMAL_CENTRIFUGE, "centrifuge", "te/centrifuge", 51, 52,
                48L, 500, 10_000L, 2, MachineSpec.ProgressStyle.CENTRIFUGE, MachineSpec.Kind.THERMAL_CENTRIFUGE);
        assertEquals(8, MachineSpec.values().length);
    }

    @Test
    void resolvesOriginalBlockEntityPaths() {
        for (MachineSpec spec : MachineSpec.values()) {
            assertSame(spec, MachineSpec.fromBlockEntityPath(spec.blockEntityPath()));
            assertSame(spec, MachineSpec.fromVariantKey(spec.variantKey()));
        }
        assertNull(MachineSpec.fromBlockEntityPath("generator"));
        assertNull(MachineSpec.fromVariantKey("te/batbox"));
    }

    private static void assertMachine(
            MachineSpec spec,
            String path,
            String variantKey,
            int variantIndex,
            int legacyMeta,
            long euPerTick,
            int operationTicks,
            long capacityEu,
            int tier,
            MachineSpec.ProgressStyle progressStyle,
            MachineSpec.Kind kind) {
        assertEquals(path, spec.blockEntityPath());
        assertEquals(variantKey, spec.variantKey());
        assertEquals(variantIndex, spec.variantIndex());
        assertEquals(legacyMeta, spec.legacyMeta());
        assertEquals(tier, spec.tier());
        assertEquals(euPerTick, spec.euPerTick());
        assertEquals(capacityEu, spec.capacityEu());
        assertEquals(operationTicks, spec.operationTicks());
        assertEquals(progressStyle, spec.progressStyle());
        assertEquals(kind, spec.kind());
    }
}
