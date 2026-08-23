package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.client.screen.GeneratorScreen;
import com.shipovskijkorp.ic2modernadapter.generator.GeneratorPlatform;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only NeoForge registration for the Generator screen. */
@EventBusSubscriber(modid = IC2ModernAdapter.MOD_ID, value = Dist.CLIENT)
public final class GeneratorClientRegistration {
    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(GeneratorPlatform.menuType(), GeneratorScreen::new);
    }

    private GeneratorClientRegistration() {
    }
}
