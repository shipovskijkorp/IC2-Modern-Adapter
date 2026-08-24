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

/** Original IC2 Fluid/Solid Canning Machine JEI category for BottleSolid recipes. */
public final class CannerJeiCategory implements IRecipeCategory<CannerBottleRecipeDefinition> {
    public static final RecipeType<CannerBottleRecipeDefinition> CANNING = RecipeType.create(
            "ic2_modern_adapter", "canner_canning", CannerBottleRecipeDefinition.class);

    private static final ResourceLocation CANNER_GUI = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/guicanner.png"));
    private static final int WIDTH = 96;
    private static final int HEIGHT = 81;
    private static final int BACKGROUND_U = 40;
    private static final int BACKGROUND_V = 16;
    private static final int MODE_BUTTON_X = 23;
    private static final int MODE_BUTTON_Y = 65;
    private static final int MODE_BUTTON_W = 50;
    private static final int MODE_BUTTON_H = 14;
    private static final int MODE_BUTTON_V = 18;
    private static final int PROGRESS_X = 34;
    private static final int PROGRESS_Y = 6;
    private static final int PROGRESS_U = 233;
    private static final int PROGRESS_V = 0;
    private static final int PROGRESS_W = 23;
    private static final int PROGRESS_H = 14;

    private final IDrawable background;
    private final IDrawable icon;

    public CannerJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(IC2VariantStacks.create(MachineSpec.CANNER.variantKey()));
    }

    @Override
    public RecipeType<CannerBottleRecipeDefinition> getRecipeType() {
        return CANNING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ic2.Canner.gui.switch.BottleSolid");
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
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStacks(recipe.displayContainerStacks());
        builder.addSlot(RecipeIngredientRole.INPUT, 40, 28)
                .addItemStacks(recipe.displayFillStacks());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 79, 1)
                .addItemStack(recipe.createOutput());
    }

    @Override
    public void draw(
            CannerBottleRecipeDefinition recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        graphics.blit(CANNER_GUI, 0, 0, BACKGROUND_U, BACKGROUND_V, WIDTH, HEIGHT);
        graphics.blit(CANNER_GUI, MODE_BUTTON_X, MODE_BUTTON_Y, 176, MODE_BUTTON_V, MODE_BUTTON_W, MODE_BUTTON_H);
        graphics.blit(CANNER_GUI, 19, 37, 3, 4, 9, 18);
        graphics.blit(CANNER_GUI, 59, 37, 3, 4, 18, 23);
        drawProgress(graphics);
    }

    private static void drawProgress(GuiGraphics graphics) {
        int width = animatedSize(PROGRESS_W, 66);
        if (width > 0) {
            graphics.blit(CANNER_GUI, PROGRESS_X, PROGRESS_Y, PROGRESS_U, PROGRESS_V, width, PROGRESS_H);
        }
    }

    private static int animatedSize(int max, int ticks) {
        int period = Math.max(1, ticks);
        long gameTick = System.currentTimeMillis() / 50L;
        int step = (int) (gameTick % (period + 1));
        return Math.max(1, Math.min(max, Math.round(max * (step / (float) period))));
    }
}
