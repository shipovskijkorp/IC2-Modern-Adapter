package com.shipovskijkorp.ic2modernadapter.content.item;

import com.shipovskijkorp.ic2modernadapter.content.OriginalTranslationKeys;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Basic inert item whose display name is resolved through the original IC2 language keys. */
public class LegacyTranslatedItem extends Item {
    private final String itemPath;
    private final Function<ItemStack, String> variantResolver;

    public LegacyTranslatedItem(
            String itemPath, Properties properties, Function<ItemStack, String> variantResolver) {
        super(properties);
        this.itemPath = Objects.requireNonNull(itemPath, "itemPath");
        this.variantResolver = Objects.requireNonNull(variantResolver, "variantResolver");
    }

    @Override
    public String getDescriptionId() {
        return OriginalTranslationKeys.itemDescriptionId(itemPath, null);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return OriginalTranslationKeys.itemDescriptionId(itemPath, variantResolver.apply(stack));
    }
}
