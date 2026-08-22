package com.shipovskijkorp.ic2modernadapter.content.item;

import com.shipovskijkorp.ic2modernadapter.content.OriginalTranslationKeys;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** BlockItem counterpart of {@link LegacyTranslatedItem}. */
public class LegacyTranslatedBlockItem extends BlockItem {
    private final String itemPath;
    private final Function<ItemStack, String> variantResolver;

    public LegacyTranslatedBlockItem(
            String itemPath,
            Block block,
            Properties properties,
            Function<ItemStack, String> variantResolver) {
        super(block, properties);
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
