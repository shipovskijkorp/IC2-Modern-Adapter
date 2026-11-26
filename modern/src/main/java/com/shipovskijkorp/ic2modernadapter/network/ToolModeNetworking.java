package com.shipovskijkorp.ic2modernadapter.network;

import com.shipovskijkorp.ic2modernadapter.content.item.tool.ToolModeModule;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ToolModeNetworking {
    private ToolModeNetworking() {}
    public static void register(IEventBus bus) { bus.addListener(ToolModeNetworking::registerPayload); }
    private static void registerPayload(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(ToggleToolModePayload.TYPE, ToggleToolModePayload.CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) ToolModeModule.toggle(player);
                });
    }
}
