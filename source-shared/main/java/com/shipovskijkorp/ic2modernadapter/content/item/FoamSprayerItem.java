package com.shipovskijkorp.ic2modernadapter.content.item;

import com.shipovskijkorp.ic2modernadapter.content.block.ConstructionFoamBlock;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantBlock;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import com.shipovskijkorp.ic2modernadapter.worldgen.SilverOreVariant;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Portable 8-bucket construction foam tank; 100 mB per successful block. */
public final class FoamSprayerItem extends LegacyTranslatedItem {
    public static final int CAPACITY = 8000;
    public static final int PER_BLOCK = 100;
    public static final int NORMAL_LIMIT = 10;

    public FoamSprayerItem(String path, Properties properties, Function<ItemStack, String> variantResolver) {
        super(path, properties, variantResolver);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) { return true; }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * FoamCharge.get(stack) / CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) { return 0xC0C0C0; }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        ItemStack sprayer = context.getItemInHand();
        int available = FoamCharge.get(sprayer);
        int limit = Math.min(player.isShiftKeyDown() ? 1 : NORMAL_LIMIT, available / PER_BLOCK);
        if (player.getAbilities().instabuild) limit = player.isShiftKeyDown() ? 1 : NORMAL_LIMIT;
        if (limit == 0) return InteractionResult.FAIL;
        BlockPos origin = context.getClickedPos();
        boolean scaffold = level.getBlockState(origin).is(IC2ContentRegistries.block("scaffold").get());
        if (!scaffold) origin = origin.relative(context.getClickedFace());
        if (!canSpray(level, origin, scaffold, player, context)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        var look = player.getLookAngle();
        Direction excluded = Direction.getNearest((float) look.x, (float) look.y, (float) look.z).getOpposite();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        int placed = 0;
        while (!queue.isEmpty() && placed < limit) {
            BlockPos pos = queue.removeFirst();
            if (!seen.add(pos) || !canSpray(level, pos, scaffold, player, context)) continue;
            BlockState old = level.getBlockState(pos);
            String key = scaffold ? SilverOreVariant.keyAt("scaffold", old.getValue(LegacyVariantBlock.VARIANT)) : null;
            if (scaffold && key == null) continue;
            boolean iron = "scaffold/iron".equals(key) || "scaffold/reinforced_iron".equals(key);
            if (!level.setBlock(pos, ConstructionFoamBlock.stateFor(iron ? "foam/reinforced" : "foam/normal"), Block.UPDATE_ALL)) continue;
            if (scaffold && !iron) {
                Block.popResource(level, pos, IC2VariantStacks.create("scaffold/wood"));
                if ("scaffold/reinforced_wood".equals(key)) {
                    Block.popResource(level, pos, new ItemStack(net.minecraft.world.item.Items.STICK, 2));
                }
            }
            if ("scaffold/reinforced_iron".equals(key)) Block.popResource(level, pos, IC2VariantStacks.create("fence/iron"));
            placed++;
            for (Direction side : Direction.values()) {
                if (side != excluded) queue.addLast(pos.relative(side));
            }
        }
        if (placed == 0) return InteractionResult.PASS;
        if (!player.getAbilities().instabuild) FoamCharge.set(sprayer, available - PER_BLOCK * placed);
        return InteractionResult.CONSUME;
    }

    private static boolean canSpray(Level level, BlockPos pos, boolean scaffold, Player player, UseOnContext context) {
        if (level.isOutsideBuildHeight(pos) || !level.hasChunkAt(pos) || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) return false;
        BlockState state = level.getBlockState(pos);
        if (scaffold) return state.is(IC2ContentRegistries.block("scaffold").get());
        return state.isAir() || (state.canBeReplaced() && state.getFluidState().isEmpty());
    }
}
