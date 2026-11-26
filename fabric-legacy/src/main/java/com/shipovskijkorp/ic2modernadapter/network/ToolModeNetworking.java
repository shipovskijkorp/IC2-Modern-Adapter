package com.shipovskijkorp.ic2modernadapter.network;

import com.shipovskijkorp.ic2modernadapter.content.item.tool.ToolModeModule;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public final class ToolModeNetworking {
    public static final ResourceLocation ID = new ResourceLocation("ic2_modern_adapter", "toggle_tool_mode");
    private ToolModeNetworking() {}
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            if (!buf.isReadable()) server.execute(() -> ToolModeModule.toggle(player));
        });
    }
}
