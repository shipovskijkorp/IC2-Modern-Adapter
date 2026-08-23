package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.client.screen.ElectricFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.InductionFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.IronFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.furnace.FurnacePlatform;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only NeoForge registration for IC2 furnace screens. */
@EventBusSubscriber(modid = IC2ModernAdapter.MOD_ID, value = Dist.CLIENT)
public final class FurnaceClientRegistration {
    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(FurnacePlatform.ironMenuType(), IronFurnaceScreen::new);
        event.register(FurnacePlatform.electricMenuType(), ElectricFurnaceScreen::new);
        event.register(FurnacePlatform.inductionMenuType(), InductionFurnaceScreen::new);
    }

    private FurnaceClientRegistration() {
    }
}
