package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.client.screen.CompressorScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.MaceratorScreen;
import com.shipovskijkorp.ic2modernadapter.machine.MachinePlatform;
import com.shipovskijkorp.ic2modernadapter.machine.MachineSpec;
import net.minecraft.client.gui.screens.MenuScreens;

/** Client-only Fabric registration for IC2 standard-machine screens. */
public final class MachineClientRegistration {
    public static void register() {
        MenuScreens.register(MachinePlatform.menuType(MachineSpec.MACERATOR), MaceratorScreen::new);
        MenuScreens.register(MachinePlatform.menuType(MachineSpec.COMPRESSOR), CompressorScreen::new);
    }

    private MachineClientRegistration() {
    }
}
