package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeRuntime;
import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeStacks;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

/** Runtime machine-recipe table compiled from the user's original IC2 archive. */
public final class LegacyMachineRecipeRegistry {
    private static volatile Map<MachineSpec, List<LegacyMachineRecipeDefinition>> recipes = emptyMap();

    public static void replaceAll(Map<MachineSpec, List<LegacyMachineRecipeDefinition>> values) {
        EnumMap<MachineSpec, List<LegacyMachineRecipeDefinition>> copy = new EnumMap<>(MachineSpec.class);
        for (MachineSpec spec : MachineSpec.values()) {
            copy.put(spec, List.copyOf(values.getOrDefault(spec, List.of())));
        }
        recipes = Map.copyOf(copy);
    }

    public static List<LegacyMachineRecipeDefinition> recipes(MachineSpec machine) {
        return recipes.getOrDefault(machine, List.of());
    }

    public static LegacyMachineRecipeDefinition find(MachineSpec machine, ItemStack input) {
        return find(machine, input, null);
    }

    public static LegacyMachineRecipeDefinition find(MachineSpec machine, ItemStack input, String sourcePrefix) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        for (LegacyMachineRecipeDefinition recipe : recipes(machine)) {
            if (sourcePrefix != null && !recipe.source().startsWith(sourcePrefix)) {
                continue;
            }
            if (input.getCount() >= recipe.inputCount()
                    && LegacyRecipeRuntime.matchesIngredient(recipe.input(), input, LegacyRecipeStacks.INSTANCE)) {
                return recipe;
            }
        }
        return null;
    }

    public static boolean isInput(MachineSpec machine, ItemStack input) {
        return find(machine, input) != null;
    }

    private static Map<MachineSpec, List<LegacyMachineRecipeDefinition>> emptyMap() {
        EnumMap<MachineSpec, List<LegacyMachineRecipeDefinition>> empty = new EnumMap<>(MachineSpec.class);
        for (MachineSpec spec : MachineSpec.values()) {
            empty.put(spec, List.of());
        }
        return Map.copyOf(empty);
    }

    private LegacyMachineRecipeRegistry() {
    }
}
