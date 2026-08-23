package com.shipovskijkorp.ic2modernadapter.content.item;

import com.shipovskijkorp.ic2modernadapter.energy.cable.CableBlock;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** IC2 Insulation Cutter: crafting tool plus cable insulation add/remove interactions. */
public final class WireCutterItem extends LegacyCraftingToolItem {
    public static final int MAX_USES = 60;
    private static final String RUBBER_VARIANT = "crafting/rubber";

    private final Function<ItemStack, String> variantResolver;

    public WireCutterItem(
            String itemPath,
            Properties properties,
            Function<ItemStack, String> variantResolver) {
        super(itemPath, properties, variantResolver);
        this.variantResolver = variantResolver;
    }

    /**
     * Original right-click behavior: with one rubber anywhere in the inventory, add one insulation
     * layer and spend one cutter use. The held cutter is the tool; rubber does not need to be held.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CableBlock) || !CableBlock.canAddInsulation(state)) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player == null || !hasRubber(player)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!CableBlock.tryAddInsulation(level, pos)) {
            return InteractionResult.PASS;
        }
        consumeRubber(player);
        damageHeldTool(context.getItemInHand(), 1, player);
        return InteractionResult.SUCCESS;
    }

    private boolean hasRubber(Player player) {
        Inventory inventory = player.getInventory();
        // The 1.12 implementation searches mainInventory only. Creative mode skips consumption,
        // but it still requires a rubber item to be present before the interaction succeeds.
        int mainSlots = Math.min(36, inventory.getContainerSize());
        for (int slot = 0; slot < mainSlots; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (RUBBER_VARIANT.equals(variantResolver.apply(stack)) && !stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void consumeRubber(Player player) {
        if (player.getAbilities().instabuild) {
            return;
        }
        Inventory inventory = player.getInventory();
        int mainSlots = Math.min(36, inventory.getContainerSize());
        for (int slot = 0; slot < mainSlots; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (RUBBER_VARIANT.equals(variantResolver.apply(stack)) && !stack.isEmpty()) {
                stack.shrink(1);
                inventory.setChanged();
                return;
            }
        }
    }
}
