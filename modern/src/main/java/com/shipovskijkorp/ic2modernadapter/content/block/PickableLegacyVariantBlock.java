package com.shipovskijkorp.ic2modernadapter.content.block;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import com.shipovskijkorp.ic2modernadapter.content.VanillaBasalt;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import com.shipovskijkorp.ic2modernadapter.worldgen.SilverOreVariant;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/** Restores the actual IC2 metadata variant on creative middle-click (Pick Block). */
public final class PickableLegacyVariantBlock extends LegacyVariantBlock {
    private final String blockPath;

    public PickableLegacyVariantBlock(
            Properties properties, String blockPath, int variantCount,
            ToIntFunction<ItemStack> variantResolver) {
        super(properties, variantCount, variantResolver);
        this.blockPath = blockPath;
    }

    // Do not create new IC2 basalt blocks, even from a legacy inventory stack.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (VanillaBasalt.isLegacyState(blockPath, state.getValue(VARIANT))) {
            return Blocks.BASALT.getStateForPlacement(context);
        }
        return state;
    }

    // Lazily migrate existing placed basalt without changing the other resource indices.
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return VanillaBasalt.isLegacyState(blockPath, state.getValue(VARIANT));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (VanillaBasalt.isLegacyState(blockPath, state.getValue(VARIANT))) {
            level.setBlock(pos, Blocks.BASALT.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        String key = SilverOreVariant.keyAt(blockPath, state.getValue(VARIANT));
        return key == null ? new ItemStack(this) : IC2VariantStacks.create(key);
    }
}
