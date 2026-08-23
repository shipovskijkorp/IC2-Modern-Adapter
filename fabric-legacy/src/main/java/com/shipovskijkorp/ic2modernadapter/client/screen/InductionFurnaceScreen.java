package com.shipovskijkorp.ic2modernadapter.client.screen;

import com.shipovskijkorp.ic2modernadapter.menu.InductionFurnaceMenu;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** IC2 Induction Furnace GUI using the original IC2 layout. */
public final class InductionFurnaceScreen extends AbstractContainerScreen<InductionFurnaceMenu> {
    private static final ResourceLocation COMMON = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/common.png"));
    private static final ResourceLocation INPUT_OVERLAY = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/overlay/induction_furnace_input.png"));
    private static final ResourceLocation OUTPUT_OVERLAY = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/overlay/induction_furnace_output.png"));
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    private static final int ENERGY_X = 55;
    private static final int ENERGY_Y = 37;

    public InductionFurnaceScreen(InductionFurnaceMenu menu, Inventory inventory, Component title) {
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
        this.inventoryLabelY = 73;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(ENERGY_X - 4, ENERGY_Y - 1, 16, 16, mouseX, mouseY)) {
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

        graphics.blit(INPUT_OVERLAY, leftPos + 42, topPos + 16, 0, 0, 34, 18, 34, 18);
        graphics.blit(OUTPUT_OVERLAY, leftPos + 110, topPos + 30, 0, 0, 38, 26, 38, 26);
        drawSlot(graphics, leftPos + 50, topPos + 52);
        for (int slot = 0; slot < 2; slot++) {
            drawSlot(graphics, leftPos + 151, topPos + 25 + slot * 18);
        }

        drawEnergyBolt(graphics, leftPos + ENERGY_X, topPos + ENERGY_Y, ratio(menu.getEuStored(), menu.getEuCapacity()));
        drawProgressArrow(graphics, leftPos + 81, topPos + 35, ratio(menu.getProgress(), menu.getMaxProgress()));

        graphics.drawString(this.font, Component.translatable("ic2.generic.text.heat"), leftPos + 10, topPos + 36, 0x404040, false);
        graphics.drawString(this.font, Component.literal(menu.getHeatPercent() + "%"), leftPos + 10, topPos + 46, 0x404040, false);

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

    private static void drawLargeSlot(GuiGraphics graphics, int x, int y) {
        blit(graphics, x, y, 99, 35, 26, 26);
    }

    private static void drawProgressArrow(GuiGraphics graphics, int x, int y, float ratio) {
        blit(graphics, x - 5, y, 160, 0, 32, 16);
        int width = (int) Math.floor(22.0F * clamp01(ratio));
        if (width > 0) {
            blit(graphics, x, y, 165, 16, width, 15);
        }
    }

    private static void drawEnergyBolt(GuiGraphics graphics, int x, int y, float ratio) {
        blit(graphics, x - 4, y - 1, 96, 64, 16, 16);
        int height = Math.round(13.0F * clamp01(ratio));
        if (height <= 0) {
            return;
        }
        int offset = 13 - height;
        blit(graphics, x, y + offset, 116, 65 + offset, 7, height);
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

    private static void blit(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(COMMON, x, y, u, v, width, height);
    }
}
