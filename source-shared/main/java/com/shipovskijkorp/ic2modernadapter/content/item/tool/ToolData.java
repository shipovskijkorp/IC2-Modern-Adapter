package com.shipovskijkorp.ic2modernadapter.content.item.tool;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.minecraft.world.item.ItemStack;

public final class ToolData {
    private ToolData() {}
    public static boolean flag(ItemStack stack, String key) { return IC2VariantStacks.electricData(stack).getBoolean(key); }
    public static void flag(ItemStack stack, String key, boolean value) {
        var tag = IC2VariantStacks.electricData(stack);
        if (tag.getBoolean(key) == value) return;
        tag.putBoolean(key, value);
        IC2VariantStacks.setElectricData(stack, tag);
    }
}
