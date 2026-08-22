package com.shipovskijkorp.ic2modernadapter.resource;

import java.io.IOException;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.solid.brrp.v1.RRPEventHelper;
import pers.solid.brrp.v1.api.RuntimeResourcePack;

/** Publishes the original-IC2-backed client resources through BRRP on Fabric. */
public final class IC2RuntimeResources {
    private static final Logger LOGGER = LoggerFactory.getLogger(IC2RuntimeResources.class);
    private static volatile CompiledIc2ResourcePack compiled;
    private static RuntimeResourcePack runtimePack;

    public static synchronized void register() {
        if (runtimePack != null) {
            return;
        }

        CompiledIc2ResourcePack packData = compiled();
        RuntimeResourcePack pack = RuntimeResourcePack.create(id("original_ic2_runtime"));
        pack.setDisplayName(Component.literal("IC2 Modern Adapter - Original IC2 Resources"));
        pack.setAllowsDuplicateResource(false);
        packData.resources().forEach((path, bytes) ->
                pack.addResource(PackType.CLIENT_RESOURCES, ic2(path), bytes));

        // Same effective priority as the Forge/NeoForge built-in TOP pack: above ordinary mod
        // resources, while still allowing an enabled user resource pack to override IC2 visuals.
        RRPEventHelper.BEFORE_USER.registerSidedPack(PackType.CLIENT_RESOURCES, pack);
        runtimePack = pack;
        LOGGER.info("Published {} compiled IC2 runtime resources through Fabric/BRRP", packData.size());
    }

    private static CompiledIc2ResourcePack compiled() {
        CompiledIc2ResourcePack value = compiled;
        if (value != null) {
            return value;
        }
        synchronized (IC2RuntimeResources.class) {
            value = compiled;
            if (value == null) {
                try {
                    Path source = OriginalIc2Locator.locate(FabricLoader.getInstance().getGameDir());
                    value = IC2RuntimeResourceCompiler.compile(source);
                    compiled = value;
                    LOGGER.info("Compiled {} IC2 runtime resources in memory from {}", value.size(), source);
                } catch (IOException | RuntimeException e) {
                    throw new IllegalStateException("Unable to compile original IC2 client resources", e);
                }
            }
        }
        return value;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("ic2_modern_adapter", path);
    }

    private static ResourceLocation ic2(String path) {
        return ResourceLocation.fromNamespaceAndPath(CompiledIc2ResourcePack.NAMESPACE, path);
    }

    private IC2RuntimeResources() {
    }
}
