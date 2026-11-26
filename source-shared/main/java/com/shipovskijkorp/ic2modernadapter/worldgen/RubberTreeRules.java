package com.shipovskijkorp.ic2modernadapter.worldgen;

import java.util.function.IntUnaryOperator;

/** Constants and random rules from IC2 Experimental 2.8.222. */
public final class RubberTreeRules {
    public static final int MAX_HEIGHT = 8;
    public static final int FIRST_HOLE_CHANCE = 25;
    public static final int HOLE_CHANCE_DECREMENT = 10;
    public static final int SAPLING_GROWTH_CHANCE = 30;
    public static final int RESIN_REGENERATION_CHANCE = 7;
    public static final int DRY_TAP_CHANCE = 5;

    public static int biomeContribution(boolean swamp, boolean forestOrJungle, IntUnaryOperator random) {
        int count = 0;
        if (swamp) count += random.applyAsInt(10) + 5;
        if (forestOrJungle) count += random.applyAsInt(5) + 1;
        return count;
    }

    public static boolean shouldRefill(IntUnaryOperator random) {
        return random.applyAsInt(RESIN_REGENERATION_CHANCE) == 0;
    }

    public static int wetYield(IntUnaryOperator random) { return random.applyAsInt(3) + 1; }

    public static DryTap dryTap(IntUnaryOperator random) {
        return new DryTap(random.applyAsInt(DRY_TAP_CHANCE) == 0, random.applyAsInt(DRY_TAP_CHANCE) == 0);
    }

    public record DryTap(boolean destroySpot, boolean yieldResin) {}

    public static int treeHeight(int clearance, IntUnaryOperator random) {
        if (clearance < 2) return 0;
        return clearance - random.applyAsInt(clearance / 2 + 1);
    }

    public static boolean hasCanopy(int height, int layer) {
        return !(height >= 4 && (height >= 7 || layer <= 1) && layer <= 2);
    }

    public static boolean hasLeaf(int dx, int dz, int height, int layer, IntUnaryOperator random) {
        int chance = Math.max(1, layer + 4 - height);
        return (dx <= 1 && dz <= 1) || (dx <= 1 && random.applyAsInt(chance) == 0)
                || (dz <= 1 && random.applyAsInt(chance) == 0);
    }

    private RubberTreeRules() {}
}
