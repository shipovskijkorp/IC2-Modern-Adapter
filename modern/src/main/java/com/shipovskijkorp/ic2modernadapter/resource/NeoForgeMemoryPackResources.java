package com.shipovskijkorp.ic2modernadapter.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

/** NeoForge 1.21.1 view of the compiled IC2 in-memory asset pack. */
final class NeoForgeMemoryPackResources extends AbstractPackResources {
    private static final byte[] CLIENT_PACK_META = ("{\"pack\":{\"pack_format\":34,"
            + "\"description\":\"IC2 Modern Adapter runtime resources\"}}")
            .getBytes(StandardCharsets.UTF_8);
    private static final byte[] SERVER_PACK_META = ("{\"pack\":{\"pack_format\":48,"
            + "\"description\":\"IC2 Modern Adapter runtime recipes\"}}")
            .getBytes(StandardCharsets.UTF_8);

    private final CompiledIc2ResourcePack compiled;
    private final PackType packType;

    NeoForgeMemoryPackResources(PackLocationInfo location, CompiledIc2ResourcePack compiled, PackType packType) {
        super(location);
        this.compiled = compiled;
        this.packType = packType;
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getRootResource(String... paths) {
        if (paths.length == 1 && "pack.mcmeta".equals(paths[0])) {
            return bytes(packType == PackType.SERVER_DATA ? SERVER_PACK_META : CLIENT_PACK_META);
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
                    .map(NeoForgeMemoryPackResources::bytes).orElse(null);
            case SERVER_DATA -> compiled.serverDataResource(location.getPath())
                    .map(NeoForgeMemoryPackResources::bytes).orElse(null);
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
                output.accept(ResourceLocation.fromNamespaceAndPath(namespace, resourcePath), bytes(data));
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
