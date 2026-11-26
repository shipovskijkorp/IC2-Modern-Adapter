package com.shipovskijkorp.ic2modernadapter.content.block;

import com.shipovskijkorp.ic2modernadapter.worldgen.RubberTreeGrowth;
import com.shipovskijkorp.ic2modernadapter.worldgen.RubberTreeRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Native sapling with original IC2 random growth and bonemeal behavior. */
public final class RubberSaplingBlock extends BushBlock implements BonemealableBlock {
    public static final com.mojang.serialization.MapCodec<RubberSaplingBlock> CODEC = simpleCodec(RubberSaplingBlock::new);
    @Override protected com.mojang.serialization.MapCodec<RubberSaplingBlock> codec() { return CODEC; }
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 12, 14);

    public RubberSaplingBlock(Properties properties) { super(properties); }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(RubberTreeRules.SAPLING_GROWTH_CHANCE) == 0) {
            RubberTreeGrowth.grow(level, pos, random, false);
        }
    }

    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return true; }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        RubberTreeGrowth.grow(level, pos, random, false);
    }
}
