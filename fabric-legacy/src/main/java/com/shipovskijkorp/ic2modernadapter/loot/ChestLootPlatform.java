package com.shipovskijkorp.ic2modernadapter.loot;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;

public final class ChestLootPlatform {
    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, builder, source) -> {
            if (source.isBuiltin() && OriginalChestLoot.applies(id)) {
                builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootTableReference.lootTableReference(
                        new ResourceLocation("ic2_modern_adapter", id.getPath()))));
            }
        });
    }
}
