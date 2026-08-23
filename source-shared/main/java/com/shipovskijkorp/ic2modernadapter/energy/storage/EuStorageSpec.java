package com.shipovskijkorp.ic2modernadapter.energy.storage;

import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantFacingBlock;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Canonical IC2 Experimental 2.8.222 electric storage block parameters. */
public enum EuStorageSpec {
    BATBOX("batbox", "te/batbox", 74, 72, 1, 32L, 40_000L),
    CESU("cesu", "te/cesu", 75, 73, 2, 128L, 300_000L),
    MFE("mfe", "te/mfe", 76, 74, 3, 512L, 4_000_000L),
    MFSU("mfsu", "te/mfsu", 77, 75, 4, 2_048L, 40_000_000L);

    private static final Map<Integer, EuStorageSpec> BY_VARIANT = new HashMap<>();
    private static final Map<String, EuStorageSpec> BY_BLOCK_ENTITY_PATH = new HashMap<>();
    private static final Map<String, EuStorageSpec> BY_VARIANT_KEY = new HashMap<>();

    static {
        for (EuStorageSpec spec : values()) {
            if (BY_VARIANT.put(spec.variantIndex, spec) != null) {
                throw new IllegalStateException("Duplicate IC2 EU storage variant index: " + spec.variantIndex);
            }
            if (BY_BLOCK_ENTITY_PATH.put(spec.blockEntityPath, spec) != null) {
                throw new IllegalStateException("Duplicate IC2 EU storage block entity path: " + spec.blockEntityPath);
            }
            if (BY_VARIANT_KEY.put(spec.variantKey, spec) != null) {
                throw new IllegalStateException("Duplicate IC2 EU storage variant key: " + spec.variantKey);
            }
        }
    }

    private final String blockEntityPath;
    private final String variantKey;
    private final int variantIndex;
    private final int legacyMeta;
    private final int tier;
    private final long outputEuPerTick;
    private final long capacityEu;

    EuStorageSpec(
            String blockEntityPath,
            String variantKey,
            int variantIndex,
            int legacyMeta,
            int tier,
            long outputEuPerTick,
            long capacityEu) {
        this.blockEntityPath = blockEntityPath;
        this.variantKey = variantKey;
        this.variantIndex = variantIndex;
        this.legacyMeta = legacyMeta;
        this.tier = tier;
        this.outputEuPerTick = outputEuPerTick;
        this.capacityEu = capacityEu;
    }

    public String blockEntityPath() {
        return blockEntityPath;
    }

    public String variantKey() {
        return variantKey;
    }

    public int variantIndex() {
        return variantIndex;
    }

    public int legacyMeta() {
        return legacyMeta;
    }

    public int tier() {
        return tier;
    }

    public long outputEuPerTick() {
        return outputEuPerTick;
    }

    public long capacityEu() {
        return capacityEu;
    }

    public String translationKey() {
        return "ic2.te." + blockEntityPath;
    }

    public static boolean isStorage(BlockState state) {
        return fromBlockState(state) != null;
    }

    public static @Nullable EuStorageSpec fromBlockState(BlockState state) {
        if (state == null || !state.hasProperty(LegacyVariantFacingBlock.VARIANT)) {
            return null;
        }
        return BY_VARIANT.get(state.getValue(LegacyVariantFacingBlock.VARIANT));
    }

    public static @Nullable EuStorageSpec fromBlockEntityPath(String path) {
        return BY_BLOCK_ENTITY_PATH.get(path);
    }

    public static @Nullable EuStorageSpec fromVariantKey(String variantKey) {
        return BY_VARIANT_KEY.get(variantKey);
    }
}
