package com.shipovskijkorp.ic2modernadapter.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GeneratorFuelHooksTest {
    @Test
    void restoresOriginalIc2ExtraVanillaFuelValues() {
        // IC2's IFuelHandler registered both at 50 furnace ticks; Generator divides by four.
        assertEquals(12, GeneratorFuelRules.getFuelTicks("minecraft:sugar_cane", null, 0));
        assertEquals(12, GeneratorFuelRules.getFuelTicks("minecraft:cactus", null, 0));
    }

    @Test
    void restoresOriginalIc2SpecificFuelValues() {
        assertEquals(20, GeneratorFuelRules.getFuelTicks("ic2:sapling", null, 0));
        assertEquals(87, GeneratorFuelRules.getFuelTicks("ic2:misc_resource", "crafting/scrap", 0));
        assertEquals(787, GeneratorFuelRules.getFuelTicks("ic2:misc_resource", "crafting/scrap_box", 0));
    }

    @Test
    void basicGeneratorRejectsVanillaLavaBucketEvenIfFurnaceAcceptsIt() {
        assertEquals(0, GeneratorFuelRules.getFuelTicks("minecraft:lava_bucket", null, 20_000));
    }

    @Test
    void ordinaryFurnaceFuelStillUsesQuarterBurnTime() {
        assertEquals(400, GeneratorFuelRules.getFuelTicks("minecraft:coal", null, 1_600));
        assertEquals(0, GeneratorFuelRules.getFuelTicks("minecraft:stone", null, -1));
    }
}
