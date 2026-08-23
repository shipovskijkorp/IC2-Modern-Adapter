package com.shipovskijkorp.ic2modernadapter.content.item;

import com.shipovskijkorp.ic2modernadapter.radiation.RadioactivitySpec;
import com.shipovskijkorp.ic2modernadapter.radiation.RadiationPlatform;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** IC2 item that applies the original inventory radiation exposure while carried. */
public final class RadioactiveItem extends LegacyTranslatedItem {
    private final String itemPath;
    private final Function<ItemStack, String> variantResolver;

    public RadioactiveItem(String itemPath, Properties properties, Function<ItemStack, String> variantResolver) {
        super(itemPath, properties, variantResolver);
        this.itemPath = Objects.requireNonNull(itemPath, "itemPath");
        this.variantResolver = Objects.requireNonNull(variantResolver, "variantResolver");
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide() || !(entity instanceof LivingEntity living)) {
            return;
        }

        RadioactivitySpec exposure = RadioactivitySpec.forItemStack(itemPath, variantResolver.apply(stack));
        if (exposure != null) {
            RadiationPlatform.applyTo(living, exposure.durationTicks(), exposure.amplifier());
        }
    }
}
