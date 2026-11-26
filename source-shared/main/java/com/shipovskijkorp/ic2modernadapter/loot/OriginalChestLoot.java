package com.shipovskijkorp.ic2modernadapter.loot;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;

/** The original IC2 chest pools, with the old blacksmith mapped to the modern weaponsmith. */
public final class OriginalChestLoot {
    private static final Set<String> PATHS = Set.of(
            "chests/abandoned_mineshaft",
            "chests/desert_pyramid",
            "chests/end_city_treasure",
            "chests/igloo_chest",
            "chests/jungle_temple",
            "chests/nether_bridge",
            "chests/simple_dungeon",
            "chests/spawn_bonus_chest",
            "chests/stronghold_corridor",
            "chests/stronghold_crossing",
            "chests/stronghold_library",
            "chests/village/village_weaponsmith");

    public static boolean applies(ResourceLocation id) {
        return "minecraft".equals(id.getNamespace()) && PATHS.contains(id.getPath());
    }

    private OriginalChestLoot() {}
}
