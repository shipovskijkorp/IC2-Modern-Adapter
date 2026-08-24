package com.shipovskijkorp.ic2modernadapter.client.screen;

import com.shipovskijkorp.ic2modernadapter.machine.CannerMode;
import com.shipovskijkorp.ic2modernadapter.machine.Ic2FluidKind;
import com.shipovskijkorp.ic2modernadapter.menu.CannerMenu;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Original IC2 Fluid/Solid Canning Machine GUI. */
public final class CannerScreen extends AbstractContainerScreen<CannerMenu> {
    private static final ResourceLocation GUI = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/guicanner.png"));
    private static final ResourceLocation COMMON = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/common.png"));
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 184;
    private static final int ENERGY_X = 12;
    private static final int ENERGY_Y = 62;
    private static final int INPUT_TANK_X = 39;
    private static final int OUTPUT_TANK_X = 117;
    private static final int TANK_Y = 42;
    private static final int TANK_WIDTH = 20;
    private static final int TANK_HEIGHT = 55;
    private static final int MODE_BUTTON_X = 63;
    private static final int MODE_BUTTON_Y = 81;
    private static final int MODE_BUTTON_W = 50;
    private static final int MODE_BUTTON_H = 14;
    private static final int SWAP_BUTTON_X = 77;
    private static final int SWAP_BUTTON_Y = 64;
    private static final int SWAP_BUTTON_W = 22;
    private static final int SWAP_BUTTON_H = 13;
    private static final int PROGRESS_X = 74;
    private static final int PROGRESS_Y = 22;

    public CannerScreen(CannerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 91;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderCannerTooltips(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GUI, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        drawModeOverlay(graphics, menu.getMode());
        drawTank(graphics, leftPos + INPUT_TANK_X, topPos + TANK_Y,
                menu.getInputTankFluid(), ratio(menu.getInputTankAmount(), menu.getTankCapacity()));
        drawTank(graphics, leftPos + OUTPUT_TANK_X, topPos + TANK_Y,
                menu.getOutputTankFluid(), ratio(menu.getOutputTankAmount(), menu.getTankCapacity()));
        drawEnergyBoltFill(graphics, leftPos + ENERGY_X, topPos + ENERGY_Y, ratio(menu.getEuStored(), menu.getEuCapacity()));
        drawProgress(graphics, leftPos + PROGRESS_X, topPos + PROGRESS_Y, ratio(menu.getProgress(), menu.getMaxProgress()));
        drawModeButton(graphics, menu.getMode());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && minecraft != null && minecraft.gameMode != null) {
            if (isInside(mouseX, mouseY, MODE_BUTTON_X, MODE_BUTTON_Y, MODE_BUTTON_W, MODE_BUTTON_H)) {
                int next = menu.getMode().next().id();
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, CannerMenu.BUTTON_MODE_BASE + next);
                return true;
            }
            if (isInside(mouseX, mouseY, SWAP_BUTTON_X, SWAP_BUTTON_Y, SWAP_BUTTON_W, SWAP_BUTTON_H)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, CannerMenu.BUTTON_SWAP_TANKS);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderCannerTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isHovering(ENERGY_X - 4, ENERGY_Y - 1, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(menu.getEuStored() + "/" + menu.getEuCapacity() + " EU"),
                    mouseX, mouseY);
        } else if (isHovering(INPUT_TANK_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    tankTooltip(menu.getInputTankFluid(), menu.getInputTankAmount()), mouseX, mouseY);
        } else if (isHovering(OUTPUT_TANK_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    tankTooltip(menu.getOutputTankFluid(), menu.getOutputTankAmount()), mouseX, mouseY);
        } else if (isHovering(MODE_BUTTON_X, MODE_BUTTON_Y, MODE_BUTTON_W, MODE_BUTTON_H, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.translatable(menu.getMode().tooltipKey()), mouseX, mouseY);
        } else if (isHovering(SWAP_BUTTON_X, SWAP_BUTTON_Y, SWAP_BUTTON_W, SWAP_BUTTON_H, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.translatable("ic2.Canner.gui.switchTanks"), mouseX, mouseY);
        }
    }

    private void drawModeButton(GuiGraphics graphics, CannerMode mode) {
        graphics.blit(GUI, leftPos + MODE_BUTTON_X, topPos + MODE_BUTTON_Y, 176, 18 + mode.id() * 14, MODE_BUTTON_W, MODE_BUTTON_H);
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + width
                && mouseY >= topPos + y && mouseY < topPos + y + height;
    }

    private static Component tankTooltip(Ic2FluidKind fluid, int amount) {
        return Component.literal(fluid.displayName() + ": " + amount + "/8000 mB");
    }

    private void drawModeOverlay(GuiGraphics graphics, CannerMode mode) {
        if (mode == CannerMode.BOTTLE_SOLID) {
            graphics.blit(GUI, leftPos + 59, topPos + 53, 3, 4, 9, 18);
            graphics.blit(GUI, leftPos + 99, topPos + 53, 3, 4, 18, 23);
        } else if (mode == CannerMode.EMPTY_LIQUID) {
            graphics.blit(GUI, leftPos + 71, topPos + 43, 196, 0, 26, 18);
            graphics.blit(GUI, leftPos + 59, topPos + 53, 3, 4, 9, 18);
        } else if (mode == CannerMode.BOTTLE_LIQUID) {
            graphics.blit(GUI, leftPos + 99, topPos + 53, 3, 4, 18, 23);
            graphics.blit(GUI, leftPos + 71, topPos + 43, 196, 0, 26, 18);
        }
    }

    private static void drawTank(GuiGraphics graphics, int x, int y, Ic2FluidKind fluid, float ratio) {
        float fillRatio = clamp01(ratio);
        if (fillRatio <= 0.0F || fluid == null || fluid.isEmpty()) {
            blitCommon(graphics, x, y, 70, 100, 20, 55);
            return;
        }
        blitCommon(graphics, x, y, 6, 100, 20, 55);
        int fluidHeight = Math.round(47.0F * fillRatio);
        if (fluidHeight > 0) {
            int fluidY = y + 4 + (47 - fluidHeight);
            graphics.fill(x + 4, fluidY, x + 16, y + 51, fluid.tintArgb());
        }
        blitCommon(graphics, x, y, 38, 100, 20, 55);
    }

    private static void drawEnergyBoltFill(GuiGraphics graphics, int x, int y, float ratio) {
        int height = Math.round(13.0F * clamp01(ratio));
        if (height <= 0) return;
        int offset = 13 - height;
        blitCommon(graphics, x, y + offset, 116, 65 + offset, 7, height);
    }

    private static void drawProgress(GuiGraphics graphics, int x, int y, float ratio) {
        int width = Math.round(23.0F * clamp01(ratio));
        if (width > 0) {
            graphics.blit(GUI, x, y, 233, 0, width, 14);
        }
    }

    private static float ratio(int value, int max) { return max <= 0 ? 0.0F : value / (float) max; }
    private static float clamp01(float value) { return value < 0.0F ? 0.0F : Math.min(1.0F, value); }
    private static void blitCommon(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(COMMON, x, y, u, v, width, height);
    }
}
