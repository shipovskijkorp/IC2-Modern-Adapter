package com.shipovskijkorp.ic2modernadapter.registry;

import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantBlock;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyTeBlock;
import com.shipovskijkorp.ic2modernadapter.generator.GeneratorBlockEntity;
import com.shipovskijkorp.ic2modernadapter.furnace.FurnaceSpec;
import com.shipovskijkorp.ic2modernadapter.furnace.IronFurnaceBlockEntity;
import com.shipovskijkorp.ic2modernadapter.furnace.ElectricFurnaceBlockEntity;
import com.shipovskijkorp.ic2modernadapter.furnace.InductionFurnaceBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.MachineBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.MetalFormerBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.OreWashingPlantBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.ThermalCentrifugeBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.MachineSpec;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageBlockEntity;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageSpec;
import com.shipovskijkorp.ic2modernadapter.energy.item.EuElectricItemSpec;
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
import com.shipovskijkorp.ic2modernadapter.content.item.LegacyCraftingToolItem;
import com.shipovskijkorp.ic2modernadapter.content.item.armor.ModernArmorMaterials;
import com.shipovskijkorp.ic2modernadapter.content.item.armor.HazmatArmorItem;
import com.shipovskijkorp.ic2modernadapter.content.item.armor.TranslatedArmorItem;
import com.shipovskijkorp.ic2modernadapter.content.item.tool.BronzeToolMaterial;
import com.shipovskijkorp.ic2modernadapter.content.item.tool.TranslatedAxeItem;
import com.shipovskijkorp.ic2modernadapter.content.item.tool.TranslatedHoeItem;
import com.shipovskijkorp.ic2modernadapter.content.item.tool.TranslatedPickaxeItem;
import com.shipovskijkorp.ic2modernadapter.content.item.tool.TranslatedShovelItem;
import com.shipovskijkorp.ic2modernadapter.content.item.tool.TranslatedSwordItem;
import com.shipovskijkorp.ic2modernadapter.content.item.IodineTabletItem;
import com.shipovskijkorp.ic2modernadapter.content.item.EuElectricItem;
import com.shipovskijkorp.ic2modernadapter.content.item.RadioactiveItem;
import com.shipovskijkorp.ic2modernadapter.content.item.WireCutterItem;
import com.shipovskijkorp.ic2modernadapter.toolbox.ToolBoxItem;
import com.shipovskijkorp.ic2modernadapter.radiation.RadioactivitySpec;
import com.shipovskijkorp.ic2modernadapter.radiation.RadiationEffect;
import com.shipovskijkorp.ic2modernadapter.recipe.LegacyCraftingRecipe;
import com.shipovskijkorp.ic2modernadapter.recipe.LegacySmeltingRecipe;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.WaterFluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NeoForge 1.21.1 registration of the static IC2 2.8.222 content surface.
 *
 * <p>The original registry surface remains stable under the {@code ic2} namespace. Most entries
 * are still identity placeholders, while completed subtypes (Generator and electric storage blocks) attach
 * their real behavior without changing the original registry IDs.</p>
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
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, "ic2_modern_adapter");
    private static final Supplier<RecipeSerializer<?>> LEGACY_CRAFTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("legacy_crafting", LegacyCraftingRecipe.Serializer::new);
    private static final Supplier<RecipeSerializer<?>> LEGACY_SMELTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("legacy_smelting", LegacySmeltingRecipe.Serializer::new);

    /** Internal modern carrier block for the original standalone ic2:cable item. */
    private static final Supplier<CableBlock> CABLE_BLOCK = BLOCKS.register("cable", () -> new CableCarrierBlock(
            BlockBehaviour.Properties.of().strength(0.2F).sound(SoundType.WOOL).noOcclusion(),
            IC2VariantStacks::variantKey,
            IC2VariantStacks::create,
            CableBlockEntity::new));

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
            if ("cable".equals(path)) {
                item = ITEMS.register(path, () -> new CableItem(
                        new Item.Properties(), CABLE_BLOCK.get(), IC2VariantStacks::variantKey));
            } else if (BLOCK_ITEM_PATHS.contains(path) || "dynamite".equals(path)) {
                Supplier<? extends Block> block = requireBlock(path);
                item = ITEMS.register(path, () -> createBlockItem(path, block.get()));
            } else {
                item = ITEMS.register(path, () -> createStandaloneItem(path));
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
            EFFECTS_BY_PATH.put(path, MOB_EFFECTS.register(path, RadiationEffect::new));
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
            BLOCK_ENTITIES_BY_PATH.put(path, BLOCK_ENTITY_TYPES.register(path, () -> {
                if ("generator".equals(path)) {
                    return BlockEntityType.Builder.of(GeneratorBlockEntity::new, teBlock.get()).build(null);
                }
                EuStorageSpec storage = EuStorageSpec.fromBlockEntityPath(path);
                if (storage != null) {
                    return BlockEntityType.Builder.of(
                            (pos, state) -> new EuStorageBlockEntity(storage, pos, state),
                            teBlock.get()).build(null);
                }
                MachineSpec machine = MachineSpec.fromBlockEntityPath(path);
                if (machine != null) {
                    return BlockEntityType.Builder.of(
                            (pos, state) -> createMachineBlockEntity(machine, pos, state),
                            teBlock.get()).build(null);
                }
                FurnaceSpec furnace = FurnaceSpec.fromBlockEntityPath(path);
                if (furnace != null) {
                    return BlockEntityType.Builder.of(
                            (pos, state) -> createFurnaceBlockEntity(furnace, pos, state),
                            teBlock.get()).build(null);
                }
                if ("cable".equals(path) || "detector_cable".equals(path) || "splitter_cable".equals(path)) {
                    return BlockEntityType.Builder.of(
                            (pos, state) -> new CableBlockEntity(
                                    java.util.Objects.requireNonNull(EuCableVariant.fromBlockState(state)), pos, state),
                            CABLE_BLOCK.get()).build(null);
                }
                return BlockEntityType.Builder.<BlockEntity>of((pos, state) -> null, teBlock.get()).build(null);
            }));
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
        RECIPE_SERIALIZERS.register(modEventBus);
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
                    IC2ContentRegistries::createMachineBlockEntity,
                    IC2ContentRegistries::createFurnaceBlockEntity,
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

    private static BlockEntity createMachineBlockEntity(MachineSpec spec, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        return switch (spec.kind()) {
            case STANDARD -> new MachineBlockEntity(spec, pos, state);
            case METAL_FORMER -> new MetalFormerBlockEntity(pos, state);
            case ORE_WASHING -> new OreWashingPlantBlockEntity(pos, state);
            case THERMAL_CENTRIFUGE -> new ThermalCentrifugeBlockEntity(pos, state);
        };
    }

    private static BlockEntity createFurnaceBlockEntity(FurnaceSpec spec, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        return switch (spec) {
            case IRON -> new IronFurnaceBlockEntity(pos, state);
            case ELECTRIC -> new ElectricFurnaceBlockEntity(pos, state);
            case INDUCTION -> new InductionFurnaceBlockEntity(pos, state);
        };
    }

    private static Item createStandaloneItem(String path) {
        return switch (path) {
            case "bronze_sword" -> new TranslatedSwordItem(
                    path, BronzeToolMaterial.INSTANCE, 5, -2.4F, new Item.Properties(), IC2VariantStacks::variantKey);
            case "bronze_pickaxe" -> new TranslatedPickaxeItem(
                    path, BronzeToolMaterial.INSTANCE, 1, -2.8F, new Item.Properties(), IC2VariantStacks::variantKey);
            case "bronze_shovel" -> new TranslatedShovelItem(
                    path, BronzeToolMaterial.INSTANCE, 1.5F, -3.0F, new Item.Properties(), IC2VariantStacks::variantKey);
            case "bronze_axe" -> new TranslatedAxeItem(
                    path, BronzeToolMaterial.INSTANCE, 8.0F, -3.1F, new Item.Properties(), IC2VariantStacks::variantKey);
            case "bronze_hoe" -> new TranslatedHoeItem(
                    path, BronzeToolMaterial.INSTANCE, 0, -3.0F, new Item.Properties(), IC2VariantStacks::variantKey);
            case "bronze_helmet" -> new TranslatedArmorItem(
                    path, ModernArmorMaterials.BRONZE.holder(), ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ModernArmorMaterials.BRONZE.durability(ArmorItem.Type.HELMET)),
                    IC2VariantStacks::variantKey);
            case "bronze_chestplate" -> new TranslatedArmorItem(
                    path, ModernArmorMaterials.BRONZE.holder(), ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ModernArmorMaterials.BRONZE.durability(ArmorItem.Type.CHESTPLATE)),
                    IC2VariantStacks::variantKey);
            case "bronze_leggings" -> new TranslatedArmorItem(
                    path, ModernArmorMaterials.BRONZE.holder(), ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ModernArmorMaterials.BRONZE.durability(ArmorItem.Type.LEGGINGS)),
                    IC2VariantStacks::variantKey);
            case "bronze_boots" -> new TranslatedArmorItem(
                    path, ModernArmorMaterials.BRONZE.holder(), ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ModernArmorMaterials.BRONZE.durability(ArmorItem.Type.BOOTS)),
                    IC2VariantStacks::variantKey);
            case "alloy_chestplate" -> new TranslatedArmorItem(
                    path, ModernArmorMaterials.ALLOY.holder(), ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ModernArmorMaterials.ALLOY.durability(ArmorItem.Type.CHESTPLATE)),
                    IC2VariantStacks::variantKey);
            case "hazmat_helmet" -> new HazmatArmorItem(
                    path, ModernArmorMaterials.HAZMAT.holder(), ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ModernArmorMaterials.HAZMAT.durability(ArmorItem.Type.HELMET)),
                    IC2VariantStacks::variantKey);
            case "hazmat_chestplate" -> new HazmatArmorItem(
                    path, ModernArmorMaterials.HAZMAT.holder(), ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ModernArmorMaterials.HAZMAT.durability(ArmorItem.Type.CHESTPLATE)),
                    IC2VariantStacks::variantKey);
            case "hazmat_leggings" -> new HazmatArmorItem(
                    path, ModernArmorMaterials.HAZMAT.holder(), ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ModernArmorMaterials.HAZMAT.durability(ArmorItem.Type.LEGGINGS)),
                    IC2VariantStacks::variantKey);
            case "rubber_boots" -> new HazmatArmorItem(
                    path, ModernArmorMaterials.RUBBER_BOOTS.holder(), ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ModernArmorMaterials.RUBBER_BOOTS.durability(ArmorItem.Type.BOOTS)),
                    IC2VariantStacks::variantKey);
            case "re_battery", "advanced_re_battery", "energy_crystal", "lapotron_crystal" -> new EuElectricItem(
                    path, EuElectricItemSpec.fromItemPath(path), new Item.Properties().stacksTo(16), IC2VariantStacks::variantKey);
            case "iodine_tablet" -> new IodineTabletItem(
                    path, new Item.Properties(), IC2VariantStacks::variantKey);
            case "nuclear" -> new RadioactiveItem(
                    path, new Item.Properties(), IC2VariantStacks::variantKey);
            case "cutter" -> new WireCutterItem(
                    path, new Item.Properties().durability(WireCutterItem.MAX_USES), IC2VariantStacks::variantKey);
            case "tool_box" -> new ToolBoxItem(
                    path, new Item.Properties().stacksTo(1), IC2VariantStacks::variantKey);
            case "forge_hammer" -> new LegacyCraftingToolItem(
                    path, new Item.Properties().durability(80), IC2VariantStacks::variantKey);
            case "cf_pack", "jetpack" -> new LegacyTranslatedItem(
                    path, new Item.Properties().durability(27), IC2VariantStacks::variantKey);
            case "rsh_condensator" -> new LegacyTranslatedItem(
                    path, new Item.Properties().durability(20_000), IC2VariantStacks::variantKey);
            case "lzh_condensator" -> new LegacyTranslatedItem(
                    path, new Item.Properties().durability(100_000), IC2VariantStacks::variantKey);
            default -> RadioactivitySpec.radioactiveFuelRods().contains(path)
                    ? new RadioactiveItem(path, new Item.Properties(), IC2VariantStacks::variantKey)
                    : new LegacyTranslatedItem(path, new Item.Properties(), IC2VariantStacks::variantKey);
        };
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

    private IC2ContentRegistries() {
    }
}
