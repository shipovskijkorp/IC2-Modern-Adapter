package com.shipovskijkorp.ic2modernadapter.energy.cable;

import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/** Minecraft 1.21.1 clone-stack signature bridge for the shared cable carrier block. */
public final class CableCarrierBlock extends CableBlock {
    public CableCarrierBlock(
            Properties properties,
            Function<ItemStack, String> itemVariantResolver,
            Function<String, ItemStack> variantStackFactory,
            CableEntityFactory cableEntityFactory) {
        super(properties, itemVariantResolver, variantStackFactory, cableEntityFactory);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return variantStack(state);
    }
}
