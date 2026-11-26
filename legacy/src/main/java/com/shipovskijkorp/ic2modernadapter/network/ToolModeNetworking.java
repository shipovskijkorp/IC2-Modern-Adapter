package com.shipovskijkorp.ic2modernadapter.network;

import com.shipovskijkorp.ic2modernadapter.content.item.tool.ToolModeModule;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ToolModeNetworking {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("ic2_modern_adapter", "tool_mode"), () -> VERSION, VERSION::equals, VERSION::equals);
    private ToolModeNetworking() {}
    public record Toggle() {}
    public static void register() {
        CHANNEL.registerMessage(0, Toggle.class, (msg, buf) -> {}, buf -> new Toggle(), (msg, ctx) -> {
            var context = ctx.get();
            var player = context.getSender();
            if (player != null) context.enqueueWork(() -> ToolModeModule.toggle(player));
            context.setPacketHandled(true);
        }, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}
