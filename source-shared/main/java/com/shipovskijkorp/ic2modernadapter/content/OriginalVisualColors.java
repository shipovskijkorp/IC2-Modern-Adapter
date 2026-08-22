package com.shipovskijkorp.ic2modernadapter.content;

import java.util.List;

/** Original IC2 tint multipliers required by models used by the visual placeholder milestone. */
public final class OriginalVisualColors {
    public static final int WHITE = 0xFFFFFFFF;
    public static final int RUBBER_LEAVES = 0xFF669944;

    private static final OriginalContentManifest MANIFEST = OriginalContentManifest.get();
    private static final List<OriginalContentManifest.StackVariant> TE_VARIANTS = MANIFEST.stackVariants("te");

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

    private OriginalVisualColors() {
    }
}
