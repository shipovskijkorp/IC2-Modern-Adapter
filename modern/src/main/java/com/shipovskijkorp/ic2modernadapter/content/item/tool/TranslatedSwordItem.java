package com.shipovskijkorp.ic2modernadapter.content.item.tool;

import com.shipovskijkorp.ic2modernadapter.content.OriginalTranslationKeys;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public final class TranslatedSwordItem extends SwordItem {
    private final String itemPath;
    private final Function<ItemStack, String> variantResolver;

    public TranslatedSwordItem(String itemPath, Tier tier, int attackDamage, float attackSpeed,
            Properties properties, Function<ItemStack, String> variantResolver) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, attackDamage, attackSpeed)));
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
