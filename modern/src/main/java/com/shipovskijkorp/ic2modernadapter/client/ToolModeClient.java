package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.content.item.tool.ToolModeModule;
import com.shipovskijkorp.ic2modernadapter.network.ToggleToolModePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = IC2ModernAdapter.MOD_ID, value = Dist.CLIENT)
public final class ToolModeClient {
    private static final KeyMapping KEY = new KeyMapping(ToolModeModule.KEY, GLFW.GLFW_KEY_M, ToolModeModule.CATEGORY);
    private ToolModeClient() {}
    @SubscribeEvent public static void register(RegisterKeyMappingsEvent event) { event.register(KEY); }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        while (KEY.consumeClick()) {
            if (Minecraft.getInstance().player != null) PacketDistributor.sendToServer(ToggleToolModePayload.INSTANCE);
        }
    }
}
