package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.client.screen.ToolBoxScreen;
import com.shipovskijkorp.ic2modernadapter.toolbox.ToolBoxPlatform;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only Forge registration for the IC2 Tool Box screen. */
@Mod.EventBusSubscriber(
        modid = IC2ModernAdapter.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class ToolBoxClientRegistration {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ToolBoxPlatform.menuType(), ToolBoxScreen::new));
    }

    private ToolBoxClientRegistration() {
    }
}
