package com.shipovskijkorp.ic2modernadapter.content;

import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Shared JEI-facing subtype data for original IC2 metadata/NBT item families. */
public final class LegacyJeiSubtypes {
    private static final String NONE = "";
    private static final List<String> ITEM_PATHS_WITH_SUBTYPES = OriginalContentManifest.get().stackVariants().stream()
            .map(OriginalContentManifest.StackVariant::item)
            .distinct()
            .toList();

    public interface VariantResolver {
        String variantKey(ItemStack stack);
    }

    /** Root IC2 item registry paths that carry finite original meta/NBT subtypes. */
    public static List<String> itemPathsWithSubtypes() {
        return ITEM_PATHS_WITH_SUBTYPES;
    }

    /**
     * Stable subtype identity used by JEI when comparing IC2MA ItemStacks.
     *
     * <p>Returning the original finite variant key makes stacks such as {@code te/generator},
     * {@code te/macerator} and {@code cable/copper_1} distinct even though they share the same
     * modern item registry ID.</p>
     */
    public static String subtype(ItemStack stack, VariantResolver resolver) {
        String variant = resolver.variantKey(stack);
        return variant == null || variant.isBlank() ? NONE : variant;
    }

    private LegacyJeiSubtypes() {
    }
}
