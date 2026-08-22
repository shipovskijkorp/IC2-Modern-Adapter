package com.shipovskijkorp.ic2modernadapter;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(IC2ModernAdapter.MOD_ID)
public final class IC2ModernAdapter {
    public static final String MOD_ID = "ic2_modern_adapter";

    public IC2ModernAdapter() {
        IC2ContentRegistries.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
