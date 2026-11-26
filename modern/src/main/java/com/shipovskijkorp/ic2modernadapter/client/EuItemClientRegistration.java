package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.energy.item.*;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
@EventBusSubscriber(modid = com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter.MOD_ID, value = Dist.CLIENT)
public final class EuItemClientRegistration {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) { event.enqueueWork(EuItemClientRegistration::register); }
    public static void register() {
        for (EuElectricItemSpec spec : EuElectricItemSpec.values()) {
            if (spec.isBattery()) ItemProperties.register(IC2ContentRegistries.item(spec.itemPath()).get(),
                    ResourceLocation.fromNamespaceAndPath("ic2", "charge_level"), (stack, level, entity, seed) ->
                            EuChargeMath.modelLevel(EuItemManager.stored(stack), EuItemManager.capacity(stack)) / 4.0F);
        }
    }
    private EuItemClientRegistration() {}
}
