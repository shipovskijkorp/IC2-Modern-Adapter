package com.shipovskijkorp.ic2modernadapter.content.item;

import com.shipovskijkorp.ic2modernadapter.content.OriginalTranslationKeys;
import com.shipovskijkorp.ic2modernadapter.content.VanillaBasalt;
import net.minecraft.world.item.Items;
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
        if ("resource".equals(itemPath)) return Items.BASALT.getDescriptionId();
        return OriginalTranslationKeys.itemDescriptionId(itemPath, null);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        String key = variantResolver.apply(stack);
        if ("resource".equals(itemPath) && (key == null || VanillaBasalt.LEGACY_KEY.equals(key))) {
            return Items.BASALT.getDescriptionId();
        }
        return OriginalTranslationKeys.itemDescriptionId(itemPath, key);
    }
}
