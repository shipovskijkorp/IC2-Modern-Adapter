package com.shipovskijkorp.ic2modernadapter.content.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Visual-only form of the IC2 dynamite block. It intentionally cannot ignite or link yet; the
 * properties exist solely so the original blockstate/model can be represented exactly.
 */
public final class PlaceholderDynamiteBlock extends Block {
    public static final DirectionProperty FACING = DirectionProperty.create(
            "facing", direction -> direction != Direction.DOWN);
    public static final BooleanProperty LINKED = BooleanProperty.create("linked");

    public PlaceholderDynamiteBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(LINKED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        if (facing == Direction.DOWN) {
            facing = Direction.UP;
        }
        return defaultBlockState().setValue(FACING, facing).setValue(LINKED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LINKED);
    }
}
