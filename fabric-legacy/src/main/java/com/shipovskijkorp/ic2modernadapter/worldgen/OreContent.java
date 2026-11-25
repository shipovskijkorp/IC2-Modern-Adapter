package com.shipovskijkorp.ic2modernadapter.worldgen;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Adapter-owned deepslate ores and the old stone-silver ID kept for save compatibility. */
public final class OreContent {
    private static final String MOD_ID = "ic2_modern_adapter";
    private static final List<Item> CREATIVE_ITEMS = new ArrayList<>();
    private static boolean registered;

    public static synchronized void register() {
        if (registered) return;
        registerOre("silver_ore", false);
        registerOre("deepslate_silver_ore", true);
        registerOre("deepslate_tin_ore", true);
        registerOre("deepslate_lead_ore", true);
        registerOre("deepslate_uranium_ore", true);
        registered = true;
    }

    private static void registerOre(String path, boolean deepslate) {
        ResourceLocation id = new ResourceLocation(MOD_ID, path);
        Block block = Registry.register(BuiltInRegistries.BLOCK, id,
                new Block(BlockBehaviour.Properties.copy(deepslate ? Blocks.DEEPSLATE_IRON_ORE : Blocks.IRON_ORE)
                        .requiresCorrectToolForDrops()));
        Item item = Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties()));
        CREATIVE_ITEMS.add(item);
    }

    public static List<ItemStack> creativeItems() {
        List<ItemStack> result = new ArrayList<>();
        result.add(IC2VariantStacks.create(SilverOreVariant.KEY));
        for (Item item : CREATIVE_ITEMS) {
            if (!"silver_ore".equals(BuiltInRegistries.ITEM.getKey(item).getPath())) {
                result.add(new ItemStack(item));
            }
        }
        return List.copyOf(result);
    }

    private OreContent() {}
}
