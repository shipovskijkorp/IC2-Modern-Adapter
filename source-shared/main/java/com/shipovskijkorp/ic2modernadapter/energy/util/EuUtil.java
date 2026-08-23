package com.shipovskijkorp.ic2modernadapter.energy.util;

/** IC2 EU packet/tier math. */
public final class EuUtil {
    private EuUtil() {
    }

    /** Equivalent to IC2 EnergyNet#getPowerFromTier. */
    public static double powerFromTierD(int tier) {
        int normalizedTier = Math.max(0, tier);
        if (normalizedTier < 14) {
            return (double) (8L << (normalizedTier * 2));
        }
        double value = 8.0 * Math.pow(4.0, normalizedTier);
        return Double.isFinite(value) ? value : Double.MAX_VALUE;
    }

    public static long powerFromTier(int tier) {
        double value = powerFromTierD(tier);
        return value >= Long.MAX_VALUE ? Long.MAX_VALUE : (long) Math.floor(value);
    }

    /** Equivalent to IC2 EnergyNet#getTierFromPower. */
    public static int tierFromPower(double power) {
        if (power <= 0.0) {
            return 0;
        }
        double tier = Math.log(Math.max(1.0, power) / 8.0) / Math.log(4.0);
        return Math.max(0, (int) Math.ceil(tier));
    }

    public static int tierFromPower(long power) {
        return tierFromPower((double) power);
    }
}
