package com.shipovskijkorp.ic2modernadapter.compat.jei;

import com.shipovskijkorp.ic2modernadapter.machine.LegacyMachineRecipeDefinition;
import com.shipovskijkorp.ic2modernadapter.machine.MachineSpec;
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

/** JEI category for original IC2 standard-machine recipes compiled from macerator/compressor.ini. */
public final class MachineJeiCategory implements IRecipeCategory<LegacyMachineRecipeDefinition> {
    public static final RecipeType<LegacyMachineRecipeDefinition> MACERATOR = RecipeType.create(
            "ic2_modern_adapter", "macerator", LegacyMachineRecipeDefinition.class);
    public static final RecipeType<LegacyMachineRecipeDefinition> COMPRESSOR = RecipeType.create(
            "ic2_modern_adapter", "compressor", LegacyMachineRecipeDefinition.class);

    private static final ResourceLocation COMMON = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/common.png"));
    private static final int WIDTH = 160;
    private static final int HEIGHT = 60;

    private final MachineSpec spec;
    private final RecipeType<LegacyMachineRecipeDefinition> type;
    private final IDrawable background;
    private final IDrawable icon;

    public MachineJeiCategory(MachineSpec spec, IGuiHelper guiHelper) {
        this.spec = spec;
        this.type = typeFor(spec);
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(IC2VariantStacks.create(spec.variantKey()));
    }

    public static RecipeType<LegacyMachineRecipeDefinition> typeFor(MachineSpec spec) {
        return spec == MachineSpec.COMPRESSOR ? COMPRESSOR : MACERATOR;
    }

    @Override
    public RecipeType<LegacyMachineRecipeDefinition> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
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
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            LegacyMachineRecipeDefinition recipe,
            IFocusGroup focuses) {
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
        drawSimpleMachineFrame(graphics, spec.progressStyle());
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
        blit(graphics, x, y, 103, 7, 18, 18);
    }

    private static void drawLargeSlot(GuiGraphics graphics, int x, int y) {
        blit(graphics, x, y, 99, 35, 26, 26);
    }

    private static void drawEnergyBolt(GuiGraphics graphics, int x, int y) {
        blit(graphics, x - 4, y - 1, 96, 64, 16, 16);
        int height = animatedSize(13, 300);
        int offset = 13 - height;
        blit(graphics, x, y + offset, 116, 65 + offset, 7, height);
    }

    private static void drawCrushProgress(GuiGraphics graphics, int x, int y) {
        blit(graphics, x - 5, y - 3, 160, 32, 32, 16);
        blit(graphics, x, y, 165, 52, animatedSize(21, 100), 11);
    }

    private static void drawTriangleProgress(GuiGraphics graphics, int x, int y) {
        blit(graphics, x - 5, y, 160, 64, 32, 16);
        blit(graphics, x, y + 1, 165, 80, animatedSize(22, 66), 15);
    }

    private static int animatedSize(int max, int ticks) {
        int period = Math.max(1, ticks);
        long gameTick = System.currentTimeMillis() / 50L;
        int step = (int) (gameTick % (period + 1));
        return Math.max(1, Math.min(max, Math.round(max * (step / (float) period))));
    }

    private static void blit(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(COMMON, x, y, u, v, width, height);
    }
}
