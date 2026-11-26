package com.shipovskijkorp.ic2modernadapter.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleToolModePayload() implements CustomPacketPayload {
    public static final ToggleToolModePayload INSTANCE = new ToggleToolModePayload();
    public static final Type<ToggleToolModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ic2_modern_adapter", "toggle_tool_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleToolModePayload> CODEC = StreamCodec.unit(INSTANCE);
    @Override public Type<ToggleToolModePayload> type() { return TYPE; }
}
