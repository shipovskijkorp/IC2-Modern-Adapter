package com.shipovskijkorp.ic2modernadapter.containment;

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

/** Original IC2 Containment Box item. Stores radioactive material in an internal 12-slot inventory. */
public final class ContainmentBoxItem extends LegacyTranslatedItem {
    public ContainmentBoxItem(String itemPath, Properties properties, Function<ItemStack, String> variantResolver) {
        super(itemPath, properties.stacksTo(1), variantResolver);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inventory, menuPlayer) -> new ContainmentBoxMenu(ContainmentBoxPlatform.menuType(), id, inventory, stack),
                    stack.getHoverName()));
        }
        return InteractionResultHolder.success(stack);
    }

    /** Original SlotRadioactive accepts the entire nuclear item family and fuel rods. */
    public static boolean isAllowed(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && "ic2".equals(id.getNamespace()) && ("nuclear".equals(id.getPath())
                || com.shipovskijkorp.ic2modernadapter.radiation.RadioactivitySpec.radioactiveFuelRods().contains(id.getPath()));
    }
}
