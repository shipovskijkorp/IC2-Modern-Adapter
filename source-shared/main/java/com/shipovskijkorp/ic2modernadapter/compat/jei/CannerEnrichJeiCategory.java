package com.shipovskijkorp.ic2modernadapter.compat.jei;

import com.shipovskijkorp.ic2modernadapter.machine.CannerEnrichRecipeDefinition;
import com.shipovskijkorp.ic2modernadapter.machine.Ic2FluidKind;
import com.shipovskijkorp.ic2modernadapter.machine.MachineSpec;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import java.util.Objects;
import java.util.Optional;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** Original IC2 Fluid/Solid Canning Machine JEI category for EnrichLiquid recipes. */
public final class CannerEnrichJeiCategory implements IRecipeCategory<CannerEnrichRecipeDefinition> {
    public static final RecipeType<CannerEnrichRecipeDefinition> ENRICHING = RecipeType.create(
            "ic2_modern_adapter", "canner_enriching", CannerEnrichRecipeDefinition.class);

    private static final ResourceLocation CANNER_GUI = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/guicanner.png"));
    private static final ResourceLocation COMMON = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/common.png"));
    private static final int WIDTH = 96;
    private static final int HEIGHT = 81;
    private static final int BACKGROUND_U = 40;
    private static final int BACKGROUND_V = 16;
    private static final int MODE_BUTTON_X = 23;
    private static final int MODE_BUTTON_Y = 65;
    private static final int MODE_BUTTON_W = 50;
    private static final int MODE_BUTTON_H = 14;
    private static final int MODE_BUTTON_V = 60;
    private static final int INPUT_TANK_X = -1;
    private static final int OUTPUT_TANK_X = 77;
    private static final int TANK_Y = 26;
    private static final int TANK_CAPACITY_MB = 8000;
    private static final int FLUID_SLOT_WIDTH = 12;
    private static final int FLUID_SLOT_HEIGHT = 47;
    private static final int INPUT_FLUID_SLOT_X = INPUT_TANK_X + 4;
    private static final int OUTPUT_FLUID_SLOT_X = OUTPUT_TANK_X + 4;
    private static final int FLUID_SLOT_Y = TANK_Y + 4;
    private static final int PROGRESS_X = 34;
    private static final int PROGRESS_Y = 6;
    private static final int PROGRESS_U = 233;
    private static final int PROGRESS_V = 0;
    private static final int PROGRESS_W = 23;
    private static final int PROGRESS_H = 14;

    private final IDrawable background;
    private final IDrawable icon;

    public CannerEnrichJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(IC2VariantStacks.create(MachineSpec.CANNER.variantKey()));
    }

    @Override
    public RecipeType<CannerEnrichRecipeDefinition> getRecipeType() {
        return ENRICHING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ic2.Canner.gui.switch.EnrichLiquid");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CannerEnrichRecipeDefinition recipe, IFocusGroup focuses) {
        addSafeFluidSlot(builder, RecipeIngredientRole.INPUT, INPUT_FLUID_SLOT_X, FLUID_SLOT_Y,
                recipe.inputFluid(), recipe.inputAmountMb());
        builder.addSlot(RecipeIngredientRole.INPUT, 40, 28)
                .addItemStacks(recipe.displayAdditiveStacks());
        addSafeFluidSlot(builder, RecipeIngredientRole.OUTPUT, OUTPUT_FLUID_SLOT_X, FLUID_SLOT_Y,
                recipe.outputFluid(), recipe.outputAmountMb());
    }

    @Override
    public void draw(
            CannerEnrichRecipeDefinition recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        graphics.blit(CANNER_GUI, 0, 0, BACKGROUND_U, BACKGROUND_V, WIDTH, HEIGHT);
        graphics.blit(CANNER_GUI, MODE_BUTTON_X, MODE_BUTTON_Y, 176, MODE_BUTTON_V, MODE_BUTTON_W, MODE_BUTTON_H);
        drawTank(graphics, INPUT_TANK_X, TANK_Y, recipe.inputFluid(), recipe.inputAmountMb());
        drawTank(graphics, OUTPUT_TANK_X, TANK_Y, recipe.outputFluid(), recipe.outputAmountMb());
        drawProgress(graphics);
    }

    private static void addSafeFluidSlot(
            IRecipeLayoutBuilder builder,
            RecipeIngredientRole role,
            int x,
            int y,
            Ic2FluidKind fluid,
            int amountMb) {
        var slot = builder.addSlot(role, x, y)
                .setFluidRenderer(TANK_CAPACITY_MB, false, FLUID_SLOT_WIDTH, FLUID_SLOT_HEIGHT);
        jeiSafeFluid(fluid).ifPresent(value -> slot.addFluidStack(value, amountMb));
    }

    private static Optional<Fluid> jeiSafeFluid(Ic2FluidKind fluid) {
        if (fluid == null || fluid.isEmpty()) {
            return Optional.empty();
        }
        return switch (fluid) {
            case WATER -> Optional.of(Fluids.WATER);
            case LAVA -> Optional.of(Fluids.LAVA);
            default -> Optional.empty();
        };
    }

    private static void drawTank(GuiGraphics graphics, int x, int y, Ic2FluidKind fluid, int amountMb) {
        if (amountMb <= 0 || fluid == null || fluid.isEmpty()) {
            blitCommon(graphics, x, y, 70, 100, 20, 55);
            return;
        }
        blitCommon(graphics, x, y, 6, 100, 20, 55);
        float fillRatio = Math.max(0.0F, Math.min(1.0F, amountMb / (float) TANK_CAPACITY_MB));
        int fluidHeight = Math.round(47.0F * fillRatio);
        if (fluidHeight > 0) {
            int fluidY = y + 4 + (47 - fluidHeight);
            graphics.fill(x + 4, fluidY, x + 16, y + 51, fluid.tintArgb());
        }
        blitCommon(graphics, x, y, 38, 100, 20, 55);
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

    private static void blitCommon(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(COMMON, x, y, u, v, width, height);
    }
}
