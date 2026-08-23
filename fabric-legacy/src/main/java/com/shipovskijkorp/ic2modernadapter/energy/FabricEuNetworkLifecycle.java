package com.shipovskijkorp.ic2modernadapter.energy;

import com.shipovskijkorp.ic2modernadapter.energy.net.EuNetwork;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/** Fabric-only lifecycle bridge. All EU routing/simulation remains in source-shared. */
public final class FabricEuNetworkLifecycle {
    private FabricEuNetworkLifecycle() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(EuNetwork::onLevelTickEnd);
    }
}
