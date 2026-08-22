package com.shipovskijkorp.ic2modernadapter.content.block;

import java.util.function.ToIntFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Inert placeholder for a legacy IC2 block whose old metadata selected one of several models.
 *
 * <p>The block deliberately has no machine/gameplay behavior. Its only state is the finite visual
 * variant needed to preserve the identity carried by the original ItemStack.</p>
 */
public class LegacyVariantBlock extends Block {
    public static final String VARIANT_PROPERTY_NAME = "variant";

    /**
     * The largest finite block-item subtype table in IC2 2.8.222 is {@code ic2:te} with 106
     * variants. A shared property is required because {@link Block}'s constructor builds the state
     * definition before subclass instance fields are initialized.
     */
    public static final int MAX_VARIANT_INDEX = 105;
    public static final IntegerProperty VARIANT =
            IntegerProperty.create(VARIANT_PROPERTY_NAME, 0, MAX_VARIANT_INDEX);

    private final int maxVariant;
    private final ToIntFunction<ItemStack> variantResolver;

    public LegacyVariantBlock(Properties properties, int variantCount, ToIntFunction<ItemStack> variantResolver) {
        super(properties);
        if (variantCount < 2 || variantCount > MAX_VARIANT_INDEX + 1) {
            throw new IllegalArgumentException(
                    "LegacyVariantBlock variant count must be in [2, " + (MAX_VARIANT_INDEX + 1) + "]");
        }
        this.maxVariant = variantCount - 1;
        this.variantResolver = variantResolver;
        registerDefaultState(stateDefinition.any().setValue(VARIANT, 0));
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
        return defaultBlockState().setValue(VARIANT, variant);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }
}
