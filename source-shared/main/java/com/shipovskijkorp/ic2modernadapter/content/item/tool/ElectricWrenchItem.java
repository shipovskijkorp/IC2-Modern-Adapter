package com.shipovskijkorp.ic2modernadapter.content.item.tool;

import com.shipovskijkorp.ic2modernadapter.content.item.EuElectricItem;
import com.shipovskijkorp.ic2modernadapter.energy.item.EuElectricItemSpec;
import com.shipovskijkorp.ic2modernadapter.energy.item.EuItemManager;
import java.util.function.Function;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class ElectricWrenchItem extends EuElectricItem {
    public ElectricWrenchItem(String path, Properties properties, Function<ItemStack, String> variants) {
        super(path, EuElectricItemSpec.ELECTRIC_WRENCH, properties.stacksTo(1), variants);
    }
    @Override public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer(); ItemStack stack = ctx.getItemInHand();
        if (player == null || !WrenchAction.target(ctx.getLevel().getBlockState(ctx.getClickedPos()))) return InteractionResult.PASS;
        if (ctx.getLevel().isClientSide()) return WrenchAction.target(ctx.getLevel().getBlockState(ctx.getClickedPos())) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        boolean creative = player.getAbilities().instabuild;
        EuItemManager.chargeFromArmor(stack, player);
        if (!creative && !EuItemManager.canUse(stack, 100)) return InteractionResult.FAIL;
        WrenchAction.Result result = WrenchAction.apply(ctx, creative || EuItemManager.canUse(stack, 1000));
        if (result == WrenchAction.Result.NOTHING) return InteractionResult.FAIL;
        if (!creative) EuItemManager.use(stack, result == WrenchAction.Result.ROTATED ? 100 : 1000, player);
        ctx.getLevel().playSound(null, ctx.getClickedPos(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.5F, 1.3F);
        return InteractionResult.CONSUME;
    }
}
