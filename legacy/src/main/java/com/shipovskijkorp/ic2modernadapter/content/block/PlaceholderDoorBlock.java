package com.shipovskijkorp.ic2modernadapter.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;

/** 1.20.1 door-shaped placeholder: placement/state geometry works, interaction logic does not. */
public final class PlaceholderDoorBlock extends DoorBlock {
    public PlaceholderDoorBlock(Properties properties) {
        super(properties, BlockSetType.IRON);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighbor,
            BlockPos neighborPos,
            boolean movedByPiston) {
        // Visual placeholder only: do not react to redstone yet.
    }
}
