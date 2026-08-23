package com.shipovskijkorp.ic2modernadapter.content.item;

import com.shipovskijkorp.ic2modernadapter.radiation.RadiationPlatform;
import java.util.function.Function;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** IC2 iodine tablet: spends one tablet per remaining second of radiation removed. */
public final class IodineTabletItem extends LegacyTranslatedItem {
    public IodineTabletItem(String itemPath, Properties properties, Function<ItemStack, String> variantResolver) {
        super(itemPath, properties, variantResolver);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }

        int consumed = RadiationPlatform.reduceRadiationByIodine(player, stack.getCount());
        if (consumed <= 0) {
            return InteractionResultHolder.pass(stack);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(consumed);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return InteractionResultHolder.success(stack);
    }
}
