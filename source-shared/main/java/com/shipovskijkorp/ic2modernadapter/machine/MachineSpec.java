package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantFacingBlock;
import java.util.Locale;
import net.minecraft.world.level.block.state.BlockState;

/** Canonical IC2 Experimental standard-machine definitions implemented by IC2MA. */
public enum MachineSpec {
    CANNER("canner", "te/canner", 4L, 200, 800L, 1, ProgressStyle.CANNING, Kind.CANNER),
    COMPRESSOR("compressor", "te/compressor", 2L, 300, 600L, 1, ProgressStyle.TRIANGLE, Kind.STANDARD),
    EXTRACTOR("extractor", "te/extractor", 2L, 300, 600L, 1, ProgressStyle.DROP, Kind.STANDARD),
    MACERATOR("macerator", "te/macerator", 2L, 300, 600L, 1, ProgressStyle.CRUSH, Kind.STANDARD),
    METAL_FORMER("metal_former", "te/metal_former", 10L, 200, 2_000L, 1, ProgressStyle.METAL_FORMER, Kind.METAL_FORMER),
    ORE_WASHING_PLANT("ore_washing_plant", "te/ore_washing_plant", 16L, 500, 8_000L, 1, ProgressStyle.ORE_WASHING, Kind.ORE_WASHING),
    SOLID_CANNER("solid_canner", "te/solid_canner", 2L, 200, 400L, 1, ProgressStyle.CANNING, Kind.SOLID_CANNER),
    THERMAL_CENTRIFUGE("centrifuge", "te/centrifuge", 48L, 500, 10_000L, 2, ProgressStyle.CENTRIFUGE, Kind.THERMAL_CENTRIFUGE);

    private final String blockEntityPath;
    private final String variantKey;
    private final long euPerTick;
    private final int operationTicks;
    private final long capacityEu;
    private final int tier;
    private final ProgressStyle progressStyle;
    private final Kind kind;
    private final int variantIndex;
    private final int legacyMeta;

    MachineSpec(
            String blockEntityPath,
            String variantKey,
            long euPerTick,
            int operationTicks,
            long capacityEu,
            int tier,
            ProgressStyle progressStyle,
            Kind kind) {
        this.blockEntityPath = blockEntityPath;
        this.variantKey = variantKey;
        this.euPerTick = euPerTick;
        this.operationTicks = operationTicks;
        this.capacityEu = capacityEu;
        this.tier = tier;
        this.progressStyle = progressStyle;
        this.kind = kind;
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

    public ProgressStyle progressStyle() {
        return progressStyle;
    }

    public Kind kind() {
        return kind;
    }

    public boolean isStandard() {
        return kind == Kind.STANDARD;
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

    public static MachineSpec fromBlockEntityPath(String path) {
        for (MachineSpec spec : values()) {
            if (spec.blockEntityPath.equals(path)) {
                return spec;
            }
        }
        return null;
    }

    public static MachineSpec fromVariantKey(String variantKey) {
        for (MachineSpec spec : values()) {
            if (spec.variantKey.equals(variantKey)) {
                return spec;
            }
        }
        return null;
    }

    public static MachineSpec fromBlockState(BlockState state) {
        if (state == null || !state.hasProperty(LegacyVariantFacingBlock.VARIANT)) {
            return null;
        }
        int variant = state.getValue(LegacyVariantFacingBlock.VARIANT);
        for (MachineSpec spec : values()) {
            if (spec.variantIndex == variant) {
                return spec;
            }
        }
        return null;
    }

    public static boolean isMachineVariantIndex(int variant) {
        for (MachineSpec spec : values()) {
            if (spec.variantIndex == variant) {
                return true;
            }
        }
        return false;
    }

    public String recipeIdPrefix() {
        return name().toLowerCase(Locale.ROOT);
    }

    public enum ProgressStyle {
        CRUSH,
        TRIANGLE,
        DROP,
        METAL_FORMER,
        ORE_WASHING,
        CENTRIFUGE,
        CANNING
    }

    public enum Kind {
        STANDARD,
        METAL_FORMER,
        ORE_WASHING,
        THERMAL_CENTRIFUGE,
        CANNER,
        SOLID_CANNER
    }
}
