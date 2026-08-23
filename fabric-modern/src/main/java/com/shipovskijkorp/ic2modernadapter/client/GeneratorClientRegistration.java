package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.client.screen.GeneratorScreen;
import com.shipovskijkorp.ic2modernadapter.generator.GeneratorPlatform;
import net.minecraft.client.gui.screens.MenuScreens;

/** Client-only Fabric registration for the Generator screen. */
public final class GeneratorClientRegistration {
    public static void register() {
        MenuScreens.register(GeneratorPlatform.menuType(), GeneratorScreen::new);
    }

    private GeneratorClientRegistration() {
    }
}
