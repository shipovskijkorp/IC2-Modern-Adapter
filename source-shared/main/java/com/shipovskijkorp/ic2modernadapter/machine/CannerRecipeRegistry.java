package com.shipovskijkorp.ic2modernadapter.machine;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Original IC2 2.8.222 cannerBottle and cannerEnrich recipes. */
public final class CannerRecipeRegistry {
    private static final String TIN_CAN = "variant:crafting/tin_can";
    private static final String FILLED_TIN_CAN = "item:ic2:filled_tin_can";
    private static final String FUEL_ROD = "variant:crafting/fuel_rod";

    private static final List<CannerBottleRecipeDefinition> BOTTLE_RECIPES = createBottleRecipes();
    private static final List<CannerEnrichRecipeDefinition> ENRICH_RECIPES = createEnrichRecipes();

    public static List<CannerBottleRecipeDefinition> bottleRecipes() {
        return BOTTLE_RECIPES;
    }

    public static List<CannerEnrichRecipeDefinition> enrichRecipes() {
        return ENRICH_RECIPES;
    }

    public static CannerBottleRecipeDefinition findBottleRecipe(ItemStack container, ItemStack fill) {
        if (container == null || container.isEmpty() || fill == null || fill.isEmpty()) {
            return null;
        }
        for (CannerBottleRecipeDefinition recipe : BOTTLE_RECIPES) {
            if (recipe.matches(container, fill)) {
                return recipe;
            }
        }
        return null;
    }

    public static boolean acceptsBottleContainer(ItemStack container, ItemStack fill) {
        if (container == null || container.isEmpty()) {
            return false;
        }
        for (CannerBottleRecipeDefinition recipe : BOTTLE_RECIPES) {
            if (recipe.matchesContainer(container) && (fill == null || fill.isEmpty() || recipe.matchesFill(fill))) {
                return true;
            }
        }
        return false;
    }

    public static boolean acceptsBottleFill(ItemStack fill, ItemStack container) {
        if (fill == null || fill.isEmpty()) {
            return false;
        }
        for (CannerBottleRecipeDefinition recipe : BOTTLE_RECIPES) {
            if (recipe.matchesFill(fill) && (container == null || container.isEmpty() || recipe.matchesContainer(container))) {
                return true;
            }
        }
        return false;
    }

    public static CannerEnrichRecipeDefinition findEnrichRecipe(Ic2FluidKind fluid, int amountMb, ItemStack additive) {
        if (fluid == null || fluid.isEmpty() || additive == null || additive.isEmpty()) {
            return null;
        }
        for (CannerEnrichRecipeDefinition recipe : ENRICH_RECIPES) {
            if (recipe.matches(fluid, amountMb, additive)) {
                return recipe;
            }
        }
        return null;
    }

    public static boolean acceptsEnrichAdditive(ItemStack additive, Ic2FluidKind fluid, int amountMb) {
        if (additive == null || additive.isEmpty()) {
            return false;
        }
        for (CannerEnrichRecipeDefinition recipe : ENRICH_RECIPES) {
            if (recipe.matchesAdditive(additive)
                    && (fluid == null || fluid.isEmpty() || (recipe.inputFluid() == fluid && amountMb >= recipe.inputAmountMb()))) {
                return true;
            }
        }
        return false;
    }

