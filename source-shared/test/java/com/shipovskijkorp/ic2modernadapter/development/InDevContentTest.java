package com.shipovskijkorp.ic2modernadapter.development;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class InDevContentTest {
    @Test
    void completedEnergyVariantsLeaveDevelopmentWithoutCompletingOtherTeVariants() {
        assertTrue(InDevContent.isItem("ic2", "te"));
        for (String variant : Set.of("te/generator", "te/batbox", "te/cesu", "te/mfe", "te/mfsu")) {
            assertFalse(InDevContent.isItem("ic2", "te", variant), variant + " should be implemented");
            assertTrue(InDevContent.completedVariants().contains(variant));
        }
        assertTrue(InDevContent.isItem("ic2", "te", "te/macerator"));
        assertFalse(InDevContent.isItem("ic2", "cable"), "all fourteen cable variants are implemented");
        assertFalse(InDevContent.isItem("ic2", "cutter"), "insulation cutter is implemented");
        assertFalse(InDevContent.isItem("ic2", "forge_hammer"), "forge hammer crafting behavior is implemented");
    }
}
