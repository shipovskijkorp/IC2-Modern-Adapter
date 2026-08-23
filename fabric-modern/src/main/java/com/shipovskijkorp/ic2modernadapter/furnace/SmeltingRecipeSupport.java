package com.shipovskijkorp.ic2modernadapter.furnace;

import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/** 1.21.x smelting recipe bridge. */
final class SmeltingRecipeSupport {
    static SmeltingRecipeMatch find(Level level, ItemStack input) {
        if (level == null || input == null || input.isEmpty()) {
            return null;
        }
        Optional<?> optional = level.getRecipeManager().getRecipeFor(
                RecipeType.SMELTING,
                new SingleRecipeInput(input.copy()),
                level);
        if (optional.isEmpty()) {
            return null;
        }
        Object recipeObject = unwrapRecipe(optional.get());
        if (recipeObject instanceof AbstractCookingRecipe recipe) {
            ItemStack output = recipe.getResultItem(level.registryAccess()).copy();
            return output.isEmpty() ? null : new SmeltingRecipeMatch(output, recipe.getExperience());
        }
        return null;
    }

    private static Object unwrapRecipe(Object value) {
        try {
            Method method = value.getClass().getMethod("value");
            return method.invoke(value);
        } catch (ReflectiveOperationException ignored) {
            return value;
        }
    }

    private SmeltingRecipeSupport() {
    }
}
