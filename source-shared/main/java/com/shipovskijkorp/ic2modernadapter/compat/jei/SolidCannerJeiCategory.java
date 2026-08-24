package com.shipovskijkorp.ic2modernadapter.compat.jei;

import com.shipovskijkorp.ic2modernadapter.machine.CannerBottleRecipeDefinition;
import com.shipovskijkorp.ic2modernadapter.machine.MachineSpec;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
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

/** Compact JEI category for Solid Canning Machine recipes. */
public final class SolidCannerJeiCategory implements IRecipeCategory<CannerBottleRecipeDefinition> {
    public static final RecipeType<CannerBottleRecipeDefinition> SOLID_CANNING = RecipeType.create(
            "ic2_modern_adapter", "solid_canner", CannerBottleRecipeDefinition.class);

    private static final ResourceLocation COMMON = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/common.png"));
    private static final ResourceLocation CANNER_ARROW = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/overlay/canner_arrow.png"));
    private static final int TEX = 256;
    private static final int WIDTH = 160;
    private static final int HEIGHT = 60;

    private final IDrawable background;
    private final IDrawable icon;

    public SolidCannerJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(IC2VariantStacks.create(MachineSpec.SOLID_CANNER.variantKey()));
    }

    @Override
    public RecipeType<CannerBottleRecipeDefinition> getRecipeType() {
        return SOLID_CANNING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(MachineSpec.SOLID_CANNER.translationKey());
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
    public void setRecipe(IRecipeLayoutBuilder builder, CannerBottleRecipeDefinition recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 37, 20)
                .addItemStacks(recipe.displayFillStacks());
        builder.addSlot(RecipeIngredientRole.INPUT, 67, 20)
                .addItemStacks(recipe.displayContainerStacks());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 116, 20)
                .addItemStack(recipe.createOutput());
    }

    @Override
    public void draw(
            CannerBottleRecipeDefinition recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        drawSlot(graphics, 36, 19);
        drawSlot(graphics, 66, 19);
        drawSlot(graphics, 115, 19);
        graphics.blit(CANNER_ARROW, 54, 19, 0, 0, 12, 18, 16, 32);
        drawProgressArrow(graphics, 89, 20);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.blit(COMMON, x, y, 103, 7, 18, 18, TEX, TEX);
    }

    private static void drawProgressArrow(GuiGraphics graphics, int x, int y) {
        graphics.blit(COMMON, x - 5, y, 160, 0, 32, 16, TEX, TEX);
        int width = animatedSize(22, 66);
        if (width > 0) {
            graphics.blit(COMMON, x, y, 165, 16, width, 15, TEX, TEX);
        }
    }

    private static int animatedSize(int max, int ticks) {
        int period = Math.max(1, ticks);
        long gameTick = System.currentTimeMillis() / 50L;
        int step = (int) (gameTick % (period + 1));
        return Math.max(1, Math.min(max, Math.round(max * (step / (float) period))));
    }
}
