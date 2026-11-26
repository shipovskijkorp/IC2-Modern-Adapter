package com.shipovskijkorp.ic2modernadapter.network;

import com.shipovskijkorp.ic2modernadapter.content.item.armor.NightVisionModule;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class NightVisionNetworking {
    private NightVisionNetworking() {}
    public static void register(IEventBus bus) { bus.addListener(NightVisionNetworking::registerPayload); }
    private static void registerPayload(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(ToggleNightVisionPayload.TYPE, ToggleNightVisionPayload.CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) NightVisionModule.toggle(player);
                });
    }
}
