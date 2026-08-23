package com.shipovskijkorp.ic2modernadapter.client.screen;

import com.shipovskijkorp.ic2modernadapter.menu.OreWashingPlantMenu;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Original IC2 Ore Washing Plant GUI. */
public final class OreWashingPlantScreen extends AbstractContainerScreen<OreWashingPlantMenu> {
    private static final ResourceLocation GUI = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/guiorewashingplant.png"));
    private static final ResourceLocation COMMON = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/common.png"));
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    private static final int ENERGY_X = 12;
    private static final int ENERGY_Y = 44;
    private static final int TANK_X = 60;
    private static final int TANK_Y = 20;
    private static final int TANK_WIDTH = 20;
    private static final int TANK_HEIGHT = 55;
    private static final int PROGRESS_X = 103;
    private static final int PROGRESS_Y = 39;
    private static final int WATER_COLOR = 0xCC3F76E4;

    public OreWashingPlantScreen(OreWashingPlantMenu menu, Inventory inventory, Component title) {
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
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(ENERGY_X - 4, ENERGY_Y - 1, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(menu.getEuStored() + "/" + menu.getEuCapacity() + " EU"),
                    mouseX, mouseY);
        }
        if (isHovering(TANK_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(menu.getWaterMb() + "/" + menu.getWaterCapacityMb() + " mB"),
                    mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GUI, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        drawWaterTank(graphics, leftPos + TANK_X, topPos + TANK_Y, ratio(menu.getWaterMb(), menu.getWaterCapacityMb()));
        drawEnergyBoltFill(graphics, leftPos + ENERGY_X, topPos + ENERGY_Y, ratio(menu.getEuStored(), menu.getEuCapacity()));
        drawOreWasherProgress(graphics, leftPos + PROGRESS_X, topPos + PROGRESS_Y,
                ratio(menu.getProgress(), menu.getMaxProgress()));
    }

    private static void drawEnergyBoltFill(GuiGraphics graphics, int x, int y, float ratio) {
        blitCommon(graphics, x - 4, y - 1, 96, 64, 16, 16);
        int height = Math.round(13.0F * clamp01(ratio));
        if (height <= 0) {
            return;
        }
        int offset = 13 - height;
        blitCommon(graphics, x, y + offset, 116, 65 + offset, 7, height);
    }

    private static void drawWaterTank(GuiGraphics graphics, int x, int y, float ratio) {
        float fillRatio = clamp01(ratio);
        if (fillRatio <= 0.0F) {
            blitCommon(graphics, x, y, 70, 100, 20, 55);
            return;
        }
        blitCommon(graphics, x, y, 6, 100, 20, 55);
        int fluidHeight = Math.round(47.0F * fillRatio);
        if (fluidHeight > 0) {
            int fluidY = y + 4 + (47 - fluidHeight);
            graphics.fill(x + 4, fluidY, x + 16, y + 51, WATER_COLOR);
        }
        blitCommon(graphics, x, y, 38, 100, 20, 55);
    }

    private static void drawOreWasherProgress(GuiGraphics graphics, int x, int y, float ratio) {
        int width = Math.round(18.0F * clamp01(ratio));
        if (width > 0) {
            graphics.blit(GUI, x, y, 177, 118, width, 18);
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
