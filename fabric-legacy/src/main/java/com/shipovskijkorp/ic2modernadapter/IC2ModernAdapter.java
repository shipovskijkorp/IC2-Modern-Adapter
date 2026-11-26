package com.shipovskijkorp.ic2modernadapter;

import com.shipovskijkorp.ic2modernadapter.creative.IC2CreativeTab;
import com.shipovskijkorp.ic2modernadapter.energy.FabricEuNetworkLifecycle;
import com.shipovskijkorp.ic2modernadapter.generator.GeneratorPlatform;
import com.shipovskijkorp.ic2modernadapter.furnace.FurnacePlatform;
import com.shipovskijkorp.ic2modernadapter.machine.MachinePlatform;
import com.shipovskijkorp.ic2modernadapter.toolbox.ToolBoxPlatform;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStoragePlatform;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import com.shipovskijkorp.ic2modernadapter.worldgen.OreContent;
import com.shipovskijkorp.ic2modernadapter.worldgen.OreWorldGen;
import com.shipovskijkorp.ic2modernadapter.resource.IC2RuntimeResources;
import net.fabricmc.api.ModInitializer;

/** Fabric common entrypoint. */
public final class IC2ModernAdapter implements ModInitializer {
    public static final String MOD_ID = "ic2_modern_adapter";

    @Override
    public void onInitialize() {
        IC2ContentRegistries.register();
        com.shipovskijkorp.ic2modernadapter.loot.ChestLootPlatform.register();
        OreContent.register();
        OreWorldGen.register();
        IC2RuntimeResources.register();
        com.shipovskijkorp.ic2modernadapter.content.item.tool.ToolInteractionPlatform.register();
        GeneratorPlatform.register();
        FurnacePlatform.register();
        EuStoragePlatform.register();
        MachinePlatform.register();
        ToolBoxPlatform.register();
        com.shipovskijkorp.ic2modernadapter.containment.ContainmentBoxPlatform.register();
        IC2CreativeTab.register();
        FabricEuNetworkLifecycle.register();
        com.shipovskijkorp.ic2modernadapter.network.ToolModeNetworking.register();
        com.shipovskijkorp.ic2modernadapter.network.NightVisionNetworking.register();
    }
}
