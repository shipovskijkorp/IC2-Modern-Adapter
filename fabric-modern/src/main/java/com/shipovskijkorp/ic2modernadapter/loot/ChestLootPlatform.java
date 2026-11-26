package com.shipovskijkorp.ic2modernadapter.loot;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;

public final class ChestLootPlatform {
    public static void register() {
        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if (source.isBuiltin() && OriginalChestLoot.applies(key.location())) {
                builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(NestedLootTable.lootTableReference(
                        ResourceKey.create(Registries.LOOT_TABLE,
                                ResourceLocation.fromNamespaceAndPath("ic2_modern_adapter", key.location().getPath())))));
            }
        });
    }
}
