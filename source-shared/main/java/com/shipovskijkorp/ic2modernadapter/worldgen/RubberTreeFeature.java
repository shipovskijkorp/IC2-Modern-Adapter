package com.shipovskijkorp.ic2modernadapter.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** One chunk-level cluster attempt, following the original IC2 biome-weighted decorator. */
public final class RubberTreeFeature extends Feature<NoneFeatureConfiguration> {
    private static final TagKey<Biome> SWAMP = tag("rubber_tree_swamp");
    private static final TagKey<Biome> FOREST = tag("rubber_tree_forest");
    private static final TagKey<Biome> JUNGLE = tag("rubber_tree_jungle");

    public RubberTreeFeature() { super(NoneFeatureConfiguration.CODEC); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int chunkX = context.origin().getX() & ~15;
        int chunkZ = context.origin().getZ() & ~15;
        int count = 0;
        for (int i = 0; i < 4; i++) {
            int sampleX = chunkX + 8 + (i & 1) * 15;
            int sampleZ = chunkZ + 8 + ((i & 2) >>> 1) * 15;
            int sampleY = Math.min(level.getMaxBuildHeight() - 1,
                    level.getHeight(Heightmap.Types.WORLD_SURFACE, sampleX, sampleZ));
            BlockPos sample = new BlockPos(sampleX, sampleY, sampleZ);
            Holder<Biome> biome = level.getBiome(sample);
            count += RubberTreeRules.biomeContribution(biome.is(SWAMP), biome.is(FOREST) || biome.is(JUNGLE), random::nextInt);
        }
        count /= 2;
        if (count == 0 || random.nextInt(100) >= count) return false;
        boolean any = false;
        for (int attempt = 0; attempt < count; attempt++) {
            int x = chunkX + 8 + random.nextInt(16);
            int z = chunkZ + 8 + random.nextInt(16);
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (RubberTreeGrowth.grow(level, pos, random, true)) any = true;
            else count -= 3;
        }
        return any;
    }

    private static TagKey<Biome> tag(String path) {
        return TagKey.create(Registries.BIOME, ResourceLocation.tryParse("ic2_modern_adapter:" + path));
    }
}
