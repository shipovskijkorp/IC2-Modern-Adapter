package com.shipovskijkorp.ic2modernadapter.generator;

/** Pure, loader-neutral fuel rules for the basic IndustrialCraft 2 Generator. */
final class GeneratorFuelRules {
    private static final String LAVA_BUCKET_ID = "minecraft:lava_bucket";
    private static final String IC2_SAPLING_ID = "ic2:sapling";
    private static final String SUGAR_CANE_ID = "minecraft:sugar_cane";
    private static final String CACTUS_ID = "minecraft:cactus";
    private static final String SCRAP_VARIANT = "crafting/scrap";
    private static final String SCRAP_BOX_VARIANT = "crafting/scrap_box";

    private static final int IC2_SAPLING_BURN_TIME = 80;
    private static final int SUGAR_CANE_BURN_TIME = 50;
    private static final int CACTUS_BURN_TIME = 50;
    private static final int SCRAP_BURN_TIME = 350;
    private static final int SCRAP_BOX_BURN_TIME = 3_150;

    static int getFuelTicks(String itemId, String variantKey, int fallbackBurnTime) {
        if (LAVA_BUCKET_ID.equals(itemId)) {
            return 0;
        }

        int burnTime = referenceBurnTime(itemId, variantKey, fallbackBurnTime);
        return Math.max(0, burnTime) / GeneratorConstants.FUEL_DIVISOR;
    }

    private static int referenceBurnTime(String itemId, String variantKey, int fallbackBurnTime) {
        if (IC2_SAPLING_ID.equals(itemId)) {
            return IC2_SAPLING_BURN_TIME;
        }
        if (SUGAR_CANE_ID.equals(itemId)) {
            return SUGAR_CANE_BURN_TIME;
        }
        if (CACTUS_ID.equals(itemId)) {
            return CACTUS_BURN_TIME;
        }
        if (SCRAP_VARIANT.equals(variantKey)) {
            return SCRAP_BURN_TIME;
        }
        if (SCRAP_BOX_VARIANT.equals(variantKey)) {
            return SCRAP_BOX_BURN_TIME;
        }

        return Math.max(0, fallbackBurnTime);
    }

    private GeneratorFuelRules() {
    }
}
