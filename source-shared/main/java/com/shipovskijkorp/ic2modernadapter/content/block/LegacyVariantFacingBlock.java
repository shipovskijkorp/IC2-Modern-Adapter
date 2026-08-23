package com.shipovskijkorp.ic2modernadapter.content.block;

import com.shipovskijkorp.ic2modernadapter.generator.GeneratorConstants;
import com.shipovskijkorp.ic2modernadapter.machine.MachineSpec;
import java.util.function.ToIntFunction;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** IC2 TE root block preserving subtype, facing and machine active state. */
public class LegacyVariantFacingBlock extends Block {
    public static final String VARIANT_PROPERTY_NAME = "variant";
    public static final IntegerProperty VARIANT = LegacyVariantBlock.VARIANT;
    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

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
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    public IntegerProperty variantProperty() {
        return VARIANT;
    }

    protected final int resolvePlacementVariant(ItemStack stack) {
        int variant = variantResolver.applyAsInt(stack);
        return variant < 0 || variant > maxVariant ? 0 : variant;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int variant = resolvePlacementVariant(context.getItemInHand());
        Direction facing = variant == GeneratorConstants.VARIANT_INDEX || MachineSpec.isMachineVariantIndex(variant)
                ? context.getHorizontalDirection().getOpposite()
                : context.getNearestLookingDirection().getOpposite();
        return defaultBlockState()
                .setValue(VARIANT, variant)
                .setValue(FACING, facing)
                .setValue(ACTIVE, false);
    }


    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
        builder.add(FACING);
        builder.add(ACTIVE);
    }
}
