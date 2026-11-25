package com.shipovskijkorp.ic2modernadapter.worldgen;

import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;

/**
 * Silver is an extension of the original ic2:resource family, not an alteration of the
 * immutable IC2 2.8.222 reference manifest. No original subtype indices are shifted.
 */
public final class SilverOreVariant {
    public static final String KEY = "resource/silver_ore";
    public static final int INDEX = 14; // After the 14 original resource variants (0..13).
    public static final int LEGACY_META = 16; // IC2's last resource meta is 15.
    public static final int MODEL_DATA = INDEX + 1;

    public static int variantCount(String blockPath, int originalCount) {
        if (!"resource".equals(blockPath)) return originalCount;
        if (originalCount != INDEX) {
            throw new IllegalStateException("Reference ic2:resource variant order changed");
        }
        return originalCount + 1;
    }

    /** The same state-index to item-variant mapping is used for pick-block and placement. */
    public static String keyAt(String blockPath, int index) {
        var originals = OriginalContentManifest.get().stackVariants(blockPath);
        if (index >= 0 && index < originals.size()) return originals.get(index).key();
        if ("resource".equals(blockPath) && index == INDEX) return KEY;
        return null;
    }

    private SilverOreVariant() {}
}
