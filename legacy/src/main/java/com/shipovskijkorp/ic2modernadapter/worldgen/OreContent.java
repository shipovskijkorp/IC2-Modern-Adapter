package com.shipovskijkorp.ic2modernadapter.worldgen;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/** Adapter-owned deepslate ores and the old stone-silver ID kept for save compatibility. */
public final class OreContent {
    private static final String MOD_ID = "ic2_modern_adapter";
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final Map<String, Supplier<? extends Item>> ITEMS_BY_PATH = new LinkedHashMap<>();

    static {
        registerOre("silver_ore", false);
        registerOre("deepslate_silver_ore", true);
        registerOre("deepslate_tin_ore", true);
        registerOre("deepslate_lead_ore", true);
        registerOre("deepslate_uranium_ore", true);
    }

    private static void registerOre(String path, boolean deepslate) {
        Supplier<? extends Block> block = BLOCKS.register(path,
                () -> new Block(BlockBehaviour.Properties.copy(
                        deepslate ? Blocks.DEEPSLATE_IRON_ORE : Blocks.IRON_ORE)
                        .requiresCorrectToolForDrops()));
        ITEMS_BY_PATH.put(path, ITEMS.register(path, () -> new BlockItem(block.get(), new Item.Properties())));
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    public static List<ItemStack> creativeItems() {
        List<ItemStack> result = new ArrayList<>();
        result.add(IC2VariantStacks.create(SilverOreVariant.KEY));
        ITEMS_BY_PATH.forEach((path, supplier) -> {
            if (!"silver_ore".equals(path)) result.add(new ItemStack(supplier.get()));
        });
        return List.copyOf(result);
    }

    private OreContent() {}
}
