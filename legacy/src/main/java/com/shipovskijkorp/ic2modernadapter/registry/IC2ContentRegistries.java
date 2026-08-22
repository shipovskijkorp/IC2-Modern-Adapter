package com.shipovskijkorp.ic2modernadapter.registry;

import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantBlock;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantFacingBlock;
import com.shipovskijkorp.ic2modernadapter.content.block.PlaceholderDynamiteBlock;
import com.shipovskijkorp.ic2modernadapter.content.block.PlaceholderDoorBlock;
import com.shipovskijkorp.ic2modernadapter.content.item.LegacyTranslatedBlockItem;
import com.shipovskijkorp.ic2modernadapter.content.item.LegacyTranslatedDoubleHighBlockItem;
import com.shipovskijkorp.ic2modernadapter.content.item.LegacyTranslatedItem;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Forge 1.20.1 registration of the static IC2 2.8.222 content surface.
 *
 * <p>Everything is intentionally an inert identity placeholder for now. The important invariant at
 * this stage is that every original production registry identity exists under the {@code ic2}
 * namespace and can later receive real behavior without changing its ID.</p>
 */
public final class IC2ContentRegistries {
    private static final OriginalContentManifest MANIFEST = OriginalContentManifest.get();
    private static final String NAMESPACE = MANIFEST.namespace();

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, NAMESPACE);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NAMESPACE);
    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, NAMESPACE);
    private static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, NAMESPACE);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NAMESPACE);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, NAMESPACE);

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
            BLOCKS_BY_PATH.put(path, BLOCKS.register(path, () -> createPlaceholderBlock(path)));
        }

        for (String path : MANIFEST.registries().items()) {
            Supplier<? extends Item> item;
            if (BLOCK_ITEM_PATHS.contains(path) || "dynamite".equals(path)) {
                Supplier<? extends Block> block = requireBlock(path);
                item = ITEMS.register(path, () -> createBlockItem(path, block.get()));
            } else {
                item = ITEMS.register(path, () -> new LegacyTranslatedItem(
                        path, new Item.Properties(), IC2VariantStacks::variantKey));
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

    private static Block createPlaceholderBlock(String path) {
        BlockBehaviour.Properties properties = placeholderProperties(path);
        int variantCount = MANIFEST.stackVariants(path).size();
        if ("te".equals(path)) {
            return new LegacyVariantFacingBlock(properties, variantCount, IC2VariantStacks::placementVariantIndex);
        }
        if (variantCount > 1) {
            return new LegacyVariantBlock(properties, variantCount, IC2VariantStacks::placementVariantIndex);
        }
        return switch (path) {
            case "rubber_wood" -> new RotatedPillarBlock(properties);
            case "fence" -> new FenceBlock(properties);
            case "reinforced_door" -> new PlaceholderDoorBlock(properties);
            case "dynamite" -> new PlaceholderDynamiteBlock(properties);
            default -> new Block(properties);
        };
    }

    private static BlockBehaviour.Properties placeholderProperties(String path) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().strength(1.0F);
        if ("te".equals(path)
                || "leaves".equals(path)
                || "sapling".equals(path)
                || "scaffold".equals(path)
                || "fence".equals(path)
                || "sheet".equals(path)
                || "glass".equals(path)
                || "mining_pipe".equals(path)
                || "reinforced_door".equals(path)
                || "dynamite".equals(path)
                || MANIFEST.registries().fluidPaths().contains(path)) {
            properties.noOcclusion();
        }
        return properties;
    }

    private static Item createBlockItem(String path, Block block) {
        Item.Properties properties = new Item.Properties();
        if ("reinforced_door".equals(path)) {
            return new LegacyTranslatedDoubleHighBlockItem(
                    path, block, properties, IC2VariantStacks::variantKey);
        }
        return new LegacyTranslatedBlockItem(path, block, properties, IC2VariantStacks::variantKey);
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
