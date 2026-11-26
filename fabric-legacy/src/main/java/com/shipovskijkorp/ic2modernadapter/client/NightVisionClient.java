package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.content.item.armor.NightVisionModule;
import com.shipovskijkorp.ic2modernadapter.network.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class NightVisionClient {
    private NightVisionClient() {}
    public static void register() {
        KeyMapping key = KeyBindingHelper.registerKeyBinding(new KeyMapping(NightVisionModule.KEY, GLFW.GLFW_KEY_N, NightVisionModule.CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (key.consumeClick()) {
                if (client.player != null && ClientPlayNetworking.canSend(NightVisionNetworking.ID))
                    ClientPlayNetworking.send(NightVisionNetworking.ID, net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create());
            }
        });
    }
}
