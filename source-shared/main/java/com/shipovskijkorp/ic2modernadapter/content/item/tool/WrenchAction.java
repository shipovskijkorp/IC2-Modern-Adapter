package com.shipovskijkorp.ic2modernadapter.content.item.tool;

import com.shipovskijkorp.ic2modernadapter.content.block.LegacyTeBlock;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantFacingBlock;
import com.shipovskijkorp.ic2modernadapter.energy.storage.AbstractEuStorageBlockEntity;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageSpec;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import com.shipovskijkorp.ic2modernadapter.worldgen.SilverOreVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Classic IC2 wrench: face selection, followed by lossless dismantling on the same face. */
public final class WrenchAction {
    public enum Result { NOTHING, ROTATED, REMOVED }
    private WrenchAction() {}
    public static boolean target(BlockState state) {
        return state.getBlock() instanceof LegacyTeBlock
                || simpleVanilla(state);
    }
    private static boolean simpleVanilla(BlockState state) {
        return state.is(Blocks.FURNACE) || state.is(Blocks.BLAST_FURNACE) || state.is(Blocks.SMOKER)
                || state.is(Blocks.DISPENSER) || state.is(Blocks.DROPPER) || state.is(Blocks.OBSERVER)
                ;
    }
    public static Result apply(UseOnContext ctx, boolean remove) {
        Level level = ctx.getLevel(); BlockPos pos = ctx.getClickedPos(); Player player = ctx.getPlayer();
        if (player == null || !player.mayBuild() || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, ctx.getClickedFace(), ctx.getItemInHand())) return Result.NOTHING;
        BlockState state = level.getBlockState(pos);
        if (level.isClientSide()) return target(state) ? Result.ROTATED : Result.NOTHING;
        Direction facing = player.isShiftKeyDown() ? ctx.getClickedFace().getOpposite() : ctx.getClickedFace();
        if (state.getBlock() instanceof LegacyTeBlock && state.hasProperty(LegacyVariantFacingBlock.FACING)) {
            Direction old = state.getValue(LegacyVariantFacingBlock.FACING);
            EuStorageSpec storage = EuStorageSpec.fromBlockState(state);
            boolean horizontal = LegacyTeBlock.isGenerator(state) || LegacyTeBlock.isImplementedFurnace(state)
                    || com.shipovskijkorp.ic2modernadapter.machine.MachineSpec.fromBlockState(state) != null;
            boolean supported = (!horizontal || facing.getAxis().isHorizontal()) && (storage == null || !storage.isChargepad() || facing != Direction.UP);
            if (old != facing && supported && level.setBlock(pos, state.setValue(LegacyVariantFacingBlock.FACING, facing), Block.UPDATE_ALL)) {
                return Result.ROTATED;
            }
            if (!remove) return Result.NOTHING;
            String key = SilverOreVariant.keyAt("te", state.getValue(LegacyVariantFacingBlock.VARIANT));
            if (key == null || !(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                    || !ToolInteractionPlatform.mayDismantle(serverPlayer, pos, state)) return Result.NOTHING;
            ItemStack drop = IC2VariantStacks.create(key);
            if (level.getBlockEntity(pos) instanceof AbstractEuStorageBlockEntity energy) {
                // Same stored-EU item channel already used by LegacyTeBlock placement.
                IC2VariantStacks.setBlockEntityEnergy(drop, energy.getEuStored() * 4L / 5L);
            }
            // setBlock triggers the existing onRemove inventory spill, exactly once.
            if (!level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS)) return Result.NOTHING;
            if (!player.getAbilities().instabuild) Block.popResource(level, pos, drop);
            return Result.REMOVED;
        }
        // Avoid turning multi-block structures (beds, doors, extended pistons) into invalid states.
        if (!simpleVanilla(state)) return Result.NOTHING;
        if (state.hasProperty(BlockStateProperties.FACING) && state.getValue(BlockStateProperties.FACING) != facing
                && BlockStateProperties.FACING.getPossibleValues().contains(facing)) {
            return level.setBlock(pos, state.setValue(BlockStateProperties.FACING, facing), Block.UPDATE_ALL) ? Result.ROTATED : Result.NOTHING;
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && facing.getAxis().isHorizontal()
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) != facing) {
            return level.setBlock(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing), Block.UPDATE_ALL) ? Result.ROTATED : Result.NOTHING;
        }
        return Result.NOTHING;
    }
}
