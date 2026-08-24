package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeRuntime;
import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeStacks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** One original IC2 cannerBottle recipe: container + fill item -> output item. */
public record CannerBottleRecipeDefinition(
        String id,
        String container,
        int containerCount,
        String fill,
        int fillCount,
        String output,
        int outputCount) {
    public CannerBottleRecipeDefinition {
        if (containerCount <= 0 || fillCount <= 0 || outputCount <= 0) {
            throw new IllegalArgumentException("Canner recipe counts must be positive: " + id);
        }
    }

    public boolean matches(ItemStack containerStack, ItemStack fillStack) {
        return containerStack != null
                && fillStack != null
                && containerStack.getCount() >= containerCount
                && fillStack.getCount() >= fillCount
                && LegacyRecipeRuntime.matchesIngredient(container, containerStack, LegacyRecipeStacks.INSTANCE)
                && LegacyRecipeRuntime.matchesIngredient(fill, fillStack, LegacyRecipeStacks.INSTANCE);
    }

    public boolean matchesContainer(ItemStack stack) {
        return stack != null && stack.getCount() >= containerCount
                && LegacyRecipeRuntime.matchesIngredient(container, stack, LegacyRecipeStacks.INSTANCE);
    }

    public boolean matchesFill(ItemStack stack) {
        return stack != null && stack.getCount() >= fillCount
                && LegacyRecipeRuntime.matchesIngredient(fill, stack, LegacyRecipeStacks.INSTANCE);
    }

    public ItemStack createOutput() {
        return LegacyRecipeRuntime.createResult(output, outputCount, LegacyRecipeStacks.INSTANCE);
    }

    public List<ItemStack> displayContainerStacks() {
        return displayStacks(container, containerCount);
    }

    public List<ItemStack> displayFillStacks() {
        return displayStacks(fill, fillCount);
    }

    private static List<ItemStack> displayStacks(String token, int count) {
        Ingredient ingredient = LegacyRecipeRuntime.representativeIngredient(token, LegacyRecipeStacks.INSTANCE);
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : Arrays.asList(ingredient.getItems())) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack copy = stack.copy();
            copy.setCount(count);
            result.add(copy);
        }
        return result.isEmpty() ? List.of(ItemStack.EMPTY) : List.copyOf(result);
    }
}
