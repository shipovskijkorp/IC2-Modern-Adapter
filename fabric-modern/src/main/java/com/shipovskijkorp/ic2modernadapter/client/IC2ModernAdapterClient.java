package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import com.shipovskijkorp.ic2modernadapter.resource.IC2RuntimeResources;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

/** Fabric 1.21.1 client glue. */
public final class IC2ModernAdapterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GeneratorClientRegistration.register();
        EuStorageClientRegistration.register();
        MachineClientRegistration.register();
        FurnaceClientRegistration.register();
        IC2PlaceholderColors.register();
        InDevTooltips.register();
        registerRenderLayers();
        IC2RuntimeResources.register();
    }

    private static void registerRenderLayers() {
        BlockRenderLayerMap.INSTANCE.putBlock(IC2ContentRegistries.block("leaves").get(), RenderType.cutoutMipped());
        for (String path : new String[] {
                "te", "sapling", "scaffold", "fence", "sheet", "glass",
                "mining_pipe", "reinforced_door", "dynamite"
        }) {
            BlockRenderLayerMap.INSTANCE.putBlock(IC2ContentRegistries.block(path).get(), RenderType.cutout());
        }
        for (String path : OriginalContentManifest.get().registries().fluidPaths()) {
            BlockRenderLayerMap.INSTANCE.putBlock(IC2ContentRegistries.block(path).get(), RenderType.translucent());
        }
    }
}
