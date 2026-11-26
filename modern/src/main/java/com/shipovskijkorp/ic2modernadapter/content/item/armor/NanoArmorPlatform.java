package com.shipovskijkorp.ic2modernadapter.content.item.armor;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public final class NanoArmorPlatform {
    private NanoArmorPlatform() {}
    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NanoArmorPlatform::hurt);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NanoArmorPlatform::fall);
    }
    private static void hurt(LivingIncomingDamageEvent event) {
        // Defer EU usage until the actual armor stage, after the shield and invulnerability checks.
        var entity = event.getEntity();
        event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, vanillaReduction) -> {
            float incoming = container.getNewDamage();
            if (incoming <= 0 || vanillaReduction >= incoming) return vanillaReduction;
            float remaining = NanoArmorProtection.reduce(entity, container.getSource(), incoming);
            // Combine existing vanilla reduction multiplicatively with NanoSuit reduction.
            return Math.min(incoming, incoming - remaining + vanillaReduction * (remaining / incoming));
        });
    }
    private static void fall(LivingFallEvent event) {
        if (NanoArmorProtection.absorbSmallFall(event.getEntity(), event.getDistance(), event.getDamageMultiplier())) event.setCanceled(true);
    }
}
