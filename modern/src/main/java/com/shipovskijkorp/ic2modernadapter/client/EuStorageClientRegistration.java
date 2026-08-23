package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.client.screen.EuStorageScreen;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStoragePlatform;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only NeoForge registration for the shared IC2 electric-storage screen. */
@EventBusSubscriber(modid = IC2ModernAdapter.MOD_ID, value = Dist.CLIENT)
public final class EuStorageClientRegistration {
    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(EuStoragePlatform.menuType(), EuStorageScreen::new);
    }

    private EuStorageClientRegistration() {
    }
}
