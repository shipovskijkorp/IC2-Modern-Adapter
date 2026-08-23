package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.client.screen.EuStorageScreen;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStoragePlatform;
import net.minecraft.client.gui.screens.MenuScreens;

/** Client-only Fabric registration for the shared IC2 electric-storage screen. */
public final class EuStorageClientRegistration {
    public static void register() {
        MenuScreens.register(EuStoragePlatform.menuType(), EuStorageScreen::new);
    }

    private EuStorageClientRegistration() {
    }
}
