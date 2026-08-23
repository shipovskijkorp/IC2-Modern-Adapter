package com.shipovskijkorp.ic2modernadapter.furnace;

import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantFacingBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Implemented IC2 furnace-like te variants. */
public enum FurnaceSpec {
    IRON("iron_furnace", "te/iron_furnace", 0L, 160, 0L, 0),
    ELECTRIC("electric_furnace", "te/electric_furnace", 3L, 100, 300L, 1),
    INDUCTION("induction_furnace", "te/induction_furnace", 15L, 4000, 10_000L, 2);

    public static final int INDUCTION_MAX_HEAT = 10_000;
    public static final int INDUCTION_HEATUP_EU_PER_TICK = 1;
    public static final int INDUCTION_COOLDOWN_PER_TICK = 4;

    private final String blockEntityPath;
    private final String variantKey;
    private final long euPerTick;
    private final int operationTicks;
    private final long capacityEu;
    private final int tier;
    private final int variantIndex;
    private final int legacyMeta;

    FurnaceSpec(String blockEntityPath, String variantKey, long euPerTick, int operationTicks, long capacityEu, int tier) {
        this.blockEntityPath = blockEntityPath;
        this.variantKey = variantKey;
        this.euPerTick = euPerTick;
        this.operationTicks = operationTicks;
        this.capacityEu = capacityEu;
        this.tier = tier;
        OriginalContentManifest manifest = OriginalContentManifest.get();
        this.variantIndex = manifest.stackVariantIndex(variantKey);
        this.legacyMeta = manifest.stackVariant(variantKey).legacyMeta();
    }

    public String blockEntityPath() {
        return blockEntityPath;
    }

    public String variantKey() {
        return variantKey;
    }

    public long euPerTick() {
        return euPerTick;
    }

    public int operationTicks() {
        return operationTicks;
    }

    public long capacityEu() {
        return capacityEu;
    }

    public int tier() {
        return tier;
    }

    public int variantIndex() {
        return variantIndex;
    }

    public int legacyMeta() {
        return legacyMeta;
    }

    public String translationKey() {
        return "ic2.te." + blockEntityPath;
    }

    public static FurnaceSpec fromBlockEntityPath(String path) {
        for (FurnaceSpec spec : values()) {
            if (spec.blockEntityPath.equals(path)) {
                return spec;
            }
        }
        return null;
    }

    public static FurnaceSpec fromBlockState(BlockState state) {
        if (state == null || !state.hasProperty(LegacyVariantFacingBlock.VARIANT)) {
            return null;
        }
        int variant = state.getValue(LegacyVariantFacingBlock.VARIANT);
        for (FurnaceSpec spec : values()) {
            if (spec.variantIndex == variant) {
                return spec;
            }
        }
        return null;
    }

    public static boolean isFurnaceVariantIndex(int variant) {
        for (FurnaceSpec spec : values()) {
            if (spec.variantIndex == variant) {
                return true;
            }
        }
        return false;
    }
}
