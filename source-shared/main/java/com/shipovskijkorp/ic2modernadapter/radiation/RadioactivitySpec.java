package com.shipovskijkorp.ic2modernadapter.radiation;

import java.util.Map;
import java.util.Set;

/** Original IC2 radioactive inventory exposure values for item stacks. */
public record RadioactivitySpec(int durationTicks, int amplifier) {
    private static final int TICKS_PER_SECOND = 20;
    private static final RadioactivitySpec FUEL_ROD_EXPOSURE = new RadioactivitySpec(200, 100);
    private static final Set<String> RADIOACTIVE_FUEL_RODS = Set.of(
            "uranium_fuel_rod",
            "dual_uranium_fuel_rod",
            "quad_uranium_fuel_rod",
            "mox_fuel_rod",
            "dual_mox_fuel_rod",
            "quad_mox_fuel_rod");

    private static final Map<String, RadioactivitySpec> NUCLEAR_RESOURCES = Map.ofEntries(
            entry("nuclear/uranium", 60, 100),
            entry("nuclear/uranium_235", 150, 100),
            entry("nuclear/uranium_238", 10, 90),
            entry("nuclear/plutonium", 150, 100),
            entry("nuclear/mox", 300, 100),
            entry("nuclear/small_uranium_235", 150, 100),
            entry("nuclear/small_uranium_238", 10, 90),
            entry("nuclear/small_plutonium", 150, 100),
            entry("nuclear/uranium_pellet", 60, 100),
            entry("nuclear/mox_pellet", 300, 100),
            entry("nuclear/rtg_pellet", 2, 90),
            entry("nuclear/depleted_uranium", 10, 100),
            entry("nuclear/depleted_dual_uranium", 10, 100),
            entry("nuclear/depleted_quad_uranium", 10, 100),
            entry("nuclear/depleted_mox", 10, 100),
            entry("nuclear/depleted_dual_mox", 10, 100),
            entry("nuclear/depleted_quad_mox", 10, 100),
            entry("nuclear/near_depleted_uranium", 15, 100),
            entry("nuclear/re_enriched_uranium", 30, 100));

    public static RadioactivitySpec forItemStack(String itemPath, String variantKey) {
        if (RADIOACTIVE_FUEL_RODS.contains(itemPath)) {
            return FUEL_ROD_EXPOSURE;
        }
        if (!"nuclear".equals(itemPath) || variantKey == null) {
            return null;
        }
        return NUCLEAR_RESOURCES.get(variantKey);
    }

    public static Map<String, RadioactivitySpec> nuclearResources() {
        return NUCLEAR_RESOURCES;
    }

    public static Set<String> radioactiveFuelRods() {
        return RADIOACTIVE_FUEL_RODS;
    }

    private static Map.Entry<String, RadioactivitySpec> entry(String variantKey, int seconds, int amplifier) {
        return Map.entry(variantKey, new RadioactivitySpec(seconds * TICKS_PER_SECOND, amplifier));
    }
}
