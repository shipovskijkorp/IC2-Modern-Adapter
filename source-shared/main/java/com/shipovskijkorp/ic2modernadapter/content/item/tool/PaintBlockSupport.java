/* Adapted from BuildCraft Community Edition, CustomPaintHelper and ItemPaintbrush_BC8.
 * Copyright (c) 2016 SpaceToad and the BuildCraft team. MPL-2.0.
 * Reference: BCCE-team/BuildCraft commit 4617cd1e089b2cf001f10914fc9f9328d6b2a31c.
 */
package com.shipovskijkorp.ic2modernadapter.content.item.tool;

import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantBlock;
import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.Property;

/** Registry recoloring, preserving properties, tile data, and paired beds, as in BCCE. */
public final class PaintBlockSupport {
    private PaintBlockSupport() {}
    private static final Set<String> UNCOLORED = Set.of("terracotta", "candle", "candle_cake", "shulker_box");
    private static final int REPLACE = 2 | 16 | 32;
    public static boolean supported(BlockState state, BlockEntity entity, DyeColor dye) {
        return entity instanceof SignBlockEntity || state.getBlock() instanceof BedBlock
                || state.getBlock() instanceof ShulkerBoxBlock || state.getBlock() instanceof BannerBlock
                || state.getBlock() instanceof WallBannerBlock || recolored(state, dye) != null;
    }
    public static BlockState recolored(BlockState source, DyeColor color) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(source.getBlock());
        if ("ic2".equals(key.getNamespace()) && "wall".equals(key.getPath()) && source.hasProperty(LegacyVariantBlock.VARIANT)) {
            int index = OriginalContentManifest.get().stackVariantIndex("wall/" + color.getName());
            return source.getValue(LegacyVariantBlock.VARIANT) == index ? null : source.setValue(LegacyVariantBlock.VARIANT, index);
        }
        String path = key.getPath(), suffix = null;
        // BCCE recognizes existing color families (longest names first, so light_blue is not blue).
        for (DyeColor existing : DyeColor.values()) {
            String prefix = existing.getName() + "_";
            if (path.startsWith(prefix)) { suffix = path.substring(prefix.length()); break; }
        }
        if (suffix == null) {
            suffix = switch (path) {
                case "glass" -> "stained_glass";
                case "glass_pane" -> "stained_glass_pane";
                default -> UNCOLORED.contains(path) ? path : null;
            };
        }
        if (suffix == null) {
            Block block = source.getBlock();
            if (block instanceof GlazedTerracottaBlock) suffix = "glazed_terracotta";
            else if (block instanceof CandleBlock) suffix = "candle";
            else if (block instanceof CandleCakeBlock) suffix = "candle_cake";
            else if (block instanceof StainedGlassBlock) suffix = "stained_glass";
            else if (block instanceof StainedGlassPaneBlock) suffix = "stained_glass_pane";
            else if (block instanceof WoolCarpetBlock) suffix = "carpet";
            else if (block instanceof BannerBlock) suffix = "banner";
            else if (block instanceof WallBannerBlock) suffix = "wall_banner";
            else if (block instanceof BedBlock) suffix = "bed";
            else if (block instanceof ShulkerBoxBlock) suffix = "shulker_box";
            else if (block instanceof ConcretePowderBlock) suffix = "concrete_powder";
        }
        if (suffix == null || suffix.isEmpty()) return null;
        Block target = BuiltInRegistries.BLOCK.get(ToolVersionSupport.id(key.getNamespace(), color.getName() + "_" + suffix));
        if (target == null || target == Blocks.AIR || target == source.getBlock()) return null;
        BlockState result = target.defaultBlockState();
        for (Property<?> property : source.getProperties()) if (result.hasProperty(property)) result = transfer(source, result, property);
        return result;
    }
    private static <T extends Comparable<T>> BlockState transfer(BlockState from, BlockState to, Property<T> property) {
        T value = from.getValue(property);
        return property.getPossibleValues().contains(value) ? to.setValue(property, value) : to;
    }
    public static boolean paint(Level level, BlockPos pos, DyeColor color, Player player) {
        if (level.isClientSide() || !level.hasChunkAt(pos) || !level.mayInteract(player, pos) || !player.mayBuild()) return false;
        if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            if (sign.isWaxed()) return false;
            boolean front = sign.isFacingFrontText(player);
            if (sign.getText(front).getColor() == color) return false;
            return sign.updateText(text -> text.setColor(color), front);
        }
        BlockState old = level.getBlockState(pos), colored = recolored(old, color);
        if (colored == null) return false;
        if (old.getBlock() instanceof BedBlock) return bed(level, pos, old, colored, color, player);
        BlockEntity previous = level.getBlockEntity(pos);
        if (previous != null && !colored.hasBlockEntity()) return false;
        CompoundTag data = previous == null ? null : ToolVersionSupport.saveBlockEntity(previous, level);
        // ShulkerBoxBlock onRemove normally drops its entire inventory. Temporarily clear it
        // before the color-only replacement, then restore it in the replacement block entity.
        if (previous instanceof net.minecraft.world.Container container && data != null) container.clearContent();
        if (!level.setBlock(pos, colored, REPLACE)) {
            if (previous != null && data != null) ToolVersionSupport.loadBlockEntity(previous, level, data);
            return false;
        }
        if (data != null) {
            BlockEntity replacement = level.getBlockEntity(pos);
            if (replacement == null || replacement.getType() != previous.getType()) {
                level.setBlock(pos, old, REPLACE);
                BlockEntity reverted = level.getBlockEntity(pos);
                if (reverted != null) ToolVersionSupport.loadBlockEntity(reverted, level, data);
                return false;
            }
            ToolVersionSupport.loadBlockEntity(replacement, level, data);
            replacement.setChanged();
        }
        level.updateNeighborsAt(pos, colored.getBlock());
        level.sendBlockUpdated(pos, old, colored, 3);
        return true;
    }
    private static boolean bed(Level level, BlockPos pos, BlockState old, BlockState colored, DyeColor color, Player player) {
        Direction facing = old.getValue(BedBlock.FACING);
        BedPart part = old.getValue(BedBlock.PART);
        BlockPos other = pos.relative(part == BedPart.FOOT ? facing : facing.getOpposite());
        if (!level.hasChunkAt(other) || !level.mayInteract(player, other)) return false;
        BlockState partner = level.getBlockState(other);
        if (partner.getBlock() != old.getBlock() || partner.getValue(BedBlock.FACING) != facing || partner.getValue(BedBlock.PART) == part) return false;
        BlockState target = recolored(partner, color);
        if (target == null || target.getBlock() != colored.getBlock()) return false;
        if (!level.setBlock(pos, colored, REPLACE)) return false;
        if (!level.setBlock(other, target, REPLACE)) { level.setBlock(pos, old, REPLACE); return false; }
        level.updateNeighborsAt(pos, colored.getBlock());
        level.updateNeighborsAt(other, target.getBlock());
        level.sendBlockUpdated(pos, old, colored, 3);
        level.sendBlockUpdated(other, partner, target, 3);
        return true;
    }
}
