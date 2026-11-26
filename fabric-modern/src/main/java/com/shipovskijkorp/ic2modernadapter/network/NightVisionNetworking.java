package com.shipovskijkorp.ic2modernadapter.network;

import com.shipovskijkorp.ic2modernadapter.content.item.armor.NightVisionModule;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class NightVisionNetworking {
    private NightVisionNetworking() {}
    public static void register() {
        PayloadTypeRegistry.playC2S().register(ToggleNightVisionPayload.TYPE, ToggleNightVisionPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleNightVisionPayload.TYPE,
                (payload, context) -> NightVisionModule.toggle(context.player()));
    }
}
