package com.shipovskijkorp.ic2modernadapter.energy;

import com.shipovskijkorp.ic2modernadapter.energy.net.EuNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

/** Forge-only lifecycle bridge. All EU routing/simulation remains in source-shared. */
public final class ForgeEuNetworkLifecycle {
    private ForgeEuNetworkLifecycle() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(ForgeEuNetworkLifecycle::onLevelTick);
    }

    private static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            EuNetwork.onLevelTickEnd(event.level);
        }
    }
}
