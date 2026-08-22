package com.shipovskijkorp.ic2modernadapter.registry;

import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.WaterFluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NeoForge 1.21.1 registration of the static IC2 2.8.222 content surface.
 *
 * <p>Everything is intentionally an inert identity placeholder for now. The important invariant at
 * this stage is that every original production registry identity exists under the {@code ic2}
 * namespace and can later receive real behavior without changing its ID.</p>
 */
public final class IC2ContentRegistries {
    private static final OriginalContentManifest MANIFEST = OriginalContentManifest.get();
    private static final String NAMESPACE = MANIFEST.namespace();

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NAMESPACE);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NAMESPACE);
    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, NAMESPACE);
    private static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, NAMESPACE);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NAMESPACE);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NAMESPACE);

    private static final Map<String, Supplier<? extends Block>> BLOCKS_BY_PATH = new LinkedHashMap<>();
    private static final Map<String, Supplier<? extends Item>> ITEMS_BY_PATH = new LinkedHashMap<>();
    private static final Map<String, Supplier<? extends Fluid>> FLUIDS_BY_PATH = new LinkedHashMap<>();
    private static final Map<String, Supplier<? extends MobEffect>> EFFECTS_BY_PATH = new LinkedHashMap<>();
    private static final Map<String, Supplier<? extends EntityType<?>>> ENTITIES_BY_PATH = new LinkedHashMap<>();
    private static final Map<String, Supplier<? extends BlockEntityType<?>>> BLOCK_ENTITIES_BY_PATH =
            new LinkedHashMap<>();
    private static final Set<String> BLOCK_ITEM_PATHS = Set.copyOf(MANIFEST.registries().blockItems());

    static {
        for (String path : MANIFEST.registries().blocks()) {
            BLOCKS_BY_PATH.put(path, BLOCKS.registerSimpleBlock(path));
        }

        for (String path : MANIFEST.registries().items()) {
            Supplier<? extends Item> item;
            if (BLOCK_ITEM_PATHS.contains(path)) {
                item = ITEMS.registerSimpleBlockItem(path, requireBlock(path));
            } else {
                item = ITEMS.registerSimpleItem(path);
            }
            ITEMS_BY_PATH.put(path, item);
        }

        // Identity-only placeholders until the real IC2 fluid implementation lands. We keep one
        // modern Fluid registry identity per original FluidName and deliberately do not invent
        // flowing_* IDs that did not exist in the reference build.
        for (OriginalContentManifest.FluidIdentity fluid : MANIFEST.registries().fluids()) {
            FLUIDS_BY_PATH.put(fluid.path(), FLUIDS.register(fluid.path(), WaterFluid.Source::new));
        }

        for (String path : MANIFEST.registries().mobEffects()) {
            EFFECTS_BY_PATH.put(path, MOB_EFFECTS.register(path, PlaceholderRadiationEffect::new));
        }

        // NeoForge 1.21.1 does not yet have the later DeferredRegister.Entities helper, so use the
        // vanilla entity registry directly. EntityType.Builder#build still takes the string ID on
        // this target.
        for (String path : MANIFEST.registries().entities()) {
            ENTITIES_BY_PATH.put(path, ENTITY_TYPES.register(path, () -> EntityType.Builder
                    .<Entity>of((type, level) -> null, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build(NAMESPACE + ":" + path)));
        }

        Supplier<? extends Block> teBlock = requireBlock("te");
        for (String path : MANIFEST.registries().blockEntities()) {
            BLOCK_ENTITIES_BY_PATH.put(path, BLOCK_ENTITY_TYPES.register(path, () -> BlockEntityType.Builder
                    .<BlockEntity>of((pos, state) -> null, teBlock.get())
                    .build(null)));
        }

        validateCoverage();
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        FLUIDS.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }

    public static Supplier<? extends Block> block(String path) {
        return require(BLOCKS_BY_PATH, "block", path);
    }

    public static Supplier<? extends Item> item(String path) {
        return require(ITEMS_BY_PATH, "item", path);
    }

    public static Supplier<? extends Fluid> fluid(String path) {
        return require(FLUIDS_BY_PATH, "fluid", path);
    }

    public static Supplier<? extends MobEffect> mobEffect(String path) {
        return require(EFFECTS_BY_PATH, "mob effect", path);
    }

    public static Supplier<? extends EntityType<?>> entityType(String path) {
        return require(ENTITIES_BY_PATH, "entity type", path);
    }

    public static Supplier<? extends BlockEntityType<?>> blockEntityType(String path) {
        return require(BLOCK_ENTITIES_BY_PATH, "block entity type", path);
    }

    private static Supplier<? extends Block> requireBlock(String path) {
        return require(BLOCKS_BY_PATH, "block", path);
    }

    private static <T> T require(Map<String, T> entries, String kind, String path) {
        T value = entries.get(path);
        if (value == null) {
            throw new IllegalArgumentException("Unknown IC2 " + kind + " registry path: " + path);
        }
        return value;
    }

    private static void validateCoverage() {
        requireSameKeys("blocks", MANIFEST.registries().blocks(), BLOCKS_BY_PATH.keySet());
        requireSameKeys("items", MANIFEST.registries().items(), ITEMS_BY_PATH.keySet());
        requireSameKeys("fluids", MANIFEST.registries().fluidPaths(), FLUIDS_BY_PATH.keySet());
        requireSameKeys("mob effects", MANIFEST.registries().mobEffects(), EFFECTS_BY_PATH.keySet());
        requireSameKeys("entities", MANIFEST.registries().entities(), ENTITIES_BY_PATH.keySet());
        requireSameKeys("block entities", MANIFEST.registries().blockEntities(), BLOCK_ENTITIES_BY_PATH.keySet());
    }

    private static void requireSameKeys(String kind, Iterable<String> expected, Set<String> actual) {
        Set<String> expectedSet = new java.util.LinkedHashSet<>();
        expected.forEach(expectedSet::add);
        if (!expectedSet.equals(actual)) {
            throw new IllegalStateException("Incomplete IC2 " + kind + " registration: expected "
                    + expectedSet + ", got " + actual);
        }
    }

    private static final class PlaceholderRadiationEffect extends MobEffect {
        private PlaceholderRadiationEffect() {
            super(MobEffectCategory.HARMFUL, 5_149_489);
        }
    }

    private IC2ContentRegistries() {
    }
}
