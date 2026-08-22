package com.shipovskijkorp.ic2modernadapter.content;

import java.util.List;
import java.util.Map;

/**
 * Maps modern placeholder ItemStacks back to the translation keys used by IC2 2.8.222.
 *
 * <p>The original IC2 localization loader prepended {@code ic2.} to almost every entry from
 * {@code assets/ic2/lang_ic2/*.properties}. Keeping those keys means the runtime language
 * converter can preserve the original translations verbatim instead of maintaining a second
 * localization table in IC2 Modern Adapter.</p>
 */
public final class OriginalTranslationKeys {
    private static final OriginalContentManifest MANIFEST = OriginalContentManifest.get();

    private static final Map<String, String> ROOT_SPECIAL_CASES = Map.of(
            "barrel", "item.EmptyBoozeBarrel",
            "booze_mug", "booze_mug",
            "mug", "mug.empty",
            "crop_seed_bag", "crop.unknown",
            "sapling", "sapling.rubber");

    /** Returns the complete Minecraft translation key for an IC2 item stack. */
    public static String itemDescriptionId(String itemPath, String variantKey) {
        return "ic2." + legacyItemKey(itemPath, variantKey);
    }

    /**
     * Returns the key as it appeared in the original {@code lang_ic2/*.properties} file, before
     * IC2's old localization loader added its {@code ic2.} namespace prefix.
     */
    public static String legacyItemKey(String itemPath, String variantKey) {
        if (variantKey != null && !variantKey.isBlank()) {
            try {
                OriginalContentManifest.StackVariant variant = MANIFEST.stackVariant(variantKey);
                if (variant.item().equals(itemPath)) {
                    return legacyVariantKey(variant);
                }
            } catch (IllegalArgumentException ignored) {
                // User-editable/corrupt stack data must never crash tooltip/name resolution.
            }
        }

        List<OriginalContentManifest.StackVariant> variants = MANIFEST.stackVariants(itemPath);
        if (!variants.isEmpty()) {
            // This also mirrors the old metadata=0 fallback for the finite multi-items. Actual
            // creative stacks carry ic2ma_variant and therefore resolve their exact subtype.
            return legacyVariantKey(variants.get(0));
        }
        return ROOT_SPECIAL_CASES.getOrDefault(itemPath, itemPath);
    }

    public static String legacyVariantKey(OriginalContentManifest.StackVariant variant) {
        String item = variant.item();
        String suffix = suffix(variant.key());

        if (item.equals("painter") && suffix.equals("blank")) {
            return "painter";
        }
        if (item.equals("fluid_cell")) {
            // IC2 used one base display name and showed the contained fluid separately.
            return "fluid_cell";
        }
        if (item.equals("cable")) {
            int split = suffix.lastIndexOf('_');
            String type = suffix.substring(0, split);
            String insulation = suffix.substring(split + 1);
            return switch (type) {
                case "glass" -> "cable.glass_cable";
                case "detector", "splitter" -> "cable." + type + "_cable";
                default -> "cable." + type + "_cable_" + insulation;
            };
        }
        if (item.equals("pipe")) {
            int split = suffix.indexOf('_');
            String type = suffix.substring(0, split);
            String size = suffix.substring(split + 1);
            return "pipe." + type + "_pipe_" + size;
        }
        return item + "." + suffix;
    }

    /** One original item had a code-generated, non-localized default name. */
    public static Map<String, String> generatedCompatibilityTranslations() {
        return Map.of("ic2.booze_mug", "Zero");
    }

    private static String suffix(String variantKey) {
        int slash = variantKey.indexOf('/');
        if (slash < 0 || slash == variantKey.length() - 1) {
            throw new IllegalArgumentException("Invalid IC2 stack variant key: " + variantKey);
        }
        return variantKey.substring(slash + 1);
    }

    private OriginalTranslationKeys() {
    }
}
