package com.shipovskijkorp.ic2modernadapter.worldgen;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Loader registration; biome injection is in the bundled biome modifier. */
public final class RubberTreeWorldGen {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, "ic2_modern_adapter");
    static { FEATURES.register("rubber_tree_cluster", RubberTreeFeature::new); }
    public static void register(IEventBus bus) { FEATURES.register(bus); }
    private RubberTreeWorldGen() {}
}
