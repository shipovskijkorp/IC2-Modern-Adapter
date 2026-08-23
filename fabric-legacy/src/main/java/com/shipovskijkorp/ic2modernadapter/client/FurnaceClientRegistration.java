package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.client.screen.ElectricFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.InductionFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.IronFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.furnace.FurnacePlatform;
import net.minecraft.client.gui.screens.MenuScreens;

/** Client-only Fabric registration for IC2 furnace screens. */
public final class FurnaceClientRegistration {
    public static void register() {
        MenuScreens.register(FurnacePlatform.ironMenuType(), IronFurnaceScreen::new);
        MenuScreens.register(FurnacePlatform.electricMenuType(), ElectricFurnaceScreen::new);
        MenuScreens.register(FurnacePlatform.inductionMenuType(), InductionFurnaceScreen::new);
    }

    private FurnaceClientRegistration() {
    }
}
