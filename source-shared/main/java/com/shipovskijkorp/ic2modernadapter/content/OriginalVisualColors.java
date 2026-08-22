package com.shipovskijkorp.ic2modernadapter.content;

import java.util.List;

/** Original IC2 tint multipliers required by models used by the visual placeholder milestone. */
public final class OriginalVisualColors {
    public static final int WHITE = 0xFFFFFFFF;
    public static final int RUBBER_LEAVES = 0xFF669944;
    public static final int PIPE_BRONZE = 0xFFF05111;
    public static final int PIPE_STEEL = 0xFF808080;
    public static final int PUMP_COVER_LV = 0xFF00FF00;
    public static final int PUMP_COVER_MV = 0xFFFFFF00;

    private static final OriginalContentManifest MANIFEST = OriginalContentManifest.get();
    private static final List<OriginalContentManifest.StackVariant> TE_VARIANTS = MANIFEST.stackVariants("te");
    private static final List<OriginalContentManifest.StackVariant> PIPE_VARIANTS = MANIFEST.stackVariants("pipe");
    private static final List<OriginalContentManifest.StackVariant> COVER_VARIANTS = MANIFEST.stackVariants("cover");

    public static int te(int variantIndex) {
        if (variantIndex < 0 || variantIndex >= TE_VARIANTS.size()) {
            return WHITE;
        }
        String key = TE_VARIANTS.get(variantIndex).key();
        return switch (key) {
            case "te/wooden_storage_box" -> 0xFF9F844D;
            case "te/iron_storage_box" -> 0xFFC8C8C8;
            case "te/bronze_storage_box" -> 0xFFFF8000;
            case "te/steel_storage_box" -> 0xFF808080;
            default -> WHITE;
        };
    }

    public static int pipe(int variantIndex) {
        if (variantIndex < 0 || variantIndex >= PIPE_VARIANTS.size()) {
            return WHITE;
        }
        String key = PIPE_VARIANTS.get(variantIndex).key();
        return key.startsWith("pipe/bronze_") ? PIPE_BRONZE : PIPE_STEEL;
    }

    public static int cover(int variantIndex, int tintIndex) {
        if (tintIndex != 1 || variantIndex < 0 || variantIndex >= COVER_VARIANTS.size()) {
            return WHITE;
        }
        String key = COVER_VARIANTS.get(variantIndex).key();
        return switch (key) {
            case "cover/pump_lv" -> PUMP_COVER_LV;
            case "cover/pump_mv" -> PUMP_COVER_MV;
            default -> WHITE;
        };
    }

    private OriginalVisualColors() {
    }
}
