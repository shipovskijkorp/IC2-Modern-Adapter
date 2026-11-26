package com.shipovskijkorp.ic2modernadapter.content.item.armor;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class NanoArmorRulesTest {
    @Test void fullSuitUsesAdditiveSlotWeightsAndAbsorbsNinetyPercent() {
        float sum = 0;
        long paid = 0;
        for (float weight : new float[]{0.15F, 0.40F, 0.30F, 0.15F}) {
            var absorption = NanoArmorRules.absorption(20, weight, 1_000_000);
            sum += absorption.damage();
            paid += absorption.eu();
        }
        assertEquals(18.0F, sum, 0.0001F);
        assertTrue(paid >= 90_000 && paid <= 90_004);
    }

    @Test void partialChargeNeverCreatesFreeProtectionOrConsumesMoreThanAvailable() {
        for (long charge : new long[]{0, 1, 199, 200, 499, 5000, 10000, 1000000}) {
            for (int damage = 1; damage <= 100; damage++) {
                for (float weight : new float[]{0.15F, 0.40F, 0.30F}) {
                    var a = NanoArmorRules.absorption(damage, weight, charge);
                    assertTrue(a.eu() >= 0 && a.eu() <= charge);
                    assertTrue(a.damage() >= 0 && a.damage() <= damage * weight * 0.9F + 0.0001F);
                    assertTrue(a.damage() <= a.eu() / 5000.0 + 0.0001);
                    if (charge < 200) assertEquals(0, a.damage(), 0.0F);
                }
            }
        }
    }

    @Test void smallFallThresholdMatchesIL() {
        assertEquals(0, NanoArmorRules.smallFallCost(3.999F));
        assertEquals(5000, NanoArmorRules.smallFallCost(4F));
        assertEquals(5000, NanoArmorRules.smallFallCost(4.999F));
        assertEquals(35000, NanoArmorRules.smallFallCost(10.999F));
        assertEquals(0, NanoArmorRules.smallFallCost(11F));
        assertEquals(0, NanoArmorRules.smallFallCost(Float.NaN));
        assertEquals(0, NanoArmorRules.smallFallCost(Float.POSITIVE_INFINITY));
    }
}
