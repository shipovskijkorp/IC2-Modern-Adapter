package com.shipovskijkorp.ic2modernadapter.content.block;

import java.util.function.ToIntFunction;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Visual-only IC2 TE placeholder preserving both subtype and six-way facing. */
public final class LegacyVariantFacingBlock extends Block {
    public static final String VARIANT_PROPERTY_NAME = "variant";
    public static final IntegerProperty VARIANT = LegacyVariantBlock.VARIANT;
    public static final DirectionProperty FACING = DirectionProperty.create("facing");

    private final int maxVariant;
    private final ToIntFunction<ItemStack> variantResolver;

    public LegacyVariantFacingBlock(Properties properties, int variantCount, ToIntFunction<ItemStack> variantResolver) {
        super(properties);
        if (variantCount < 2 || variantCount > LegacyVariantBlock.MAX_VARIANT_INDEX + 1) {
            throw new IllegalArgumentException(
                    "LegacyVariantFacingBlock variant count must be in [2, "
                            + (LegacyVariantBlock.MAX_VARIANT_INDEX + 1) + "]");
        }
        this.maxVariant = variantCount - 1;
        this.variantResolver = variantResolver;
        registerDefaultState(stateDefinition.any()
                .setValue(VARIANT, 0)
                .setValue(FACING, Direction.NORTH));
    }

    public IntegerProperty variantProperty() {
        return VARIANT;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int variant = variantResolver.applyAsInt(context.getItemInHand());
        if (variant < 0 || variant > maxVariant) {
            variant = 0;
        }
        Direction facing = context.getNearestLookingDirection().getOpposite();
        return defaultBlockState()
                .setValue(VARIANT, variant)
                .setValue(FACING, facing);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
        builder.add(FACING);
    }
}
