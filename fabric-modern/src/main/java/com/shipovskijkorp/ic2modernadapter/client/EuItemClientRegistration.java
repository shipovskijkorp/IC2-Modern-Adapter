package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.energy.item.*;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
public final class EuItemClientRegistration {
    public static void register() {
        for (EuElectricItemSpec spec : EuElectricItemSpec.values()) {
            if (spec.isBattery()) ItemProperties.register(IC2ContentRegistries.item(spec.itemPath()).get(),
                    ResourceLocation.fromNamespaceAndPath("ic2", "charge_level"), (stack, level, entity, seed) ->
                            EuChargeMath.modelLevel(EuItemManager.stored(stack), EuItemManager.capacity(stack)) / 4.0F);
        }
    }
    private EuItemClientRegistration() {}
}
