package com.shipovskijkorp.ic2modernadapter.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CannerRecipeRegistryTest {
    @Test
    void exposesOriginalIc2CannerBottleSurface() {
        assertEquals(22, CannerRecipeRegistry.bottleRecipes().size());
        assertTrue(CannerRecipeRegistry.bottleRecipes().stream()
                .anyMatch(recipe -> recipe.id().equals("uranium_fuel_rod")
                        && recipe.container().equals("variant:crafting/fuel_rod")
                        && recipe.fill().equals("variant:nuclear/uranium")
                        && recipe.output().equals("item:ic2:uranium_fuel_rod")));
        assertTrue(CannerRecipeRegistry.bottleRecipes().stream()
                .anyMatch(recipe -> recipe.id().equals("cake")
                        && recipe.containerCount() == 12
                        && recipe.outputCount() == 12));
        assertTrue(CannerRecipeRegistry.bottleRecipes().stream()
                .anyMatch(recipe -> recipe.id().equals("poisonous_potato")
                        && recipe.containerCount() == 1
                        && recipe.fillCount() == 2));
    }

    @Test
    void exposesOriginalIc2CannerEnrichSurface() {
        assertEquals(6, CannerRecipeRegistry.enrichRecipes().size());
        assertTrue(CannerRecipeRegistry.enrichRecipes().stream()
                .anyMatch(recipe -> recipe.id().equals("coolant_lapis_water")
                        && recipe.inputFluid() == Ic2FluidKind.WATER
                        && recipe.inputAmountMb() == 1000
                        && recipe.additive().equals("ore:dustLapis")
                        && recipe.additiveCount() == 8
                        && recipe.outputFluid() == Ic2FluidKind.COOLANT
                        && recipe.outputAmountMb() == 1000));
        assertTrue(CannerRecipeRegistry.enrichRecipes().stream()
                .anyMatch(recipe -> recipe.id().equals("hot_water")
                        && recipe.inputFluid() == Ic2FluidKind.WATER
                        && recipe.inputAmountMb() == 6000
                        && recipe.outputFluid() == Ic2FluidKind.HOT_WATER));
    }
}
