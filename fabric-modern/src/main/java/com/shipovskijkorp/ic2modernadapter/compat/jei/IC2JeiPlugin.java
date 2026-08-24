package com.shipovskijkorp.ic2modernadapter.compat.jei;

import com.shipovskijkorp.ic2modernadapter.client.screen.CompressorScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.CannerScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.ExtractorScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.ElectricFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.InductionFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.IronFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.MaceratorScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.MetalFormerScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.OreWashingPlantScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.ThermalCentrifugeScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.SolidCannerScreen;
import com.shipovskijkorp.ic2modernadapter.content.LegacyJeiSubtypes;
import com.shipovskijkorp.ic2modernadapter.machine.LegacyMachineRecipeRegistry;
import com.shipovskijkorp.ic2modernadapter.machine.CannerRecipeRegistry;
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

/** JEI integration for legacy IC2 metadata/custom-data subtype identity on 1.21.1. */
@JeiPlugin
public final class IC2JeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            "ic2_modern_adapter", "ic2_legacy_subtypes");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        for (String itemPath : LegacyJeiSubtypes.itemPathsWithSubtypes()) {
            Item item = IC2ContentRegistries.item(itemPath).get();
            registration.registerSubtypeInterpreter(
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
                new MachineJeiCategory(MachineSpec.EXTRACTOR, guiHelper),
                new CannerJeiCategory(guiHelper),
                new CannerEnrichJeiCategory(guiHelper),
                new SolidCannerJeiCategory(guiHelper),
                new MachineJeiCategory(MetalFormerMode.EXTRUDING, guiHelper),
                new MachineJeiCategory(MetalFormerMode.ROLLING, guiHelper),
                new MachineJeiCategory(MetalFormerMode.CUTTING, guiHelper),
                new MachineJeiCategory(MachineSpec.ORE_WASHING_PLANT, guiHelper),
                new MachineJeiCategory(MachineSpec.THERMAL_CENTRIFUGE, guiHelper));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(MaceratorScreen.class, 80, 38, 21, 11, MachineJeiCategory.MACERATOR);
        registration.addRecipeClickArea(CompressorScreen.class, 80, 35, 22, 15, MachineJeiCategory.COMPRESSOR);
        registration.addRecipeClickArea(ExtractorScreen.class, 80, 35, 22, 15, MachineJeiCategory.EXTRACTOR);
        registration.addRecipeClickArea(CannerScreen.class, 74, 22, 23, 14,
                CannerJeiCategory.CANNING,
                CannerEnrichJeiCategory.ENRICHING);
        registration.addRecipeClickArea(SolidCannerScreen.class, 88, 35, 22, 15,
                SolidCannerJeiCategory.SOLID_CANNING);
        registration.addRecipeClickArea(MetalFormerScreen.class, 52, 39, 46, 9,
                MachineJeiCategory.METAL_FORMER_EXTRUDING,
                MachineJeiCategory.METAL_FORMER_ROLLING,
                MachineJeiCategory.METAL_FORMER_CUTTING);
        registration.addRecipeClickArea(OreWashingPlantScreen.class, 103, 39, 18, 18,
                MachineJeiCategory.ORE_WASHING_PLANT);
        registration.addRecipeClickArea(ThermalCentrifugeScreen.class, 84, 25, 3, 28,
                MachineJeiCategory.THERMAL_CENTRIFUGE);
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
                MachineJeiCategory.EXTRACTOR,
                LegacyMachineRecipeRegistry.recipes(MachineSpec.EXTRACTOR));
        registration.addRecipes(
                CannerJeiCategory.CANNING,
                CannerRecipeRegistry.bottleRecipes());
        registration.addRecipes(
                CannerEnrichJeiCategory.ENRICHING,
                CannerRecipeRegistry.enrichRecipes());
        registration.addRecipes(
                SolidCannerJeiCategory.SOLID_CANNING,
                CannerRecipeRegistry.bottleRecipes());
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
        registration.addRecipes(
                MachineJeiCategory.THERMAL_CENTRIFUGE,
                LegacyMachineRecipeRegistry.recipes(MachineSpec.THERMAL_CENTRIFUGE));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.MACERATOR.variantKey()), MachineJeiCategory.MACERATOR);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.COMPRESSOR.variantKey()), MachineJeiCategory.COMPRESSOR);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.EXTRACTOR.variantKey()), MachineJeiCategory.EXTRACTOR);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.CANNER.variantKey()), CannerJeiCategory.CANNING);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.CANNER.variantKey()), CannerEnrichJeiCategory.ENRICHING);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.SOLID_CANNER.variantKey()), SolidCannerJeiCategory.SOLID_CANNING);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.METAL_FORMER.variantKey()), MachineJeiCategory.METAL_FORMER_EXTRUDING);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.METAL_FORMER.variantKey()), MachineJeiCategory.METAL_FORMER_ROLLING);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.METAL_FORMER.variantKey()), MachineJeiCategory.METAL_FORMER_CUTTING);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.ORE_WASHING_PLANT.variantKey()), MachineJeiCategory.ORE_WASHING_PLANT);
        registration.addRecipeCatalyst(IC2VariantStacks.create(MachineSpec.THERMAL_CENTRIFUGE.variantKey()), MachineJeiCategory.THERMAL_CENTRIFUGE);
    }
}