    private static List<CannerBottleRecipeDefinition> createBottleRecipes() {
        List<CannerBottleRecipeDefinition> recipes = new ArrayList<>();
        addBottle(recipes, "uranium_fuel_rod", FUEL_ROD, 1, "variant:nuclear/uranium", 1, "item:ic2:uranium_fuel_rod", 1);
        addBottle(recipes, "mox_fuel_rod", FUEL_ROD, 1, "variant:nuclear/mox", 1, "item:ic2:mox_fuel_rod", 1);
        addFood(recipes, "potato", 1, "item:minecraft:potato", 1);
        addFood(recipes, "cookie", 2, "item:minecraft:cookie", 1);
        addFood(recipes, "melon", 2, "item:minecraft:melon_slice", 1);
        addFood(recipes, "fish", 2, "item:minecraft:cod", 1);
        addFood(recipes, "chicken", 2, "item:minecraft:chicken", 1);
        addFood(recipes, "porkchop", 3, "item:minecraft:porkchop", 1);
        addFood(recipes, "beef", 3, "item:minecraft:beef", 1);
        addFood(recipes, "apple", 4, "item:minecraft:apple", 1);
        addFood(recipes, "carrot", 4, "item:minecraft:carrot", 1);
        addFood(recipes, "bread", 5, "item:minecraft:bread", 1);
        addFood(recipes, "cooked_fish", 5, "item:minecraft:cooked_cod", 1);
        addFood(recipes, "cooked_chicken", 6, "item:minecraft:cooked_chicken", 1);
        addFood(recipes, "baked_potato", 6, "item:minecraft:baked_potato", 1);
        addFood(recipes, "mushroom_stew", 6, "item:minecraft:mushroom_stew", 1);
        addFood(recipes, "pumpkin_pie", 6, "item:minecraft:pumpkin_pie", 1);
        addFood(recipes, "cooked_porkchop", 8, "item:minecraft:cooked_porkchop", 1);
        addFood(recipes, "cooked_beef", 8, "item:minecraft:cooked_beef", 1);
        addFood(recipes, "cake", 12, "item:minecraft:cake", 1);
        addFood(recipes, "poisonous_potato", 1, "item:minecraft:poisonous_potato", 2);
        addFood(recipes, "rotten_flesh", 1, "item:minecraft:rotten_flesh", 2);
        return List.copyOf(recipes);
    }

    private static void addFood(List<CannerBottleRecipeDefinition> recipes, String id, int canCount, String fill, int fillCount) {
        addBottle(recipes, id, TIN_CAN, canCount, fill, fillCount, FILLED_TIN_CAN, canCount);
    }

    private static void addBottle(
            List<CannerBottleRecipeDefinition> recipes,
            String id,
            String container,
            int containerCount,
            String fill,
            int fillCount,
            String output,
            int outputCount) {
        recipes.add(new CannerBottleRecipeDefinition(id, container, containerCount, fill, fillCount, output, outputCount));
    }

    private static List<CannerEnrichRecipeDefinition> createEnrichRecipes() {
        List<CannerEnrichRecipeDefinition> recipes = new ArrayList<>();
        recipes.add(new CannerEnrichRecipeDefinition("milk", Ic2FluidKind.WATER, 1000,
                "variant:dust/milk", 1, Ic2FluidKind.MILK, 1000));
        recipes.add(new CannerEnrichRecipeDefinition("construction_foam", Ic2FluidKind.WATER, 1000,
                "variant:crafting/cf_powder", 1, Ic2FluidKind.CONSTRUCTION_FOAM, 1000));
        recipes.add(new CannerEnrichRecipeDefinition("coolant_lapis_water", Ic2FluidKind.WATER, 1000,
                "ore:dustLapis", 8, Ic2FluidKind.COOLANT, 1000));
        recipes.add(new CannerEnrichRecipeDefinition("coolant_lapis_distilled", Ic2FluidKind.DISTILLED_WATER, 1000,
                "ore:dustLapis", 1, Ic2FluidKind.COOLANT, 1000));
        recipes.add(new CannerEnrichRecipeDefinition("biomass", Ic2FluidKind.WATER, 1000,
                "variant:crafting/bio_chaff", 1, Ic2FluidKind.BIOMASS, 1000));
        recipes.add(new CannerEnrichRecipeDefinition("hot_water", Ic2FluidKind.WATER, 6000,
                "item:minecraft:stick", 1, Ic2FluidKind.HOT_WATER, 1000));
        return List.copyOf(recipes);
    }

    private CannerRecipeRegistry() {
    }
}
