package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.client.screen.CompressorScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.ExtractorScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.MaceratorScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.MetalFormerScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.OreWashingPlantScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.ThermalCentrifugeScreen;
import com.shipovskijkorp.ic2modernadapter.machine.MachinePlatform;
import com.shipovskijkorp.ic2modernadapter.machine.MachineSpec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only NeoForge registration for IC2 machine screens. */
@EventBusSubscriber(modid = IC2ModernAdapter.MOD_ID, value = Dist.CLIENT)
public final class MachineClientRegistration {
    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(MachinePlatform.menuType(MachineSpec.MACERATOR), MaceratorScreen::new);
        event.register(MachinePlatform.menuType(MachineSpec.COMPRESSOR), CompressorScreen::new);
        event.register(MachinePlatform.menuType(MachineSpec.EXTRACTOR), ExtractorScreen::new);
        event.register(MachinePlatform.menuType(MachineSpec.METAL_FORMER), MetalFormerScreen::new);
        event.register(MachinePlatform.menuType(MachineSpec.ORE_WASHING_PLANT), OreWashingPlantScreen::new);
        event.register(MachinePlatform.menuType(MachineSpec.THERMAL_CENTRIFUGE), ThermalCentrifugeScreen::new);
    }

    private MachineClientRegistration() {
    }
}
