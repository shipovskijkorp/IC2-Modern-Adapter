package com.shipovskijkorp.ic2modernadapter;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(IC2ModernAdapter.MOD_ID)
public final class IC2ModernAdapter {
    public static final String MOD_ID = "ic2_modern_adapter";

    public IC2ModernAdapter(IEventBus modEventBus) {
        IC2ContentRegistries.register(modEventBus);
    }
}
