package com.shipovskijkorp.ic2modernadapter.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleNightVisionPayload() implements CustomPacketPayload {
    public static final ToggleNightVisionPayload INSTANCE = new ToggleNightVisionPayload();
    public static final Type<ToggleNightVisionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ic2_modern_adapter", "toggle_nightvision"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleNightVisionPayload> CODEC = StreamCodec.unit(INSTANCE);
    @Override public Type<ToggleNightVisionPayload> type() { return TYPE; }
}
