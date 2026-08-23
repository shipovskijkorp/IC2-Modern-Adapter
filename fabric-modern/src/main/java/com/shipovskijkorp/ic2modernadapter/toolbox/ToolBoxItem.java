package com.shipovskijkorp.ic2modernadapter.toolbox;

import com.shipovskijkorp.ic2modernadapter.content.item.LegacyTranslatedItem;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Original IC2 Tool Box item. Stores small tool stacks in an internal 9-slot inventory. */
public final class ToolBoxItem extends LegacyTranslatedItem {
    public ToolBoxItem(String itemPath, Properties properties, Function<ItemStack, String> variantResolver) {
        super(itemPath, properties.stacksTo(1), variantResolver);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inventory, menuPlayer) -> new ToolBoxMenu(ToolBoxPlatform.menuType(), id, inventory, stack),
                    stack.getHoverName()));
        }
        return InteractionResultHolder.success(stack);
    }

    public static boolean isAllowed(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() instanceof ToolBoxItem) {
            return false;
        }
        if (stack.isDamageableItem()) {
            return true;
        }
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return id.startsWith("ic2:") && (id.contains("tool")
                || id.contains("hammer")
                || id.contains("cutter")
                || id.contains("wrench")
                || id.contains("meter")
                || id.contains("scanner")
                || id.contains("drill")
                || id.contains("chainsaw")
                || id.contains("treetap")
                || id.contains("battery")
                || id.contains("crystal")
                || id.contains("dynamite")
                || id.equals("ic2:cable")
                || id.equals("ic2:pipe"));
    }
}
