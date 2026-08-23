package com.shipovskijkorp.ic2modernadapter.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class MachineSpecTest {
    @Test
    void matchesCanonicalStandardMachines() {
        assertMachine(MachineSpec.COMPRESSOR, "compressor", "te/compressor", 42, 43,
                MachineSpec.ProgressStyle.TRIANGLE);
        assertMachine(MachineSpec.MACERATOR, "macerator", "te/macerator", 46, 47,
                MachineSpec.ProgressStyle.CRUSH);
        assertEquals(2, MachineSpec.values().length);
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
            MachineSpec.ProgressStyle progressStyle) {
        assertEquals(path, spec.blockEntityPath());
        assertEquals(variantKey, spec.variantKey());
        assertEquals(variantIndex, spec.variantIndex());
        assertEquals(legacyMeta, spec.legacyMeta());
        assertEquals(1, spec.tier());
        assertEquals(2L, spec.euPerTick());
        assertEquals(600L, spec.capacityEu());
        assertEquals(300, spec.operationTicks());
        assertEquals(progressStyle, spec.progressStyle());
    }
}
