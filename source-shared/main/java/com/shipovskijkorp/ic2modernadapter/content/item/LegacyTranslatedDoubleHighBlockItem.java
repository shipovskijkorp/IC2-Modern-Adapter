package com.shipovskijkorp.ic2modernadapter.content.item;

import com.shipovskijkorp.ic2modernadapter.content.OriginalTranslationKeys;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Translation-aware placeholder item for the reinforced door. */
public final class LegacyTranslatedDoubleHighBlockItem extends DoubleHighBlockItem {
    private final String itemPath;
    private final Function<ItemStack, String> variantResolver;

    public LegacyTranslatedDoubleHighBlockItem(
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
