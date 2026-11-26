package com.shipovskijkorp.ic2modernadapter.content.item.tool;

import com.shipovskijkorp.ic2modernadapter.content.item.LegacyCraftingToolItem;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import java.util.function.Function;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class ClassicWrenchItem extends LegacyCraftingToolItem {
    public static final int MAX_USES = 120;
    public ClassicWrenchItem(String path, Properties properties, Function<ItemStack, String> variants) { super(path, properties.durability(MAX_USES), variants); }
    @Override public boolean isValidRepairItem(ItemStack stack, ItemStack repair) { return "ingot/bronze".equals(IC2VariantStacks.variantKey(repair)); }
    @Override public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null || !WrenchAction.target(ctx.getLevel().getBlockState(ctx.getClickedPos()))) return InteractionResult.PASS;
        WrenchAction.Result result = WrenchAction.apply(ctx, true);
        if (result == WrenchAction.Result.NOTHING) return InteractionResult.FAIL;
        if (ctx.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        damageHeldTool(ctx.getItemInHand(), result == WrenchAction.Result.ROTATED ? 1 : 10, player);
        ctx.getLevel().playSound(null, ctx.getClickedPos(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.5F, 1.3F);
        return InteractionResult.CONSUME;
    }
}
