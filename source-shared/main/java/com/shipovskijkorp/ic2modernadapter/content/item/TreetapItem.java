package com.shipovskijkorp.ic2modernadapter.content.item;

import com.shipovskijkorp.ic2modernadapter.content.block.RubberWoodBlock;
import java.util.function.Function;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

/** Original 16-use manual treetap. */
public final class TreetapItem extends LegacyCraftingToolItem {
    public static final int MAX_USES = 16;

    public TreetapItem(String path, Properties properties, Function<ItemStack, String> variantResolver) {
        super(path, properties, variantResolver);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (!(state.getBlock() instanceof RubberWoodBlock) || context.getPlayer() == null) return InteractionResult.PASS;
        if (!ResinExtraction.canTap(state, context.getClickedFace())) return InteractionResult.FAIL;
        if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        if (!ResinExtraction.extract(context.getLevel(), context.getClickedPos(), context.getClickedFace(), state)) return InteractionResult.FAIL;
        damageHeldTool(context.getItemInHand(), 1, context.getPlayer());
        return InteractionResult.CONSUME;
    }
}
