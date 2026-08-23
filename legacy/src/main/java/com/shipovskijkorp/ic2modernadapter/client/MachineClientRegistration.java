package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.client.screen.CompressorScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.MaceratorScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.MetalFormerScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.OreWashingPlantScreen;
import com.shipovskijkorp.ic2modernadapter.machine.MachinePlatform;
import com.shipovskijkorp.ic2modernadapter.machine.MachineSpec;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only Forge registration for IC2 machine screens. */
@Mod.EventBusSubscriber(
        modid = IC2ModernAdapter.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class MachineClientRegistration {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(MachinePlatform.menuType(MachineSpec.MACERATOR), MaceratorScreen::new);
            MenuScreens.register(MachinePlatform.menuType(MachineSpec.COMPRESSOR), CompressorScreen::new);
            MenuScreens.register(MachinePlatform.menuType(MachineSpec.METAL_FORMER), MetalFormerScreen::new);
            MenuScreens.register(MachinePlatform.menuType(MachineSpec.ORE_WASHING_PLANT), OreWashingPlantScreen::new);
        });
    }

    private MachineClientRegistration() {
    }
}
