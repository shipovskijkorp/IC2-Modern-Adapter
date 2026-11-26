package com.shipovskijkorp.ic2modernadapter.content.block;

import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ItemInteractionResult;

public final class PlatformScaffoldBlock extends ConstructionScaffoldBlock {
    public PlatformScaffoldBlock(Properties properties, ToIntFunction<ItemStack> resolver) { super(properties, resolver); }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        return tryReinforce(stack, state, level, pos, player)
                ? ItemInteractionResult.sidedSuccess(level.isClientSide()) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        String key = com.shipovskijkorp.ic2modernadapter.worldgen.SilverOreVariant.keyAt("scaffold", state.getValue(VARIANT));
        return key == null ? new ItemStack(this) : IC2VariantStacks.create(key);
    }
}
