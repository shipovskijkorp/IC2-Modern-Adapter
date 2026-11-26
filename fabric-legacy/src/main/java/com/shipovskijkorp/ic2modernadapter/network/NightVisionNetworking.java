package com.shipovskijkorp.ic2modernadapter.network;

import com.shipovskijkorp.ic2modernadapter.content.item.armor.NightVisionModule;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public final class NightVisionNetworking {
    public static final ResourceLocation ID = new ResourceLocation("ic2_modern_adapter", "toggle_nightvision");
    private NightVisionNetworking() {}
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            if (!buf.isReadable()) server.execute(() -> NightVisionModule.toggle(player));
        });
    }
}
