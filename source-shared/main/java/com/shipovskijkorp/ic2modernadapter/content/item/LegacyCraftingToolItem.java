package com.shipovskijkorp.ic2modernadapter.content.item;

import java.util.function.Function;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Shared durability behavior for IC2's Forge Hammer and Insulation Cutter. */
public class LegacyCraftingToolItem extends LegacyTranslatedItem {
    public LegacyCraftingToolItem(
            String itemPath,
            Properties properties,
            Function<ItemStack, String> variantResolver) {
        super(itemPath, properties, variantResolver);
    }

    /** Applies tool-use damage without loader hooks; creative players keep an undamaged tool. */
    public static void damageHeldTool(ItemStack stack, int amount, Player player) {
        if (stack == null || stack.isEmpty() || amount <= 0 || player.getAbilities().instabuild) {
            return;
        }
        if (!stack.isDamageableItem()) {
            return;
        }
        int next = stack.getDamageValue() + amount;
        if (next >= stack.getMaxDamage()) {
            stack.shrink(1);
        } else {
            stack.setDamageValue(next);
        }
    }
}
