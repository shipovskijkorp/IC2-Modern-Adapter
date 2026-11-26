package com.shipovskijkorp.ic2modernadapter.worldgen;

import com.shipovskijkorp.ic2modernadapter.content.block.RubberWoodBlock;
import com.shipovskijkorp.ic2modernadapter.content.block.RubberWoodBlock.Resin;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Shared IC2 tree shape for world generation and player-planted saplings. */
public final class RubberTreeGrowth {
    private static final Direction[] FACES = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public static boolean grow(LevelAccessor level, BlockPos pos, RandomSource random, boolean natural) {
        if (level.isOutsideBuildHeight(pos) || level.isOutsideBuildHeight(pos.below())) return false;
        BlockState sapling = IC2ContentRegistries.block("sapling").get().defaultBlockState();
        BlockState base = level.getBlockState(pos);
        if ((!base.isAir() && !base.is(sapling.getBlock())) || !sapling.canSurvive(level, pos)) return false;
        int clearance = 1;
        while (clearance < RubberTreeRules.MAX_HEIGHT && !level.isOutsideBuildHeight(pos.above(clearance))
                && level.getBlockState(pos.above(clearance)).isAir()) {
            clearance++;
        }
        int height = RubberTreeRules.treeHeight(clearance, random::nextInt);
        if (height == 0) return false;

        BlockState wood = IC2ContentRegistries.block("rubber_wood").get().defaultBlockState();
        BlockState leaves = IC2ContentRegistries.block("leaves").get().defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, false).setValue(LeavesBlock.WATERLOGGED, false);
        if (natural && level instanceof net.minecraft.world.level.WorldGenLevel generation) {
            if (!generation.ensureCanWrite(pos) || !generation.ensureCanWrite(pos.offset(-2, 0, -2))
                    || !generation.ensureCanWrite(pos.offset(2, 0, 2))) return false;
        }
        int flags = natural ? Block.UPDATE_CLIENTS : Block.UPDATE_ALL;
        int holeChance = RubberTreeRules.FIRST_HOLE_CHANCE;
        for (int layer = 0; layer < height; layer++) {
            BlockState log = wood;
            if (random.nextInt(100) <= holeChance) {
                holeChance -= RubberTreeRules.HOLE_CHANCE_DECREMENT;
                log = wood.setValue(RubberWoodBlock.RESIN, Resin.forFace(FACES[random.nextInt(4)], true));
            }
            level.setBlock(pos.above(layer), log, flags);
            if (!RubberTreeRules.hasCanopy(height, layer)) continue;
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (!RubberTreeRules.hasLeaf(Math.abs(x), Math.abs(z), height, layer, random::nextInt)) continue;
                    placeLeaf(level, pos.offset(x, layer, z), leaves, Math.abs(x) + Math.abs(z), flags);
                }
            }
        }
        // IC2 uses a new random draw in the loop condition, not a fixed-length crown.
        for (int i = 0; i <= height / 4 + random.nextInt(2); i++) {
            placeLeaf(level, pos.above(height + i), leaves, i + 1, flags);
        }
        return true;
    }

    private static void placeLeaf(LevelAccessor level, BlockPos pos, BlockState leaves, int distance, int flags) {
        if (distance > 0 && !level.isOutsideBuildHeight(pos) && level.getBlockState(pos).isAir()) {
            level.setBlock(pos, leaves.setValue(LeavesBlock.DISTANCE, Math.min(6, distance)), flags);
        }
    }

    private RubberTreeGrowth() {}
}
