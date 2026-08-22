package com.shipovskijkorp.ic2modernadapter.content;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/** Builds the legacy identity payload carried by modern IC2MA ItemStacks. */
public final class LegacyVariantNbt {
    /** Modern replacement for the removed 1.12 ItemStack damage/meta identity channel. */
    public static final String LEGACY_META_KEY = "ic2ma_legacy_meta";
    /** Stable adapter-side identity used by later model/behavior dispatch. */
    public static final String VARIANT_KEY = "ic2ma_variant";

    public static CompoundTag build(OriginalContentManifest.StackVariant variant) {
        CompoundTag tag = new CompoundTag();
        if (variant.legacyMeta() >= 0) {
            tag.putInt(LEGACY_META_KEY, variant.legacyMeta());
        }
        tag.putString(VARIANT_KEY, variant.key());

        for (OriginalContentManifest.NbtEntry entry : variant.nbt()) {
            put(tag, entry);
        }
        return tag;
    }

    private static void put(CompoundTag root, OriginalContentManifest.NbtEntry entry) {
        String[] path = entry.path().split("\\.");
        CompoundTag parent = root;
        for (int i = 0; i < path.length - 1; i++) {
            String part = path[i];
            CompoundTag child;
            if (parent.contains(part, Tag.TAG_COMPOUND)) {
                child = parent.getCompound(part);
            } else {
                child = new CompoundTag();
                parent.put(part, child);
            }
            parent = child;
        }

        String key = path[path.length - 1];
        switch (entry.type()) {
            case "byte" -> parent.putByte(key, Byte.parseByte(entry.value()));
            case "int" -> parent.putInt(key, Integer.parseInt(entry.value()));
            case "string" -> parent.putString(key, entry.value());
            default -> throw new IllegalArgumentException("Unsupported reference NBT type: " + entry.type());
        }
    }

    private LegacyVariantNbt() {
    }
}
