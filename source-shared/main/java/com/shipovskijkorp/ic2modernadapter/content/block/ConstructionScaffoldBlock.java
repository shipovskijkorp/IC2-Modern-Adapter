package com.shipovskijkorp.ic2modernadapter.content.block;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeRuntime;
import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeStacks;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Original material upgrade of supported wooden and iron scaffold pillars. */
public class ConstructionScaffoldBlock extends LegacyVariantBlock {
    public ConstructionScaffoldBlock(Properties properties, ToIntFunction<ItemStack> resolver) {
        super(properties, 4, resolver);
    }

    protected boolean tryReinforce(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
        if (player.isShiftKeyDown() || stack.isEmpty() || !level.mayInteract(player, pos)) return false;
        int variant = state.getValue(VARIANT);
        int cost;
        String output;
        if (variant == 0 && LegacyRecipeRuntime.matchesIngredient("ore:stickWood", stack, LegacyRecipeStacks.INSTANCE)) {
            cost = 2;
            output = "scaffold/reinforced_wood";
        } else if (variant == 2 && "fence/iron".equals(IC2VariantStacks.variantKey(stack))) {
            cost = 1;
            output = "scaffold/reinforced_iron";
        } else return false;
        if (stack.getCount() < cost) return false;
        BlockPos below = pos.below();
        while (!level.isOutsideBuildHeight(below) && level.getBlockState(below).is(this)) below = below.below();
        if (level.isOutsideBuildHeight(below) || !level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) return false;
        if (level.isClientSide()) return true;
        if (!level.setBlock(pos, ConstructionFoamBlock.stateFor(output), Block.UPDATE_ALL)) return false;
        if (!player.getAbilities().instabuild) stack.shrink(cost);
        return true;
    }
}
