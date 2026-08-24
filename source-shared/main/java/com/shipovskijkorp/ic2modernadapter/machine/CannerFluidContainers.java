package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeRuntime;
import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeStacks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Loader-neutral fluid-container rules used by the Fluid/Solid Canning Machine. */
public final class CannerFluidContainers {
    public static final int CELL_AMOUNT_MB = 1000;

    public record DrainResult(Ic2FluidKind fluid, int amountMb, ItemStack emptyContainer) {}
    public record FillResult(Ic2FluidKind fluid, int amountMb, ItemStack filledContainer) {}

    public static DrainResult drain(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        String id = itemId(stack);
        return switch (id) {
            case "minecraft:water_bucket" -> new DrainResult(Ic2FluidKind.WATER, CELL_AMOUNT_MB, item("minecraft:bucket"));
            case "minecraft:lava_bucket" -> new DrainResult(Ic2FluidKind.LAVA, CELL_AMOUNT_MB, item("minecraft:bucket"));
            case "minecraft:milk_bucket" -> new DrainResult(Ic2FluidKind.MILK, CELL_AMOUNT_MB, item("minecraft:bucket"));
            default -> drainFluidCell(stack);
        };
    }

    private static DrainResult drainFluidCell(ItemStack stack) {
        String variant = LegacyRecipeStacks.INSTANCE.variantKey(stack);
        if (variant == null || !variant.startsWith("fluid_cell/") || "fluid_cell/empty".equals(variant)) {
            return null;
        }
        Ic2FluidKind fluid = Ic2FluidKind.byKey(variant.substring("fluid_cell/".length()));
        if (fluid.isEmpty()) {
            return null;
        }
        return new DrainResult(fluid, CELL_AMOUNT_MB, LegacyRecipeStacks.INSTANCE.createVariant("fluid_cell/empty"));
    }

    public static FillResult fill(ItemStack stack, Ic2FluidKind fluid, int availableMb) {
        if (stack == null || stack.isEmpty() || fluid == null || fluid.isEmpty() || availableMb < CELL_AMOUNT_MB) {
            return null;
        }
        String id = itemId(stack);
        if ("minecraft:bucket".equals(id)) {
            if (fluid == Ic2FluidKind.WATER) {
                return new FillResult(fluid, CELL_AMOUNT_MB, item("minecraft:water_bucket"));
            }
            if (fluid == Ic2FluidKind.LAVA) {
                return new FillResult(fluid, CELL_AMOUNT_MB, item("minecraft:lava_bucket"));
            }
            if (fluid == Ic2FluidKind.MILK) {
                return new FillResult(fluid, CELL_AMOUNT_MB, item("minecraft:milk_bucket"));
            }
            return null;
        }
        String variant = LegacyRecipeStacks.INSTANCE.variantKey(stack);
        if ("fluid_cell/empty".equals(variant)) {
            return new FillResult(fluid, CELL_AMOUNT_MB, fluidCell(fluid));
        }
        return null;
    }

    public static boolean isEmptyContainer(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return "minecraft:bucket".equals(itemId(stack))
                || "fluid_cell/empty".equals(LegacyRecipeStacks.INSTANCE.variantKey(stack));
    }

    public static ItemStack fluidCell(Ic2FluidKind fluid) {
        if (fluid == null || fluid.isEmpty()) {
            return LegacyRecipeStacks.INSTANCE.createVariant("fluid_cell/empty");
        }
        return LegacyRecipeRuntime.createResult("fluid_cell:" + fluid.key(), 1, LegacyRecipeStacks.INSTANCE);
    }

    public static ItemStack representativeFluidStack(Ic2FluidKind fluid) {
        if (fluid == Ic2FluidKind.WATER) {
            ItemStack bucket = item("minecraft:water_bucket");
            if (!bucket.isEmpty()) {
                return bucket;
            }
        }
        if (fluid == Ic2FluidKind.LAVA) {
            ItemStack bucket = item("minecraft:lava_bucket");
            if (!bucket.isEmpty()) {
                return bucket;
            }
        }
        if (fluid == Ic2FluidKind.MILK) {
            ItemStack bucket = item("minecraft:milk_bucket");
            if (!bucket.isEmpty()) {
                return bucket;
            }
        }
        return fluidCell(fluid);
    }

    public static ItemStack item(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return ItemStack.EMPTY;
        }
        return BuiltInRegistries.ITEM.getOptional(location)
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
    }

    public static String itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null ? "" : id.toString();
    }

    private CannerFluidContainers() {
    }
}
