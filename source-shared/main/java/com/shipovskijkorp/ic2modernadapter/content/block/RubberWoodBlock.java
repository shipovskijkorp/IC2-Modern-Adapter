package com.shipovskijkorp.ic2modernadapter.content.block;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import com.shipovskijkorp.ic2modernadapter.worldgen.RubberTreeRules;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;

/** IC2 rubber wood: ordinary logs and renewable, directional resin spots. */
public final class RubberWoodBlock extends RotatedPillarBlock {
    public static final EnumProperty<Resin> RESIN = EnumProperty.create("resin", Resin.class);

    public RubberWoodBlock(Properties properties) {
        super(properties);
        // Keep the existing axis property so previously placed adapter logs remain ordinary wood.
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.Y).setValue(RESIN, Resin.PLAIN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RESIN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(RESIN, Resin.PLAIN);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(RESIN).isDry();
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Resin resin = state.getValue(RESIN);
        if (resin.isDry() && RubberTreeRules.shouldRefill(random::nextInt)) {
            level.setBlock(pos, state.setValue(RESIN, Resin.forFace(resin.face(), true)), Block.UPDATE_ALL);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> base = super.getDrops(state, params);
        if (base.isEmpty() || state.getValue(RESIN) == Resin.PLAIN || params.getLevel().getRandom().nextInt(6) != 0) {
            return base;
        }
        List<ItemStack> drops = new ArrayList<>(base);
        drops.add(IC2VariantStacks.create("misc_resource/resin"));
        return drops;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        Resin resin = state.getValue(RESIN);
        BlockState rotated = super.rotate(state, rotation);
        return resin == Resin.PLAIN ? rotated : rotated.setValue(RESIN, Resin.forFace(rotation.rotate(resin.face()), resin.wet()));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        Resin resin = state.getValue(RESIN);
        return resin == Resin.PLAIN ? state : state.setValue(RESIN, Resin.forFace(mirror.mirror(resin.face()), resin.wet()));
    }

    public enum Resin implements StringRepresentable {
        PLAIN("plain", null, false),
        DRY_NORTH("dry_north", Direction.NORTH, false), DRY_SOUTH("dry_south", Direction.SOUTH, false),
        DRY_WEST("dry_west", Direction.WEST, false), DRY_EAST("dry_east", Direction.EAST, false),
        WET_NORTH("wet_north", Direction.NORTH, true), WET_SOUTH("wet_south", Direction.SOUTH, true),
        WET_WEST("wet_west", Direction.WEST, true), WET_EAST("wet_east", Direction.EAST, true);

        private final String name;
        private final Direction face;
        private final boolean wet;

        Resin(String name, Direction face, boolean wet) {
            this.name = name;
            this.face = face;
            this.wet = wet;
        }

        @Override public String getSerializedName() { return name; }
        public Direction face() { return face; }
        public boolean wet() { return wet; }
        public boolean isDry() { return face != null && !wet; }

        public static Resin forFace(Direction face, boolean wet) {
            for (Resin resin : values()) {
                if (resin.face == face && resin.wet == wet) return resin;
            }
            throw new IllegalArgumentException("Invalid resin face: " + face);
        }
    }
}
