package com.shipovskijkorp.ic2modernadapter.content.item;

import com.shipovskijkorp.ic2modernadapter.energy.item.EuElectricItemSpec;
import com.shipovskijkorp.ic2modernadapter.energy.item.EuItemManager;
import java.util.function.Function;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

/** Tier-one electric treetap, 10,000 EU capacity and 50 EU per successful use. */
public final class ElectricTreetapItem extends EuElectricItem {
    public static final long OPERATION_COST = 50L;

    public ElectricTreetapItem(String path, Properties properties, Function<ItemStack, String> variantResolver) {
        super(path, EuElectricItemSpec.ELECTRIC_TREETAP, properties, variantResolver);
    }

    @Override public boolean canProvideEu(ItemStack stack) { return false; }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (context.getPlayer() == null || !ResinExtraction.canTap(state, context.getClickedFace())) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        if (!context.getPlayer().getAbilities().instabuild) EuItemManager.chargeFromArmor(stack, context.getPlayer());
        if (!EuItemManager.canUse(stack, OPERATION_COST)) return InteractionResult.PASS;
        if (!ResinExtraction.extract(context.getLevel(), context.getClickedPos(), context.getClickedFace(), state)) return InteractionResult.PASS;
        if (!context.getPlayer().getAbilities().instabuild) EuItemManager.use(stack, OPERATION_COST, context.getPlayer());
        return InteractionResult.CONSUME;
    }
}
