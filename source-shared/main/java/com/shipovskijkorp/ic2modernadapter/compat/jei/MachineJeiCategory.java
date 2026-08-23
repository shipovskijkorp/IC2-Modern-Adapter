package com.shipovskijkorp.ic2modernadapter.compat.jei;

import com.shipovskijkorp.ic2modernadapter.machine.LegacyMachineRecipeDefinition;
import com.shipovskijkorp.ic2modernadapter.machine.LegacyMachineRecipeRegistry;
import com.shipovskijkorp.ic2modernadapter.machine.MachineSpec;
import com.shipovskijkorp.ic2modernadapter.machine.MetalFormerMode;
import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeRuntime;
import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeStacks;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** JEI categories for original IC2 machine recipes compiled from the original .ini files. */
public final class MachineJeiCategory implements IRecipeCategory<LegacyMachineRecipeDefinition> {
    public static final RecipeType<LegacyMachineRecipeDefinition> MACERATOR = RecipeType.create(
            "ic2_modern_adapter", "macerator", LegacyMachineRecipeDefinition.class);
    public static final RecipeType<LegacyMachineRecipeDefinition> COMPRESSOR = RecipeType.create(
            "ic2_modern_adapter", "compressor", LegacyMachineRecipeDefinition.class);
    public static final RecipeType<LegacyMachineRecipeDefinition> METAL_FORMER_EXTRUDING = RecipeType.create(
            "ic2_modern_adapter", "metal_former_extruding", LegacyMachineRecipeDefinition.class);
    public static final RecipeType<LegacyMachineRecipeDefinition> METAL_FORMER_ROLLING = RecipeType.create(
            "ic2_modern_adapter", "metal_former_rolling", LegacyMachineRecipeDefinition.class);
    public static final RecipeType<LegacyMachineRecipeDefinition> METAL_FORMER_CUTTING = RecipeType.create(
            "ic2_modern_adapter", "metal_former_cutting", LegacyMachineRecipeDefinition.class);
    public static final RecipeType<LegacyMachineRecipeDefinition> ORE_WASHING_PLANT = RecipeType.create(
            "ic2_modern_adapter", "ore_washing_plant", LegacyMachineRecipeDefinition.class);

    private static final ResourceLocation COMMON = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/common.png"));
    private static final ResourceLocation ORE_WASHING_GUI = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/guiorewashingplant.png"));

    private static final int WIDTH = 160;
    private static final int HEIGHT = 60;

    private static final int ORE_WASHING_IMAGE_X = 37;
    private static final int ORE_WASHING_IMAGE_Y = 0;
    private static final int ORE_WASHING_IMAGE_U = 37;
    private static final int ORE_WASHING_IMAGE_V = 16;
    private static final int ORE_WASHING_IMAGE_WIDTH = 87;
    private static final int ORE_WASHING_IMAGE_HEIGHT = 60;

    private final MachineSpec spec;
    private final MetalFormerMode metalFormerMode;
    private final RecipeType<LegacyMachineRecipeDefinition> type;
    private final IDrawable background;
    private final IDrawable icon;

    public MachineJeiCategory(MachineSpec spec, IGuiHelper guiHelper) {
        this.spec = Objects.requireNonNull(spec, "spec");
        this.metalFormerMode = null;
        this.type = typeFor(spec);
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(IC2VariantStacks.create(spec.variantKey()));
    }

    public MachineJeiCategory(MetalFormerMode mode, IGuiHelper guiHelper) {
        this.spec = MachineSpec.METAL_FORMER;
        this.metalFormerMode = Objects.requireNonNull(mode, "mode");
        this.type = metalFormerType(mode);
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(mode.icon());
    }

    public static RecipeType<LegacyMachineRecipeDefinition> typeFor(MachineSpec spec) {
        return switch (spec) {
            case COMPRESSOR -> COMPRESSOR;
            case METAL_FORMER -> METAL_FORMER_EXTRUDING;
            case ORE_WASHING_PLANT -> ORE_WASHING_PLANT;
            default -> MACERATOR;
        };
    }

