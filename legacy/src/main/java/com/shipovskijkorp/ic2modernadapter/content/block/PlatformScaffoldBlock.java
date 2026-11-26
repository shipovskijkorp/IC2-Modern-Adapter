package com.shipovskijkorp.ic2modernadapter.content.block;

import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionResult;

public final class PlatformScaffoldBlock extends ConstructionScaffoldBlock {
    public PlatformScaffoldBlock(Properties properties, ToIntFunction<ItemStack> resolver) { super(properties, resolver); }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return tryReinforce(player.getItemInHand(hand), state, level, pos, player)
                ? InteractionResult.sidedSuccess(level.isClientSide()) : InteractionResult.PASS;
    }
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        String key = com.shipovskijkorp.ic2modernadapter.worldgen.SilverOreVariant.keyAt("scaffold", state.getValue(VARIANT));
        return key == null ? new ItemStack(this) : IC2VariantStacks.create(key);
    }
}
