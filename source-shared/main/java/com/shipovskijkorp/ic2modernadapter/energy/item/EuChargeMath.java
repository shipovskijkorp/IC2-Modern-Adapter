package com.shipovskijkorp.ic2modernadapter.energy.item;

/** Integral EU arithmetic shared by all supported loaders (the existing network uses long EU). */
public final class EuChargeMath {
    private EuChargeMath() {}
    public static long clamp(long stored, long capacity) {
        return Math.max(0L, Math.min(Math.max(0L, capacity), stored));
    }
    public static long charge(long stored, long capacity, long amount) {
        return Math.min(Math.max(0L, amount), Math.max(0L, capacity) - clamp(stored, capacity));
    }
    public static long discharge(long stored, long capacity, long amount) {
        return Math.min(Math.max(0L, amount), clamp(stored, capacity));
    }
    public static long budget(long requested, long limit, int itemTier, int machineTier, boolean ignoreLimit) {
        if (requested <= 0 || machineTier < itemTier) return 0;
        return ignoreLimit ? requested : Math.min(requested, Math.max(0L, limit));
    }
    /** IC2 battery's five visual levels, rounded to the closest quarter. */
    public static int modelLevel(long charge, long capacity) {
        if (capacity <= 0) return 0;
        int damage = 26 - (int)(26.0 * clamp(charge, capacity) / capacity);
        return 4 - (damage * 4 + 13) / 26;
    }
}
