package com.shipovskijkorp.ic2modernadapter.content.item.armor;

import com.shipovskijkorp.ic2modernadapter.energy.item.*;
import java.util.function.Function;
import net.minecraft.world.item.ItemStack;

/** Wearable electric armor; protection/flight mechanics are independent from EU storage. */
public final class EuElectricArmorItem extends TranslatedArmorItem implements SpecElectricItem {
    private final EuElectricItemSpec spec;
    public EuElectricArmorItem(String path, net.minecraft.world.item.ArmorMaterial material, Type type, Properties properties, Function<ItemStack, String> resolver) {
        super(path, material, type, properties, resolver);
        this.spec = EuElectricItemSpec.fromItemPath(path);
    }
    @Override public EuElectricItemSpec spec() { return spec; }
    @Override public boolean isBarVisible(ItemStack stack) { return EuItemManager.showBar(stack); }
    @Override public int getBarWidth(ItemStack stack) { return EuItemManager.barWidth(stack); }
    @Override public int getBarColor(ItemStack stack) { return EuItemManager.barColor(stack); }
}
