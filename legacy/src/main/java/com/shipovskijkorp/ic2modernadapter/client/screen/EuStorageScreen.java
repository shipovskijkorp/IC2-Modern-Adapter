package com.shipovskijkorp.ic2modernadapter.client.screen;

import com.shipovskijkorp.ic2modernadapter.menu.EuStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** IC2 Experimental electric-storage GUI backed entirely by runtime assets from the original JAR. */
public final class EuStorageScreen extends AbstractContainerScreen<EuStorageMenu> {
    private static final ResourceLocation BACKGROUND =
            new ResourceLocation("ic2", "textures/gui/guielectricblock.png");
    private static final ResourceLocation COMMON =
            new ResourceLocation("ic2", "textures/gui/common.png");
    private static final ResourceLocation BUTTON =
            new ResourceLocation("ic2_modern_adapter", "textures/gui/button.png");
    private static final ResourceLocation BUTTON_HOVER =
            new ResourceLocation("ic2_modern_adapter", "textures/gui/button_enabled.png");
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 196;
    private static final int ENERGY_X = 79;
    private static final int ENERGY_Y = 38;
    private static final int REDSTONE_X = 152;
    private static final int REDSTONE_Y = 4;
    private static final int REDSTONE_SIZE = 20;

    public EuStorageScreen(EuStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 103;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(ENERGY_X - 4, ENERGY_Y - 11, 32, 32, mouseX, mouseY)) {
            graphics.renderTooltip(
                    font,
                    Component.literal(menu.getEuStored() + "/" + menu.getEuCapacity() + " EU"),
                    mouseX,
                    mouseY);
        }
        if (isHovering(REDSTONE_X, REDSTONE_Y, REDSTONE_SIZE, REDSTONE_SIZE, mouseX, mouseY)) {
            graphics.renderTooltip(
                    font,
                    Component.translatable("ic2.EUStorage.gui.mod.redstone" + menu.getRedstoneMode()),
                    mouseX,
                    mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        drawEnergyGauge(graphics, leftPos + ENERGY_X, topPos + ENERGY_Y, menu.getEuStored(), menu.getEuCapacity());
        drawRedstoneButton(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.drawString(font, Component.translatable("ic2.EUStorage.gui.info.level"), 79, 25, 0x404040, false);
        graphics.drawString(font, Component.literal(" " + menu.getEuStored()), 110, 35, 0x404040, false);
        graphics.drawString(font, Component.literal("/" + menu.getEuCapacity()), 110, 45, 0x404040, false);
        graphics.drawString(
                font,
                Component.translatable("ic2.EUStorage.gui.info.output", menu.getOutputEuPerTick()),
                85,
                60,
                0x404040,
                false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovering(REDSTONE_X, REDSTONE_Y, REDSTONE_SIZE, REDSTONE_SIZE, mouseX, mouseY)) {
            if (minecraft != null && minecraft.player != null && minecraft.gameMode != null) {
                menu.clickMenuButton(minecraft.player, EuStorageMenu.BUTTON_CYCLE_REDSTONE);
                minecraft.gameMode.handleInventoryButtonClick(
                        menu.containerId(), EuStorageMenu.BUTTON_CYCLE_REDSTONE);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawRedstoneButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = leftPos + REDSTONE_X;
        int y = topPos + REDSTONE_Y;
        ResourceLocation texture = isHovering(
                REDSTONE_X, REDSTONE_Y, REDSTONE_SIZE, REDSTONE_SIZE, mouseX, mouseY)
                ? BUTTON_HOVER
                : BUTTON;
        graphics.blit(texture, x, y, 0, 0, REDSTONE_SIZE, REDSTONE_SIZE, REDSTONE_SIZE, REDSTONE_SIZE);
        graphics.renderItem(new ItemStack(Items.REDSTONE), x + 2, y + 2);
    }

    private static void drawEnergyGauge(GuiGraphics graphics, int x, int y, int stored, int capacity) {
        // Same EnergyGaugeStyle.Bar atlas region used by original GuiElectricBlock.
        graphics.blit(COMMON, x - 4, y - 11, 128, 0, 32, 32);
        if (stored <= 0 || capacity <= 0) {
            return;
        }
        int width = Math.max(0, Math.min(24, Math.round(24.0F * stored / (float) capacity)));
        if (width > 0) {
            graphics.blit(COMMON, x, y, 132, 43, width, 9);
        }
    }
}
