package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.client.screen.CompressorScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.MaceratorScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.MetalFormerScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.OreWashingPlantScreen;
import com.shipovskijkorp.ic2modernadapter.machine.MachinePlatform;
import com.shipovskijkorp.ic2modernadapter.machine.MachineSpec;
import net.minecraft.client.gui.screens.MenuScreens;

/** Client-only Fabric registration for IC2 machine screens. */
public final class MachineClientRegistration {
    public static void register() {
        MenuScreens.register(MachinePlatform.menuType(MachineSpec.MACERATOR), MaceratorScreen::new);
        MenuScreens.register(MachinePlatform.menuType(MachineSpec.COMPRESSOR), CompressorScreen::new);
        MenuScreens.register(MachinePlatform.menuType(MachineSpec.METAL_FORMER), MetalFormerScreen::new);
        MenuScreens.register(MachinePlatform.menuType(MachineSpec.ORE_WASHING_PLANT), OreWashingPlantScreen::new);
    }

    private MachineClientRegistration() {
    }
}
