package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.client.screen.ElectricFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.InductionFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.client.screen.IronFurnaceScreen;
import com.shipovskijkorp.ic2modernadapter.furnace.FurnacePlatform;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only Forge registration for IC2 furnace screens. */
@Mod.EventBusSubscriber(
        modid = IC2ModernAdapter.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class FurnaceClientRegistration {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(FurnacePlatform.ironMenuType(), IronFurnaceScreen::new);
            MenuScreens.register(FurnacePlatform.electricMenuType(), ElectricFurnaceScreen::new);
            MenuScreens.register(FurnacePlatform.inductionMenuType(), InductionFurnaceScreen::new);
        });
    }

    private FurnaceClientRegistration() {
    }
}
