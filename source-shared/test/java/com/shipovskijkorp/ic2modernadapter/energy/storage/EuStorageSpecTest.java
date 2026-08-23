package com.shipovskijkorp.ic2modernadapter.energy.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.shipovskijkorp.ic2modernadapter.energy.util.EuUtil;
import org.junit.jupiter.api.Test;

class EuStorageSpecTest {
    @Test
    void matchesCanonicalIc2ExperimentalStorageFamily() {
        assertStorage(EuStorageSpec.BATBOX, "batbox", "te/batbox", 74, 72, 1, 32L, 40_000L);
        assertStorage(EuStorageSpec.CESU, "cesu", "te/cesu", 75, 73, 2, 128L, 300_000L);
        assertStorage(EuStorageSpec.MFE, "mfe", "te/mfe", 76, 74, 3, 512L, 4_000_000L);
        assertStorage(EuStorageSpec.MFSU, "mfsu", "te/mfsu", 77, 75, 4, 2_048L, 40_000_000L);
        assertEquals(4, EuStorageSpec.values().length);
    }

    @Test
    void resolvesOriginalBlockEntityPaths() {
        for (EuStorageSpec spec : EuStorageSpec.values()) {
            assertSame(spec, EuStorageSpec.fromBlockEntityPath(spec.blockEntityPath()));
        }
        assertNull(EuStorageSpec.fromBlockEntityPath("generator"));
        assertNull(EuStorageSpec.fromBlockEntityPath("macerator"));
    }

    private static void assertStorage(
            EuStorageSpec spec,
            String path,
            String variantKey,
            int variantIndex,
            int legacyMeta,
            int tier,
            long output,
            long capacity) {
        assertEquals(path, spec.blockEntityPath());
        assertEquals(variantKey, spec.variantKey());
        assertEquals(variantIndex, spec.variantIndex());
        assertEquals(legacyMeta, spec.legacyMeta());
        assertEquals(tier, spec.tier());
        assertEquals(output, spec.outputEuPerTick());
        assertEquals(capacity, spec.capacityEu());
        assertEquals(output, EuUtil.powerFromTier(tier));
    }
}
