package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.client.screen.ToolBoxScreen;
import com.shipovskijkorp.ic2modernadapter.toolbox.ToolBoxPlatform;
import com.shipovskijkorp.ic2modernadapter.containment.ContainmentBoxPlatform;
import com.shipovskijkorp.ic2modernadapter.client.screen.ContainmentBoxScreen;
import net.minecraft.client.gui.screens.MenuScreens;

/** Client-only Fabric registration for the IC2 Tool Box screen. */
public final class ToolBoxClientRegistration {
    public static void register() {
        MenuScreens.register(ToolBoxPlatform.menuType(), ToolBoxScreen::new);
        MenuScreens.register(ContainmentBoxPlatform.menuType(), ContainmentBoxScreen::new);
    }

    private ToolBoxClientRegistration() {
    }
}
