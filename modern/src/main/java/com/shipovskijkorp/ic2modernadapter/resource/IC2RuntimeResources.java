package com.shipovskijkorp.ic2modernadapter.resource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Installs the original-IC2-backed runtime resource pack on NeoForge 1.21.1. */
public final class IC2RuntimeResources {
    private static final Logger LOGGER = LoggerFactory.getLogger(IC2RuntimeResources.class);
    private static final String PACK_ID = "ic2ma_original_runtime";
    private static volatile CompiledIc2ResourcePack compiled;

    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES && event.getPackType() != PackType.SERVER_DATA) {
            return;
        }

        PackType packType = event.getPackType();
        CompiledIc2ResourcePack packData = compiled();
        event.addRepositorySource(consumer -> {
            PackLocationInfo location = new PackLocationInfo(
                    PACK_ID,
                    Component.literal("IC2 Modern Adapter - Original IC2 Resources"),
                    PackSource.BUILT_IN,
                    Optional.<KnownPack>empty());
            Pack.ResourcesSupplier resources = new Pack.ResourcesSupplier() {
                @Override
                public net.minecraft.server.packs.PackResources openPrimary(PackLocationInfo info) {
                    return new NeoForgeMemoryPackResources(info, packData, packType);
                }

                @Override
                public net.minecraft.server.packs.PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                    return new NeoForgeMemoryPackResources(info, packData, packType);
                }
            };
            Pack pack = Pack.readMetaAndCreate(
                    location,
                    resources,
                    packType,
                    new PackSelectionConfig(true, Pack.Position.TOP, false));
            if (pack == null) {
                throw new IllegalStateException("Unable to create the IC2 Modern Adapter runtime resource pack");
            }
            consumer.accept(pack);
        });
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
                    Path source = OriginalIc2Locator.locate(FMLPaths.GAMEDIR.get());
                    value = IC2RuntimeResourceCompiler.compile(source);
                    compiled = value;
                    LOGGER.info("Compiled {} IC2 client resources and {} runtime recipes/data resources in memory from {}", value.clientSize(), value.serverDataSize(), source);
                } catch (IOException | RuntimeException e) {
                    throw new IllegalStateException("Unable to compile original IC2 client resources", e);
                }
            }
        }
        return value;
    }

    private IC2RuntimeResources() {
    }
}
