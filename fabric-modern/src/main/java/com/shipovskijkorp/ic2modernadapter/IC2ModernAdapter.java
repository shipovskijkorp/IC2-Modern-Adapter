package com.shipovskijkorp.ic2modernadapter;

import com.shipovskijkorp.ic2modernadapter.creative.IC2CreativeTab;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.fabricmc.api.ModInitializer;

/** Fabric common entrypoint. */
public final class IC2ModernAdapter implements ModInitializer {
    public static final String MOD_ID = "ic2_modern_adapter";

    @Override
    public void onInitialize() {
        IC2ContentRegistries.register();
        IC2CreativeTab.register();
    }
}
