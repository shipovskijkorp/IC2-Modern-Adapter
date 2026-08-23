package com.shipovskijkorp.ic2modernadapter.generator;

/** Canonical IndustrialCraft 2 Experimental basic generator values. */
public final class GeneratorConstants {
    public static final String VARIANT_KEY = "te/generator";
    /** Zero-based subtype index of te/generator inside the modern ic2:te blockstate. */
    public static final int VARIANT_INDEX = 2;
    /** Original 1.12 metadata identity retained in the compatibility manifest. */
    public static final int LEGACY_META = 3;
    public static final int TIER = 1;
    public static final long CAPACITY_EU = 4_000L;
    public static final long PRODUCTION_EU_PER_TICK = 10L;
    public static final int FUEL_DIVISOR = 4;

    private GeneratorConstants() {
    }
}
