package com.shipovskijkorp.ic2modernadapter.worldgen;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RubberTreeRulesTest {
    @Test void biomeContributionsMatchOriginalRanges() {
        assertEquals(0, RubberTreeRules.biomeContribution(false, false, n -> { throw new AssertionError(); }));
        assertEquals(5, RubberTreeRules.biomeContribution(true, false, n -> 0));
        assertEquals(14, RubberTreeRules.biomeContribution(true, false, n -> n - 1));
        assertEquals(1, RubberTreeRules.biomeContribution(false, true, n -> 0));
        assertEquals(5, RubberTreeRules.biomeContribution(false, true, n -> n - 1));
        assertEquals(19, RubberTreeRules.biomeContribution(true, true, n -> n - 1));
    }

    @Test void treeHeightAndCanopyMatchOriginal() {
        assertEquals(0, RubberTreeRules.treeHeight(1, n -> 0));
        assertEquals(4, RubberTreeRules.treeHeight(8, n -> n - 1));
        assertEquals(8, RubberTreeRules.treeHeight(8, n -> 0));
        assertFalse(RubberTreeRules.hasCanopy(8, 2));
        assertTrue(RubberTreeRules.hasCanopy(8, 3));
        assertFalse(RubberTreeRules.hasCanopy(4, 1));
        assertTrue(RubberTreeRules.hasCanopy(4, 2));
        assertTrue(RubberTreeRules.hasCanopy(3, 0));
        assertFalse(RubberTreeRules.hasLeaf(2, 2, 8, 5, n -> 0));
        assertTrue(RubberTreeRules.hasLeaf(1, 2, 8, 5, n -> 0));
    }

    @Test void dryTapUsesIndependentRollsAndRefillHasOneInSevenChance() {
        for (int i = 0; i < 7; i++) {
            int draw = i;
            assertEquals(i == 0, RubberTreeRules.shouldRefill(n -> { assertEquals(7, n); return draw; }));
        }
        for (int i = 0; i < 3; i++) {
            int draw = i;
            assertEquals(i + 1, RubberTreeRules.wetYield(n -> draw));
        }
        for (int first = 0; first < 5; first++) {
            for (int second = 0; second < 5; second++) {
                int[] draws = {first, second}; int[] index = {0};
                var outcome = RubberTreeRules.dryTap(n -> { assertEquals(5, n); return draws[index[0]++]; });
                assertEquals(2, index[0]);
                assertEquals(first == 0, outcome.destroySpot());
                assertEquals(second == 0, outcome.yieldResin());
            }
        }
    }
}
