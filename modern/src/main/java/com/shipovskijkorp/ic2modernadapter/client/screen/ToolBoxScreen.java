package com.shipovskijkorp.ic2modernadapter.client.screen;

import com.shipovskijkorp.ic2modernadapter.toolbox.ToolBoxMenu;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Original IC2 Tool Box GUI. */
public final class ToolBoxScreen extends AbstractContainerScreen<ToolBoxMenu> {
    private static final ResourceLocation GUI = Objects.requireNonNull(
            ResourceLocation.tryParse("ic2:textures/gui/guitoolbox.png"));
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    public ToolBoxScreen(ToolBoxMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 65;
        this.titleLabelY = 11;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 73;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GUI, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
