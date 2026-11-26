package com.shipovskijkorp.ic2modernadapter.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;

public final class ChestLootPlatform {
    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(ChestLootPlatform::onLoad);
    }

    private static void onLoad(LootTableLoadEvent event) {
        ResourceLocation id = event.getName();
        if (OriginalChestLoot.applies(id) && event.getTable().getPool("ic2ma_original_chest") == null) {
            event.getTable().addPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).name("ic2ma_original_chest")
                    .add(LootTableReference.lootTableReference(new ResourceLocation("ic2_modern_adapter", id.getPath())))
                    .build());
        }
    }
}
