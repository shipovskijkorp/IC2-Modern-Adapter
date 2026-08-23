package com.shipovskijkorp.ic2modernadapter.content.item.armor;

import com.shipovskijkorp.ic2modernadapter.content.OriginalTranslationKeys;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

public class TranslatedArmorItem extends ArmorItem {
    private final String itemPath;
    private final Function<ItemStack, String> variantResolver;

    public TranslatedArmorItem(String itemPath, ArmorMaterial material, Type type,
            Properties properties, Function<ItemStack, String> variantResolver) {
        super(material, type, properties);
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
