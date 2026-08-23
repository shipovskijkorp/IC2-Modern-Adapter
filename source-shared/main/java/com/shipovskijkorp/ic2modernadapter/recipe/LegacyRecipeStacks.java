package com.shipovskijkorp.ic2modernadapter.recipe;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.minecraft.world.item.ItemStack;

/** Shared bridge from runtime recipe logic to the version-specific legacy stack encoding. */
public final class LegacyRecipeStacks {
    public static final LegacyRecipeRuntime.StackAccess INSTANCE = new LegacyRecipeRuntime.StackAccess() {
        @Override
        public String variantKey(ItemStack stack) {
            return IC2VariantStacks.variantKey(stack);
        }

        @Override
        public ItemStack createVariant(String variantKey) {
            return IC2VariantStacks.create(variantKey);
        }

        @Override
        public ItemStack createDynamicVariant(String itemPath, String variantKey) {
            return IC2VariantStacks.createDynamicVariant(itemPath, variantKey);
        }
    };

    private LegacyRecipeStacks() {
    }
}
