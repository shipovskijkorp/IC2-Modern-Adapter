package com.shipovskijkorp.ic2modernadapter.content.item.tool;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** The client sends a mode-key intent, never an item, mode, charge or arbitrary target. */
public final class ToolModeModule {
    public static final String KEY = "key.ic2ma.tool_mode";
    public static final String CATEGORY = "key.categories.ic2_modern_adapter";
    private ToolModeModule() {}
    public static void toggle(ServerPlayer player) {
        if (player.isSpectator()) return;
        ItemStack stack = player.getMainHandItem();
        boolean changed = false;
        if (stack.getItem() instanceof ElectricMiningToolItem tool) changed = tool.cycleMode(stack, player);
        else if (stack.getItem() instanceof PainterItem roller) changed = roller.cycleMode(stack, player);
        if (changed) { player.getInventory().setChanged(); player.inventoryMenu.broadcastChanges(); }
    }
}
