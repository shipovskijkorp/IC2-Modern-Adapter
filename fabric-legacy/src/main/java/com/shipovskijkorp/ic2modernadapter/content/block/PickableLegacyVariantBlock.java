package com.shipovskijkorp.ic2modernadapter.content.block;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import com.shipovskijkorp.ic2modernadapter.worldgen.SilverOreVariant;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
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

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        String key = SilverOreVariant.keyAt(blockPath, state.getValue(VARIANT));
        return key == null ? new ItemStack(this) : IC2VariantStacks.create(key);
    }
}
