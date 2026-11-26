package com.shipovskijkorp.ic2modernadapter.content;

/** Retired IC2 subtype: keep its index reserved so existing resource blocks do not shift. */
public final class VanillaBasalt {
    public static final String LEGACY_KEY = "resource/basalt";
    public static final int LEGACY_INDEX = 0;
    public static final String ITEM_ID = "minecraft:basalt";
    public static final String ITEM_TOKEN = "item:" + ITEM_ID;
    public static final String BLOCK_MODEL = "minecraft:block/basalt";

    public static boolean isLegacyState(String blockPath, int index) {
        return "resource".equals(blockPath) && index == LEGACY_INDEX;
    }

    public static boolean isLegacyReference(String id, String subtype, String meta) {
        return "ic2:resource".equals(id) && ("basalt".equals(subtype)
                || (subtype == null && (meta == null || "0".equals(meta))));
    }

    private VanillaBasalt() {}
}
