package com.shipovskijkorp.ic2modernadapter.furnace;

import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeStacks;
import net.minecraft.world.item.ItemStack;

final class FurnaceInventoryUtil {
    static boolean canStacksMerge(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty() || left.getItem() != right.getItem()) {
            return false;
        }
        String leftVariant = LegacyRecipeStacks.INSTANCE.variantKey(left);
        String rightVariant = LegacyRecipeStacks.INSTANCE.variantKey(right);
        if (leftVariant == null ? rightVariant != null : !leftVariant.equals(rightVariant)) {
            return false;
        }
        return left.getDamageValue() == right.getDamageValue();
    }

    static boolean canOutput(ItemStack current, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (current.isEmpty()) {
            return stack.getCount() <= stack.getMaxStackSize();
        }
        return canStacksMerge(current, stack) && current.getCount() + stack.getCount() <= current.getMaxStackSize();
    }

    static void insertOutput(net.minecraft.core.NonNullList<ItemStack> items, int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack current = items.get(slot);
        if (current.isEmpty()) {
            items.set(slot, stack.copy());
        } else if (canStacksMerge(current, stack)) {
            current.grow(stack.getCount());
        }
    }

    private FurnaceInventoryUtil() {
    }
}
