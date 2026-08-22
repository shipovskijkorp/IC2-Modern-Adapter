package com.shipovskijkorp.ic2modernadapter;

import com.shipovskijkorp.ic2modernadapter.client.InDevTooltips;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import com.shipovskijkorp.ic2modernadapter.resource.IC2RuntimeResources;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(IC2ModernAdapter.MOD_ID)
public final class IC2ModernAdapter {
    public static final String MOD_ID = "ic2_modern_adapter";

    public IC2ModernAdapter(IEventBus modEventBus) {
        IC2ContentRegistries.register(modEventBus);
        modEventBus.addListener(IC2RuntimeResources::onAddPackFinders);
        NeoForge.EVENT_BUS.addListener(InDevTooltips::onItemTooltip);
    }
}
