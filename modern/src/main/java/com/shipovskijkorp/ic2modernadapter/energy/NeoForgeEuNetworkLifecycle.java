package com.shipovskijkorp.ic2modernadapter.energy;

import com.shipovskijkorp.ic2modernadapter.energy.net.EuNetwork;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** NeoForge-only lifecycle bridge. All EU routing/simulation remains in source-shared. */
public final class NeoForgeEuNetworkLifecycle {
    private NeoForgeEuNetworkLifecycle() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(NeoForgeEuNetworkLifecycle::onLevelTick);
    }

    private static void onLevelTick(LevelTickEvent.Post event) {
        if (!event.getLevel().isClientSide()) {
            EuNetwork.onLevelTickEnd(event.getLevel());
        }
    }
}
