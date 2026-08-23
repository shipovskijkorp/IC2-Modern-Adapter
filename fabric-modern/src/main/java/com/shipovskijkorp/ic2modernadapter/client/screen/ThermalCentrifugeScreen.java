package com.shipovskijkorp.ic2modernadapter.client.screen;

import com.shipovskijkorp.ic2modernadapter.menu.ThermalCentrifugeMenu;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Original IC2 Thermal Centrifuge GUI. */
public final class ThermalCentrifugeScreen extends AbstractContainerScreen<ThermalCentrifugeMenu> {
    private static final ResourceLocation GUI = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/guitermalcentrifuge.png"));
    private static final ResourceLocation COMMON = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/common.png"));
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    private static final int ENERGY_X = 15;
    private static final int ENERGY_Y = 42;
    private static final int PROGRESS_X = 84;
    private static final int PROGRESS_Y = 25;
    private static final int HEAT_X = 68;
    private static final int HEAT_Y = 67;

    public ThermalCentrifugeScreen(ThermalCentrifugeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelY = 10000;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(ENERGY_X - 4, ENERGY_Y - 1, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(menu.getEuStored() + "/" + menu.getEuCapacity() + " EU"),
                    mouseX,
                    mouseY);
        }
        if (isHovering(HEAT_X - 1, HEAT_Y - 1, 22, 6, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(menu.getHeat() + "/" + menu.getWorkHeat() + " Heat"),
                    mouseX,
                    mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GUI, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        drawEnergyBoltFill(graphics, leftPos + ENERGY_X, topPos + ENERGY_Y, ratio(menu.getEuStored(), menu.getEuCapacity()));
        drawCentrifugeProgress(graphics, leftPos + PROGRESS_X, topPos + PROGRESS_Y,
                ratio(menu.getProgress(), menu.getMaxProgress()));
        drawHeatGauge(graphics, leftPos + HEAT_X, topPos + HEAT_Y, ratio(menu.getHeat(), menu.getWorkHeat()));
    }

    private static void drawEnergyBoltFill(GuiGraphics graphics, int x, int y, float ratio) {
        int height = Math.round(13.0F * clamp01(ratio));
        if (height <= 0) {
            return;
        }
        int offset = 13 - height;
        blitCommon(graphics, x, y + offset, 116, 65 + offset, 7, height);
    }

    private static void drawCentrifugeProgress(GuiGraphics graphics, int x, int y, float ratio) {
        int height = Math.round(28.0F * clamp01(ratio));
        if (height <= 0) {
            return;
        }
        int offset = 28 - height;
        blitCommon(graphics, x, y + offset, 252, 33 + offset, 3, height);
    }

    private static void drawHeatGauge(GuiGraphics graphics, int x, int y, float ratio) {
        int width = Math.round(20.0F * clamp01(ratio));
        if (width > 0) {
            blitCommon(graphics, x, y, 225, 54, width, 4);
        }
    }

    private static float ratio(int value, int max) {
        return max <= 0 ? 0.0F : value / (float) max;
    }

    private static float clamp01(float value) {
        if (value < 0.0F) {
            return 0.0F;
        }
        return value > 1.0F ? 1.0F : value;
    }

    private static void blitCommon(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(COMMON, x, y, u, v, width, height);
    }
}
