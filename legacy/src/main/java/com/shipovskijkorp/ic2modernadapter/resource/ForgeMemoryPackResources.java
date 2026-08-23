package com.shipovskijkorp.ic2modernadapter.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

/** Forge 1.20.1 view of the compiled IC2 in-memory asset pack. */
final class ForgeMemoryPackResources extends AbstractPackResources {
    private static final byte[] PACK_META = ("{\"pack\":{\"pack_format\":15,"
            + "\"description\":\"IC2 Modern Adapter runtime resources\"}}")
            .getBytes(StandardCharsets.UTF_8);

    private final CompiledIc2ResourcePack compiled;

    ForgeMemoryPackResources(String packId, CompiledIc2ResourcePack compiled) {
        super(packId, true);
        this.compiled = compiled;
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getRootResource(String... paths) {
        if (paths.length == 1 && "pack.mcmeta".equals(paths[0])) {
            return bytes(PACK_META);
        }
        return null;
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (!CompiledIc2ResourcePack.NAMESPACE.equals(location.getNamespace())) {
            return null;
        }
        return switch (type) {
            case CLIENT_RESOURCES -> compiled.clientResource(location.getPath())
                    .map(ForgeMemoryPackResources::bytes).orElse(null);
            case SERVER_DATA -> compiled.serverDataResource(location.getPath())
                    .map(ForgeMemoryPackResources::bytes).orElse(null);
        };
    }

    @Override
    public void listResources(
            PackType type,
            String namespace,
            String path,
            ResourceOutput output) {
        if (!CompiledIc2ResourcePack.NAMESPACE.equals(namespace)) {
            return;
        }
        String prefix = path.isEmpty() ? "" : path.endsWith("/") ? path : path + "/";
        java.util.Map<String, byte[]> resources = type == PackType.CLIENT_RESOURCES
                ? compiled.clientResources()
                : compiled.serverDataResources();
        resources.forEach((resourcePath, data) -> {
            if (resourcePath.startsWith(prefix) && CompiledIc2ResourcePack.isValidResourcePath(resourcePath)) {
                output.accept(new ResourceLocation(namespace, resourcePath), bytes(data));
            }
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES || type == PackType.SERVER_DATA
                ? Set.of(CompiledIc2ResourcePack.NAMESPACE)
                : Set.of();
    }

    @Override
    public void close() {
        // All compiled resources are immutable byte arrays owned by the process.
    }

    private static IoSupplier<InputStream> bytes(byte[] data) {
        return () -> new ByteArrayInputStream(data);
    }
}
