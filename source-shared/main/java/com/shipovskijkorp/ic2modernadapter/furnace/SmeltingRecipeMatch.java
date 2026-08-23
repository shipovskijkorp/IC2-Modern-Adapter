package com.shipovskijkorp.ic2modernadapter.furnace;

import net.minecraft.world.item.ItemStack;

/** Loader-specific vanilla/IC2 smelting result resolved through the current RecipeManager. */
public record SmeltingRecipeMatch(ItemStack output, float experience) {
    public SmeltingRecipeMatch {
        output = output == null ? ItemStack.EMPTY : output.copy();
    }

    public boolean isEmpty() {
        return output.isEmpty();
    }
}
