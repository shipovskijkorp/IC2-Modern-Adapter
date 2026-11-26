package com.shipovskijkorp.ic2modernadapter.network;

import com.shipovskijkorp.ic2modernadapter.content.item.tool.ToolModeModule;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ToolModeNetworking {
    private ToolModeNetworking() {}
    public static void register() {
        PayloadTypeRegistry.playC2S().register(ToggleToolModePayload.TYPE, ToggleToolModePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleToolModePayload.TYPE,
                (payload, context) -> ToolModeModule.toggle(context.player()));
    }
}
