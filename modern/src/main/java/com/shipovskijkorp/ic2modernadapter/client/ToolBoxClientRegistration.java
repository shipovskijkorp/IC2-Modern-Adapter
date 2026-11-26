package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.client.screen.ToolBoxScreen;
import com.shipovskijkorp.ic2modernadapter.toolbox.ToolBoxPlatform;
import com.shipovskijkorp.ic2modernadapter.containment.ContainmentBoxPlatform;
import com.shipovskijkorp.ic2modernadapter.client.screen.ContainmentBoxScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only NeoForge registration for the IC2 Tool Box screen. */
@EventBusSubscriber(modid = IC2ModernAdapter.MOD_ID, value = Dist.CLIENT)
public final class ToolBoxClientRegistration {
    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ToolBoxPlatform.menuType(), ToolBoxScreen::new);
        event.register(ContainmentBoxPlatform.menuType(), ContainmentBoxScreen::new);
    }

    private ToolBoxClientRegistration() {
    }
}
