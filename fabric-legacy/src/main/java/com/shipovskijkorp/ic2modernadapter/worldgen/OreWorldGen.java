package com.shipovskijkorp.ic2modernadapter.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** IL-style ore frequency/height distribution with Fabric biome injection. */
public final class OreWorldGen {
    private static final String MOD_ID = "ic2_modern_adapter";

    public static void register() {
        for (String path : new String[] {"tin_ore", "lead_ore", "uranium_ore", "silver_ore"}) {
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                    GenerationStep.Decoration.UNDERGROUND_ORES, key(path));
        }
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(
                        Biomes.STONY_PEAKS, Biomes.JAGGED_PEAKS, Biomes.FROZEN_PEAKS,
                        Biomes.SNOWY_SLOPES, Biomes.GROVE, Biomes.MEADOW),
                GenerationStep.Decoration.UNDERGROUND_ORES, key("silver_ore_mountains"));
    }

    private static ResourceKey<PlacedFeature> key(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(MOD_ID, path));
    }

    private OreWorldGen() {}
}
