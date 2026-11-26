package com.shipovskijkorp.ic2modernadapter.content.item;

import net.minecraft.world.item.ItemStack;

/** Version-specific persistence of the sprayer's internal mB amount. */
public final class FoamCharge {
    private static final String KEY = "ic2ma_foam_mb";
    public static int get(ItemStack stack) {
        return stack.hasTag() ? clamp(stack.getTag().getInt(KEY)) : 0;
    }
    public static void set(ItemStack stack, int amount) {
        amount = clamp(amount);
        if (amount == 0) {
            if (stack.hasTag()) {
                stack.getTag().remove(KEY);
                if (stack.getTag().isEmpty()) stack.setTag(null);
            }
        } else stack.getOrCreateTag().putInt(KEY, amount);
    }
    private static int clamp(int amount) { return Math.max(0, Math.min(FoamSprayerItem.CAPACITY, amount)); }
    private FoamCharge() {}
}
