package com.shipovskijkorp.ic2modernadapter.content.item;

import com.shipovskijkorp.ic2modernadapter.content.block.RubberWoodBlock;
import com.shipovskijkorp.ic2modernadapter.content.block.RubberWoodBlock.Resin;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import com.shipovskijkorp.ic2modernadapter.worldgen.RubberTreeRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Both treetaps use the same server-authoritative extraction and drop behavior. */
final class ResinExtraction {
    static boolean canTap(BlockState state, Direction face) {
        return state.getBlock() instanceof RubberWoodBlock && state.getValue(RubberWoodBlock.RESIN).face() == face;
    }

    static boolean extract(Level level, BlockPos pos, Direction face, BlockState state) {
        if (level.isClientSide() || !canTap(state, face)) return false;
        Resin resin = state.getValue(RubberWoodBlock.RESIN);
        int amount;
        if (resin.wet()) {
            if (!level.setBlock(pos, state.setValue(RubberWoodBlock.RESIN, Resin.forFace(face, false)), Block.UPDATE_ALL)) return false;
            amount = RubberTreeRules.wetYield(level.random::nextInt);
        } else {
            // Two independent 1/5 rolls as in IC2; do not duplicate the original's accidental double ejection.
            RubberTreeRules.DryTap outcome = RubberTreeRules.dryTap(level.random::nextInt);
            if (outcome.destroySpot()) {
                level.setBlock(pos, state.setValue(RubberWoodBlock.RESIN, Resin.PLAIN), Block.UPDATE_ALL);
            }
            if (!outcome.yieldResin()) return false;
            amount = 1;
        }
        for (int i = 0; i < amount; i++) {
            ItemStack resinStack = IC2VariantStacks.create("misc_resource/resin");
            ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5 + face.getStepX() * 0.3,
                    pos.getY() + 0.5, pos.getZ() + 0.5 + face.getStepZ() * 0.3, resinStack);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }
        level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.7F, 1.0F);
        return true;
    }

    private ResinExtraction() {}
}
