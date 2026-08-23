package com.shipovskijkorp.ic2modernadapter.resource;

import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Installs the original-IC2-backed runtime resource pack on Forge 1.20.1. */
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
            Pack pack = Pack.readMetaAndCreate(
                    PACK_ID,
                    Component.literal("IC2 Modern Adapter - Original IC2 Resources"),
                    true,
                    id -> new ForgeMemoryPackResources(id, packData),
                    packType,
                    Pack.Position.TOP,
                    PackSource.BUILT_IN);
            if (pack == null) {
                throw new IllegalStateException("Unable to create the IC2 Modern Adapter runtime resource pack");
            }
            consumer.accept(pack);
        });
    }

    /** Ensures runtime IC2 data tables are available before client integrations query them. */
    public static void ensureCompiled() {
        compiled();
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
