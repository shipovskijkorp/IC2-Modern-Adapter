package com.shipovskijkorp.ic2modernadapter.content.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;

/** Version-specific persistence of the sprayer's internal mB amount. */
public final class FoamCharge {
    private static final String KEY = "ic2ma_foam_mb";
    public static int get(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? 0 : clamp(data.copyTag().getInt(KEY));
    }
    public static void set(ItemStack stack, int amount) {
        amount = clamp(amount);
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
        if (amount == 0) tag.remove(KEY); else tag.putInt(KEY, amount);
        if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }
    private static int clamp(int amount) { return Math.max(0, Math.min(FoamSprayerItem.CAPACITY, amount)); }
    private FoamCharge() {}
}
