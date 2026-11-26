package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.content.item.tool.ToolModeModule;
import com.shipovskijkorp.ic2modernadapter.network.ToolModeNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = IC2ModernAdapter.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ToolModeClient {
    private static final KeyMapping KEY = new KeyMapping(ToolModeModule.KEY, GLFW.GLFW_KEY_M, ToolModeModule.CATEGORY);
    private ToolModeClient() {}
    @SubscribeEvent public static void register(RegisterKeyMappingsEvent event) { event.register(KEY); }
    @Mod.EventBusSubscriber(modid = IC2ModernAdapter.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Ticks {
        @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            while (KEY.consumeClick()) {
                if (Minecraft.getInstance().player != null) ToolModeNetworking.CHANNEL.sendToServer(new ToolModeNetworking.Toggle());
            }
        }
    }
}
