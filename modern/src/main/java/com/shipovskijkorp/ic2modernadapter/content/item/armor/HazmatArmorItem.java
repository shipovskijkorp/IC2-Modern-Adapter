package com.shipovskijkorp.ic2modernadapter.content.item.armor;

import com.shipovskijkorp.ic2modernadapter.radiation.RadiationPlatform;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** IC2 hazmat piece. A complete helmet/chest/legs/rubber-boots suit blocks inventory radiation. */
public final class HazmatArmorItem extends TranslatedArmorItem {
    public HazmatArmorItem(String itemPath, Holder<ArmorMaterial> material, Type type,
            Properties properties, Function<ItemStack, String> variantResolver) {
        super(itemPath, material, type, properties, variantResolver);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide() && entity instanceof LivingEntity living && HazmatSuit.hasCompleteSuit(living)) {
            RadiationPlatform.clearIfProtected(living);
        }
    }
}
