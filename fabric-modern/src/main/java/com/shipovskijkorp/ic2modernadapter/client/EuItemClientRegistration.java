package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.energy.item.*;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
public final class EuItemClientRegistration {
    public static void register() {
        ItemProperties.register(IC2ContentRegistries.item("nano_saber").get(), ResourceLocation.fromNamespaceAndPath("ic2", "saber_active"), (stack, level, entity, seed) ->
                com.shipovskijkorp.ic2modernadapter.content.item.tool.ElectricMiningToolItem.active(stack) ? 1F : 0F);
        for (EuElectricItemSpec spec : EuElectricItemSpec.values()) {
            if (spec.isBattery()) ItemProperties.register(IC2ContentRegistries.item(spec.itemPath()).get(),
                    ResourceLocation.fromNamespaceAndPath("ic2", "charge_level"), (stack, level, entity, seed) ->
                            EuChargeMath.modelLevel(EuItemManager.stored(stack), EuItemManager.capacity(stack)) / 4.0F);
        }
    }
    private EuItemClientRegistration() {}
}
