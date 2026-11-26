package com.shipovskijkorp.ic2modernadapter.content.item.armor;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;

public final class NanoArmorPlatform {
    private NanoArmorPlatform() {}
    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, NanoArmorPlatform::hurt);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, NanoArmorPlatform::fall);
    }
    private static void hurt(LivingHurtEvent event) {
        event.setAmount(NanoArmorProtection.reduce(event.getEntity(), event.getSource(), event.getAmount()));
    }
    private static void fall(LivingFallEvent event) {
        if (NanoArmorProtection.absorbSmallFall(event.getEntity(), event.getDistance(), event.getDamageMultiplier())) event.setCanceled(true);
    }
}
