package com.shipovskijkorp.ic2modernadapter.client.screen;

import com.shipovskijkorp.ic2modernadapter.menu.MetalFormerMenu;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Original IC2 Metal Former GUI with mode switching and JEI recipe button coordinates. */
public final class MetalFormerScreen extends AbstractContainerScreen<MetalFormerMenu> {
    private static final ResourceLocation GUI = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/guimetalformer.png"));
    private static final ResourceLocation COMMON = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/common.png"));
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    private static final int MODE_BUTTON_ID = 0;
    private static final int MODE_BUTTON_X = 65;
    private static final int MODE_BUTTON_Y = 53;
    private static final int MODE_BUTTON_SIZE = 20;
    private static final int ENERGY_X = 20;
    private static final int ENERGY_Y = 37;
    private static final int PROGRESS_X = 52;
    private static final int PROGRESS_Y = 40;

    public MetalFormerScreen(MetalFormerMenu menu, Inventory inventory, Component title) {
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
        addRenderableWidget(Button.builder(Component.empty(), button -> cycleMode())
                .bounds(leftPos + MODE_BUTTON_X, topPos + MODE_BUTTON_Y, MODE_BUTTON_SIZE, MODE_BUTTON_SIZE)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderModeIcon(graphics);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(MODE_BUTTON_X, MODE_BUTTON_Y, MODE_BUTTON_SIZE, MODE_BUTTON_SIZE, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.translatable(menu.getMode().tooltipKey()), mouseX, mouseY);
        }
        if (isHovering(ENERGY_X - 4, ENERGY_Y - 1, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(this.font,
                    Component.literal(menu.getEuStored() + "/" + menu.getEuCapacity() + " EU"),
                    mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GUI, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        drawEnergyBoltFill(graphics, leftPos + ENERGY_X, topPos + ENERGY_Y, ratio(menu.getEuStored(), menu.getEuCapacity()));
        drawProgressMetalFormer(graphics, leftPos + PROGRESS_X, topPos + PROGRESS_Y,
                ratio(menu.getProgress(), menu.getMaxProgress()));
    }

    private void cycleMode() {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, MODE_BUTTON_ID);
        }
    }

    private void renderModeIcon(GuiGraphics graphics) {
        graphics.renderItem(menu.getMode().icon(), leftPos + 67, topPos + 55);
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

    private static void drawProgressMetalFormer(GuiGraphics graphics, int x, int y, float ratio) {
        int width = Math.round(46.0F * clamp01(ratio));
        if (width > 0) {
            blitCommon(graphics, x, y - 1, 200, 19, width, 9);
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
