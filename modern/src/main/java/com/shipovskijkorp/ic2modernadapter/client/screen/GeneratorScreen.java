package com.shipovskijkorp.ic2modernadapter.client.screen;

import com.shipovskijkorp.ic2modernadapter.menu.GeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** IC2 Experimental 2.8.222 Generator GUI recreated from guidef/generator.xml and common.png. */
public final class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    private static final ResourceLocation COMMON = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/common.png");
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    private static final int ENERGY_X = 100;
    private static final int ENERGY_Y = 39;

    public GeneratorScreen(GeneratorMenu menu, Inventory inventory, Component title) {
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
        this.inventoryLabelY = 72;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(ENERGY_X - 4, ENERGY_Y - 11, 32, 32, mouseX, mouseY)) {
            graphics.renderTooltip(
                    this.font,
                    Component.literal(menu.getEuStored() + "/" + menu.getEuCapacity() + " EU"),
                    mouseX,
                    mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawDefaultBackground(graphics, leftPos, topPos, imageWidth, imageHeight);

        drawSlot(graphics, leftPos + 56, topPos + 16);
        drawSlot(graphics, leftPos + 56, topPos + 52);

        int inventoryX = leftPos + 7;
        int inventoryY = topPos + 83;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(graphics, inventoryX + column * 18, inventoryY + row * 18);
            }
        }
        int hotbarY = inventoryY + 58;
        for (int column = 0; column < 9; column++) {
            drawSlot(graphics, inventoryX + column * 18, hotbarY);
        }

        drawFuelGauge(graphics, leftPos + 57, topPos + 36, menu.getFuel(), menu.getTotalFuel());
        drawEnergyGauge(graphics, leftPos + ENERGY_X, topPos + ENERGY_Y, menu.getEuStored(), menu.getEuCapacity());
    }

    private static void drawDefaultBackground(GuiGraphics graphics, int x, int y, int width, int height) {
        blit(graphics, x - 16, y - 16, 0, 0, 32, 32);
        blit(graphics, x + width - 16, y - 16, 64, 0, 32, 32);
        blit(graphics, x - 16, y + height - 16, 0, 64, 32, 32);
        blit(graphics, x + width - 16, y + height - 16, 64, 64, 32, 32);

        for (int dx = 16; dx < width - 16; dx += 32) {
            int drawWidth = Math.min(32, width - 16 - dx);
            blit(graphics, x + dx, y - 16, 32, 0, drawWidth, 32);
            blit(graphics, x + dx, y + height - 16, 32, 64, drawWidth, 32);
        }
        for (int dy = 16; dy < height - 16; dy += 32) {
            int drawHeight = Math.min(32, height - 16 - dy);
            blit(graphics, x - 16, y + dy, 0, 32, 32, drawHeight);
            blit(graphics, x + width - 16, y + dy, 64, 32, 32, drawHeight);
            for (int dx = 16; dx < width - 16; dx += 32) {
                int drawWidth = Math.min(32, width - 16 - dx);
                blit(graphics, x + dx, y + dy, 32, 32, drawWidth, drawHeight);
            }
        }
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        blit(graphics, x, y, 103, 7, 18, 18);
    }

    private static void drawFuelGauge(GuiGraphics graphics, int x, int y, int fuel, int totalFuel) {
        // GaugeStyle.Fuel: background (96,80) 16x16, inner (112,80) 13x13, orientation Up.
        blit(graphics, x, y, 96, 80, 16, 16);
        if (fuel <= 0 || totalFuel <= 0) {
            return;
        }
        int height = Math.max(0, Math.min(13, Math.round(13.0F * fuel / (float) totalFuel)));
        if (height <= 0) {
            return;
        }
        int offset = 13 - height;
        blit(graphics, x, y + offset, 112, 80 + offset, 13, height);
    }

    private static void drawEnergyGauge(GuiGraphics graphics, int x, int y, int stored, int capacity) {
        // EnergyGaugeStyle.Bar: background offset (-4,-11), inner 24x9 from (132,43), left-to-right.
        blit(graphics, x - 4, y - 11, 128, 0, 32, 32);
        if (stored <= 0 || capacity <= 0) {
            return;
        }
        int width = Math.max(0, Math.min(24, Math.round(24.0F * stored / (float) capacity)));
        if (width > 0) {
            blit(graphics, x, y, 132, 43, width, 9);
        }
    }

    private static void blit(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(COMMON, x, y, u, v, width, height);
    }
}