    public static RecipeType<LegacyMachineRecipeDefinition> metalFormerType(MetalFormerMode mode) {
        return switch (mode) {
            case EXTRUDING -> METAL_FORMER_EXTRUDING;
            case ROLLING -> METAL_FORMER_ROLLING;
            case CUTTING -> METAL_FORMER_CUTTING;
        };
    }

    public static List<LegacyMachineRecipeDefinition> recipesForMetalFormerMode(MetalFormerMode mode) {
        return LegacyMachineRecipeRegistry.recipes(MachineSpec.METAL_FORMER).stream()
                .filter(recipe -> recipe.source().startsWith(mode.sourcePrefix()))
                .toList();
    }

    @Override
    public RecipeType<LegacyMachineRecipeDefinition> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        if (metalFormerMode != null) {
            return Component.translatable(metalFormerMode.tooltipKey());
        }
        return Component.translatable(spec.translationKey());
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LegacyMachineRecipeDefinition recipe, IFocusGroup focuses) {
        if (spec == MachineSpec.ORE_WASHING_PLANT) {
            setOreWashingRecipe(builder, recipe);
            return;
        }
        if (spec == MachineSpec.METAL_FORMER) {
            setMetalFormerRecipe(builder, recipe);
            return;
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 56, 1)
                .addItemStacks(displayInputs(recipe));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 116, 19)
                .addItemStacks(displayOutputs(recipe));
    }

    @Override
    public void draw(
            LegacyMachineRecipeDefinition recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        if (spec == MachineSpec.ORE_WASHING_PLANT) {
            drawOreWashingPlantFrame(graphics);
            return;
        }
        if (spec == MachineSpec.METAL_FORMER) {
            drawMetalFormerFrame(graphics);
            return;
        }
        drawSimpleMachineFrame(graphics, spec.progressStyle());
    }

    private void setMetalFormerRecipe(IRecipeLayoutBuilder builder, LegacyMachineRecipeDefinition recipe) {
        builder.addSlot(RecipeIngredientRole.INPUT, 17, 1)
                .addItemStacks(displayInputs(recipe));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 116, 19)
                .addItemStacks(displayOutputs(recipe));
    }

    private void setOreWashingRecipe(IRecipeLayoutBuilder builder, LegacyMachineRecipeDefinition recipe) {
        builder.addSlot(RecipeIngredientRole.INPUT, 104, 1)
                .addItemStacks(displayInputs(recipe));
        List<ItemStack> outputs = displayOutputs(recipe);
        for (int i = 0; i < Math.min(3, outputs.size()); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 86 + i * 18, 46)
                    .addItemStack(outputs.get(i));
        }
    }

    private static List<ItemStack> displayInputs(LegacyMachineRecipeDefinition recipe) {
        Ingredient ingredient = LegacyRecipeRuntime.representativeIngredient(recipe.input(), LegacyRecipeStacks.INSTANCE);
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : Arrays.asList(ingredient.getItems())) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack copy = stack.copy();
            copy.setCount(recipe.inputCount());
            result.add(copy);
        }
        return result.isEmpty() ? List.of(ItemStack.EMPTY) : List.copyOf(result);
    }

    private static List<ItemStack> displayOutputs(LegacyMachineRecipeDefinition recipe) {
        List<ItemStack> result = new ArrayList<>();
        for (LegacyMachineRecipeDefinition.Output output : recipe.outputs()) {
            ItemStack stack = LegacyRecipeRuntime.createResult(output.item(), output.count(), LegacyRecipeStacks.INSTANCE);
            if (!stack.isEmpty()) {
                result.add(stack);
            }
        }
        return result.isEmpty() ? List.of(ItemStack.EMPTY) : List.copyOf(result);
    }

    private void drawMetalFormerFrame(GuiGraphics graphics) {
        drawSlot(graphics, 16, 0);
        drawLargeSlot(graphics, 111, 14);
        drawSlot(graphics, 16, 36);
        drawEnergyBolt(graphics, 20, 21);
        drawMetalFormerProgress(graphics, 52, 24);
    }

    private static void drawOreWashingPlantFrame(GuiGraphics graphics) {
        graphics.blit(
                ORE_WASHING_GUI,
                ORE_WASHING_IMAGE_X,
                ORE_WASHING_IMAGE_Y,
                ORE_WASHING_IMAGE_U,
                ORE_WASHING_IMAGE_V,
                ORE_WASHING_IMAGE_WIDTH,
                ORE_WASHING_IMAGE_HEIGHT);
        drawSlot(graphics, 103, 0);
        drawSlot(graphics, 37, 0);
        drawSlot(graphics, 10, 37);
        drawSlot(graphics, 37, 45);
        drawSlot(graphics, 85, 45);
        drawSlot(graphics, 103, 45);
        drawSlot(graphics, 121, 45);
        drawEnergyBolt(graphics, 15, 22);
        drawOreWashingProgress(graphics, 103, 23);
    }

    private static void drawSimpleMachineFrame(GuiGraphics graphics, MachineSpec.ProgressStyle progressStyle) {
        drawSlot(graphics, 55, 0);
        drawLargeSlot(graphics, 111, 14);
        drawSlot(graphics, 55, 36);
        drawEnergyBolt(graphics, 59, 21);
        if (progressStyle == MachineSpec.ProgressStyle.TRIANGLE) {
            drawTriangleProgress(graphics, 80, 19);
        } else {
            drawCrushProgress(graphics, 80, 22);
        }
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        blitCommon(graphics, x, y, 103, 7, 18, 18);
    }

    private static void drawLargeSlot(GuiGraphics graphics, int x, int y) {
        blitCommon(graphics, x, y, 99, 35, 26, 26);
    }

    private static void drawEnergyBolt(GuiGraphics graphics, int x, int y) {
        blitCommon(graphics, x - 4, y - 1, 96, 64, 16, 16);
        int height = animatedSize(13, 300);
        int offset = 13 - height;
        blitCommon(graphics, x, y + offset, 116, 65 + offset, 7, height);
    }

    private static void drawCrushProgress(GuiGraphics graphics, int x, int y) {
        blitCommon(graphics, x - 5, y - 3, 160, 32, 32, 16);
        blitCommon(graphics, x, y, 165, 52, animatedSize(21, 100), 11);
    }

    private static void drawTriangleProgress(GuiGraphics graphics, int x, int y) {
        blitCommon(graphics, x - 5, y, 160, 64, 32, 16);
        blitCommon(graphics, x, y + 1, 165, 80, animatedSize(22, 66), 15);
    }

    private static void drawMetalFormerProgress(GuiGraphics graphics, int x, int y) {
        blitCommon(graphics, x - 8, y - 3, 192, 0, 64, 16);
        blitCommon(graphics, x, y, 200, 19, animatedSize(46, 100), 9);
    }

    private static void drawOreWashingProgress(GuiGraphics graphics, int x, int y) {
        graphics.blit(ORE_WASHING_GUI, x - 1, y - 1, 102, 38, 20, 19);
        graphics.blit(ORE_WASHING_GUI, x, y, 177, 118, animatedSize(18, 100), 18);
    }

    private static int animatedSize(int max, int ticks) {
        int period = Math.max(1, ticks);
        long gameTick = System.currentTimeMillis() / 50L;
        int step = (int) (gameTick % (period + 1));
        return Math.max(1, Math.min(max, Math.round(max * (step / (float) period))));
    }

    private static void blitCommon(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(COMMON, x, y, u, v, width, height);
    }
}
