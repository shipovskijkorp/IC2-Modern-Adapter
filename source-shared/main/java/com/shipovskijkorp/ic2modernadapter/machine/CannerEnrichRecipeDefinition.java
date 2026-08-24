package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeRuntime;
import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeStacks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** One original IC2 cannerEnrich recipe: input fluid + additive -> output fluid. */
public record CannerEnrichRecipeDefinition(
        String id,
        Ic2FluidKind inputFluid,
        int inputAmountMb,
        String additive,
        int additiveCount,
        Ic2FluidKind outputFluid,
        int outputAmountMb) {
    public CannerEnrichRecipeDefinition {
        if (inputFluid == null || inputFluid.isEmpty() || outputFluid == null || outputFluid.isEmpty()) {
            throw new IllegalArgumentException("Canner enrichment recipes need non-empty fluids: " + id);
        }
        if (inputAmountMb <= 0 || additiveCount <= 0 || outputAmountMb <= 0) {
            throw new IllegalArgumentException("Canner enrichment amounts must be positive: " + id);
        }
    }

    public boolean matches(Ic2FluidKind fluid, int amountMb, ItemStack additiveStack) {
        return fluid == inputFluid
                && amountMb >= inputAmountMb
                && additiveStack != null
                && additiveStack.getCount() >= additiveCount
                && LegacyRecipeRuntime.matchesIngredient(additive, additiveStack, LegacyRecipeStacks.INSTANCE);
    }

    public boolean matchesAdditive(ItemStack stack) {
        return stack != null && stack.getCount() >= additiveCount
                && LegacyRecipeRuntime.matchesIngredient(additive, stack, LegacyRecipeStacks.INSTANCE);
    }

    public List<ItemStack> displayAdditiveStacks() {
        Ingredient ingredient = LegacyRecipeRuntime.representativeIngredient(additive, LegacyRecipeStacks.INSTANCE);
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : Arrays.asList(ingredient.getItems())) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack copy = stack.copy();
            copy.setCount(additiveCount);
            result.add(copy);
        }
        return result.isEmpty() ? List.of(ItemStack.EMPTY) : List.copyOf(result);
    }

    public ItemStack displayInputFluidStack() {
        ItemStack stack = CannerFluidContainers.representativeFluidStack(inputFluid);
        stack.setCount(Math.max(1, (int) Math.ceil(inputAmountMb / 1000.0D)));
        return stack;
    }

    public ItemStack displayOutputFluidStack() {
        ItemStack stack = CannerFluidContainers.representativeFluidStack(outputFluid);
        stack.setCount(Math.max(1, (int) Math.ceil(outputAmountMb / 1000.0D)));
        return stack;
    }
}
