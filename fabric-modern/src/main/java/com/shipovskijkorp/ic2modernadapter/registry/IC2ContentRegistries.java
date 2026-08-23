package com.shipovskijkorp.ic2modernadapter.registry;

import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantBlock;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyTeBlock;
import com.shipovskijkorp.ic2modernadapter.generator.GeneratorBlockEntity;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageBlockEntity;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageSpec;
import com.shipovskijkorp.ic2modernadapter.energy.cable.CableBlock;
import com.shipovskijkorp.ic2modernadapter.energy.cable.CableBlockEntity;
import com.shipovskijkorp.ic2modernadapter.energy.cable.CableCarrierBlock;
import com.shipovskijkorp.ic2modernadapter.energy.cable.CableItem;
import com.shipovskijkorp.ic2modernadapter.energy.cable.EuCableVariant;
import com.shipovskijkorp.ic2modernadapter.content.block.PlaceholderDynamiteBlock;
import com.shipovskijkorp.ic2modernadapter.content.block.PlaceholderDoorBlock;
import com.shipovskijkorp.ic2modernadapter.content.item.LegacyTranslatedBlockItem;
import com.shipovskijkorp.ic2modernadapter.content.item.LegacyTranslatedDoubleHighBlockItem;
import com.shipovskijkorp.ic2modernadapter.content.item.LegacyTranslatedItem;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.WaterFluid;

/** Fabric registration of the static IC2 2.8.222 content surface. */
public final class IC2ContentRegistries {
    private static final OriginalContentManifest MANIFEST = OriginalContentManifest.get();
    private static final String NAMESPACE = MANIFEST.namespace();

    private static final Map<String, Supplier<? extends Block>> BLOCKS_BY_PATH = new LinkedHashMap<>();
    private static final Map<String, Supplier<? extends Item>> ITEMS_BY_PATH = new LinkedHashMap<>();
    private static final Map<String, Supplier<? extends Fluid>> FLUIDS_BY_PATH = new LinkedHashMap<>();
    private static final Map<String, Supplier<? extends MobEffect>> EFFECTS_BY_PATH = new LinkedHashMap<>();
    private static final Map<String, Supplier<? extends EntityType<?>>> ENTITIES_BY_PATH = new LinkedHashMap<>();
    private static final Map<String, Supplier<? extends BlockEntityType<?>>> BLOCK_ENTITIES_BY_PATH =
            new LinkedHashMap<>();
    private static final Set<String> BLOCK_ITEM_PATHS = Set.copyOf(MANIFEST.registries().blockItems());
    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }

        for (String path : MANIFEST.registries().blocks()) {
            Block block = Registry.register(BuiltInRegistries.BLOCK, id(path), createPlaceholderBlock(path));
            BLOCKS_BY_PATH.put(path, () -> block);
        }

        CableBlock cableBlock = Registry.register(
                BuiltInRegistries.BLOCK,
                id("cable"),
                new CableCarrierBlock(
                        BlockBehaviour.Properties.of().strength(0.2F).sound(SoundType.WOOL).noOcclusion(),
                        IC2VariantStacks::variantKey,
                        IC2VariantStacks::create,
                        CableBlockEntity::new));

        for (String path : MANIFEST.registries().items()) {
            Item item;
            if ("cable".equals(path)) {
                item = new CableItem(new Item.Properties(), cableBlock, IC2VariantStacks::variantKey);
            } else if (BLOCK_ITEM_PATHS.contains(path) || "dynamite".equals(path)) {
                item = createBlockItem(path, requireBlock(path).get());
            } else {
                item = new LegacyTranslatedItem(path, new Item.Properties(), IC2VariantStacks::variantKey);
            }
            Item registeredItem = Registry.register(BuiltInRegistries.ITEM, id(path), item);
            ITEMS_BY_PATH.put(path, () -> registeredItem);
        }

        // One identity-only source fluid per original FluidName. Real fluid behavior comes later.
        for (OriginalContentManifest.FluidIdentity fluid : MANIFEST.registries().fluids()) {
            Fluid value = Registry.register(BuiltInRegistries.FLUID, id(fluid.path()), new WaterFluid.Source());
            FLUIDS_BY_PATH.put(fluid.path(), () -> value);
        }

        for (String path : MANIFEST.registries().mobEffects()) {
            MobEffect value = Registry.register(BuiltInRegistries.MOB_EFFECT, id(path), new PlaceholderRadiationEffect());
            EFFECTS_BY_PATH.put(path, () -> value);
        }

        for (String path : MANIFEST.registries().entities()) {
            EntityType<?> value = Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    id(path),
                    EntityType.Builder.<Entity>of((type, level) -> null, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .build(NAMESPACE + ":" + path));
            ENTITIES_BY_PATH.put(path, () -> value);
        }

        Block teBlock = requireBlock("te").get();
        for (String path : MANIFEST.registries().blockEntities()) {
            EuStorageSpec storage = EuStorageSpec.fromBlockEntityPath(path);
            BlockEntityType<?> type;
            if ("generator".equals(path)) {
                type = BlockEntityType.Builder.of(GeneratorBlockEntity::new, teBlock).build(null);
            } else if (storage != null) {
                type = BlockEntityType.Builder.of(
                        (pos, state) -> new EuStorageBlockEntity(storage, pos, state), teBlock).build(null);
            } else if ("cable".equals(path) || "detector_cable".equals(path) || "splitter_cable".equals(path)) {
                type = BlockEntityType.Builder.of(
                        (pos, state) -> new CableBlockEntity(
                                java.util.Objects.requireNonNull(EuCableVariant.fromBlockState(state)), pos, state),
                        cableBlock).build(null);
            } else {
                type = BlockEntityType.Builder.<BlockEntity>of((pos, state) -> null, teBlock).build(null);
            }
            BlockEntityType<?> value = Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    id(path),
                    type);
            BLOCK_ENTITIES_BY_PATH.put(path, () -> value);
        }

        validateCoverage();
        registered = true;
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
            return new LegacyTeBlock(
                    properties,
                    variantCount,
                    IC2VariantStacks::placementVariantIndex,
                    GeneratorBlockEntity::new,
                    EuStorageBlockEntity::new,
                    IC2VariantStacks::create);
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

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    private static final class PlaceholderRadiationEffect extends MobEffect {
        private PlaceholderRadiationEffect() {
            super(MobEffectCategory.HARMFUL, 5_149_489);
        }
    }

    private IC2ContentRegistries() {
    }
}
