package com.shipovskijkorp.ic2modernadapter.compat.jei;

import com.shipovskijkorp.ic2modernadapter.client.screen.CompressorScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.ElectricFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.InductionFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.IronFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.MaceratorScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.MetalFormerScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.OreWashingPlantScreen;
import com.shipovskijkorp.ic2modernadapter.content.LegacyJeiSubtypes;
import com.shipovskijkorp.ic2modernadapter.machine.LegacyMachineRecipeRegistry;
import com.shipovskijkorp.ic2modernadapter.machine.MachineSpec;
import com.shipovskijkorp.ic2modernadapter.machine.MetalFormerMode;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import com.shipovskijkorp.ic2modernadapter.resource.IC2RuntimeResources;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** JEI integration for legacy IC2 metadata/NBT subtype identity on 1.20.1. */
@JeiPlugin
public final class IC2JeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation("ic2_modern_adapter", "ic2_legacy_subtypes");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        for (String itemPath : LegacyJeiSubtypes.itemPathsWithSubtypes()) {
            Item item = IC2ContentRegistries.item(itemPath).get();
            registration.registerSubtypeInterpreter(
                    VanillaTypes.ITEM_STACK,
                    item,
                    (stack, context) -> LegacyJeiSubtypes.subtype(stack, IC2VariantStacks::variantKey));
        }
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new MachineJeiCategory(MachineSpec.MACERATOR, guiHelper),
                new MachineJeiCategory(MachineSpec.COMPRESSOR, guiHelper),
                new MachineJeiCategory(MetalFormerMode.EXTRUDING, guiHelper),
                new MachineJeiCategory(MetalFormerMode.ROLLING, guiHelper),
                new MachineJeiCategory(MetalFormerMode.CUTTING, guiHelper),
                new MachineJeiCategory(MachineSpec.ORE_WASHING_PLANT, guiHelper));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(MaceratorScreen.class, 80, 38, 21, 11, MachineJeiCategory.MACERATOR);
        registration.addRecipeClickArea(CompressorScreen.class, 80, 35, 22, 15, MachineJeiCategory.COMPRESSOR);
        registration.addRecipeClickArea(MetalFormerScreen.class, 52, 39, 46, 9,
                MachineJeiCategory.METAL_FORMER_EXTRUDING,
                MachineJeiCategory.METAL_FORMER_ROLLING,
                MachineJeiCategory.METAL_FORMER_CUTTING);
        registration.addRecipeClickArea(OreWashingPlantScreen.class, 103, 39, 18, 18,
                MachineJeiCategory.ORE_WASHING_PLANT);
        registration.addRecipeClickArea(IronFurnaceScreen.class, 80, 35, 22, 15, RecipeTypes.SMELTING);
        registration.addRecipeClickArea(ElectricFurnaceScreen.class, 80, 35, 22, 15, RecipeTypes.SMELTING);
        registration.addRecipeClickArea(InductionFurnaceScreen.class, 81, 35, 22, 15, RecipeTypes.SMELTING);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IC2RuntimeResources.ensureCompiled();
        registration.addRecipes(
                MachineJeiCategory.MACERATOR,
                LegacyMachineRecipeRegistry.recipes(MachineSpec.MACERATOR));
        registration.addRecipes(
                MachineJeiCategory.COMPRESSOR,
                LegacyMachineRecipeRegistry.recipes(MachineSpec.COMPRESSOR));
        registration.addRecipes(
                MachineJeiCategory.METAL_FORMER_EXTRUDING,
                MachineJeiCategory.recipesForMetalFormerMode(MetalFormerMode.EXTRUDING));
        registration.addRecipes(
                MachineJeiCategory.METAL_FORMER_ROLLING,
                MachineJeiCategory.recipesForMetalFormerMode(MetalFormerMode.ROLLING));
        registration.addRecipes(
                MachineJeiCategory.METAL_FORMER_CUTTING,
                MachineJeiCategory.recipesForMetalFormerMode(MetalFormerMode.CUTTING));
        registration.addRecipes(
                MachineJeiCategory.ORE_WASHING_PLANT,
                LegacyMachineRecipeRegistry.recipes(MachineSpec.ORE_WASHING_PLANT));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.MACERATOR.variantKey()), MachineJeiCategory.MACERATOR);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.COMPRESSOR.variantKey()), MachineJeiCategory.COMPRESSOR);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.METAL_FORMER.variantKey()), MachineJeiCategory.METAL_FORMER_EXTRUDING);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.METAL_FORMER.variantKey()), MachineJeiCategory.METAL_FORMER_ROLLING);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.METAL_FORMER.variantKey()), MachineJeiCategory.METAL_FORMER_CUTTING);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.ORE_WASHING_PLANT.variantKey()), MachineJeiCategory.ORE_WASHING_PLANT);
    }
}
