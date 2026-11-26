package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.content.item.tool.ToolModeModule;
import com.shipovskijkorp.ic2modernadapter.network.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ToolModeClient {
    private ToolModeClient() {}
    public static void register() {
        KeyMapping key = KeyBindingHelper.registerKeyBinding(new KeyMapping(ToolModeModule.KEY, GLFW.GLFW_KEY_M, ToolModeModule.CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (key.consumeClick()) {
                if (client.player != null && ClientPlayNetworking.canSend(ToggleToolModePayload.TYPE))
                    ClientPlayNetworking.send(ToggleToolModePayload.INSTANCE);
            }
        });
    }
}
