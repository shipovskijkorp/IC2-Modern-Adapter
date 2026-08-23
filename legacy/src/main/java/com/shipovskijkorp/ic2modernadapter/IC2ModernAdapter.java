package com.shipovskijkorp.ic2modernadapter;

import com.shipovskijkorp.ic2modernadapter.client.InDevTooltips;
import com.shipovskijkorp.ic2modernadapter.creative.IC2CreativeTab;
import com.shipovskijkorp.ic2modernadapter.energy.ForgeEuNetworkLifecycle;
import com.shipovskijkorp.ic2modernadapter.generator.GeneratorPlatform;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStoragePlatform;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import com.shipovskijkorp.ic2modernadapter.resource.IC2RuntimeResources;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(IC2ModernAdapter.MOD_ID)
public final class IC2ModernAdapter {
    public static final String MOD_ID = "ic2_modern_adapter";

    public IC2ModernAdapter() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IC2ContentRegistries.register(modEventBus);
        GeneratorPlatform.register(modEventBus);
        EuStoragePlatform.register(modEventBus);
        IC2CreativeTab.register(modEventBus);
        modEventBus.addListener(IC2RuntimeResources::onAddPackFinders);
        MinecraftForge.EVENT_BUS.addListener(InDevTooltips::onItemTooltip);
        ForgeEuNetworkLifecycle.register();
    }
}
