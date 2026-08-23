package com.shipovskijkorp.ic2modernadapter.energy.cable;

import com.shipovskijkorp.ic2modernadapter.energy.api.IEuEnergyStorage;
import com.shipovskijkorp.ic2modernadapter.energy.net.EuNetwork;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Native IC2 cable block used by the standalone {@code ic2:cable} item.
 *
 * <p>IndustrialCraft 2 1.12 stored placed cables as tile-entity variants of the generic
 * {@code ic2:te} block. Modern Minecraft requires a stable block-state surface for thin cable
 * geometry, so IC2MA exposes an internal {@code ic2:cable} block while preserving the original
 * public item id and all fourteen item/NBT identities. The block is not added to the canonical
 * content manifest or creative tab as a new content entry.</p>
 *
 * <p>All gameplay/network rules are loader-neutral. Forge/NeoForge/Fabric only provide the small
 * block-entity registration/persistence bridge.</p>
 */
public class CableBlock extends Block implements EntityBlock {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, EuCableVariant.values().length - 1);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");

    private static final Map<Direction, BooleanProperty> CONNECTION_PROPERTY = new EnumMap<>(Direction.class);
    private static final Map<Integer, VoxelShape> SHAPE_CACHE = new ConcurrentHashMap<>();

    static {
        CONNECTION_PROPERTY.put(Direction.DOWN, DOWN);
        CONNECTION_PROPERTY.put(Direction.UP, UP);
        CONNECTION_PROPERTY.put(Direction.NORTH, NORTH);
        CONNECTION_PROPERTY.put(Direction.SOUTH, SOUTH);
        CONNECTION_PROPERTY.put(Direction.WEST, WEST);
        CONNECTION_PROPERTY.put(Direction.EAST, EAST);
    }

    @FunctionalInterface
    public interface CableEntityFactory {
        BlockEntity create(EuCableVariant variant, BlockPos pos, BlockState state);
    }

    private final Function<ItemStack, String> itemVariantResolver;
    private final Function<String, ItemStack> variantStackFactory;
    private final CableEntityFactory cableEntityFactory;

    public CableBlock(
            Properties properties,
            Function<ItemStack, String> itemVariantResolver,
            Function<String, ItemStack> variantStackFactory,
            CableEntityFactory cableEntityFactory) {
        super(properties);
        this.itemVariantResolver = itemVariantResolver;
        this.variantStackFactory = variantStackFactory;
        this.cableEntityFactory = cableEntityFactory;

        BlockState state = stateDefinition.any()
                .setValue(VARIANT, 0)
                .setValue(ACTIVE, false);
        for (BooleanProperty property : CONNECTION_PROPERTY.values()) {
            state = state.setValue(property, false);
        }
        registerDefaultState(state);
    }

    public static BooleanProperty connectionProperty(Direction direction) {
        return CONNECTION_PROPERTY.get(direction);
    }

    public static EuCableVariant variant(BlockState state) {
        if (state == null || !state.hasProperty(VARIANT)) {
            return null;
        }
        return EuCableVariant.fromStateVariant(state.getValue(VARIANT));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        EuCableVariant cable = EuCableVariant.fromVariantKey(itemVariantResolver.apply(context.getItemInHand()));
        if (cable == null) {
            cable = EuCableVariant.COPPER_0;
        }

        BlockState state = defaultBlockState()
                .setValue(VARIANT, cable.stateVariantIndex())
                .setValue(ACTIVE, initialActive(context.getLevel(), context.getClickedPos(), cable));
        return updateConnections(state, context.getLevel(), context.getClickedPos());
    }

    private static boolean initialActive(Level level, BlockPos pos, EuCableVariant cable) {
        if (cable.isSplitter()) {
            return !level.hasNeighborSignal(pos);
        }
        return false;
    }

    /** True when the cable currently participates in the EU graph. */
    public static boolean isConnectionOpen(BlockGetter level, BlockPos pos, BlockState state) {
        EuCableVariant cable = variant(state);
        if (cable == null || !cable.isSplitter()) {
            return cable != null;
        }
        if (level instanceof Level actualLevel && !actualLevel.isClientSide()) {
            return !actualLevel.hasNeighborSignal(pos);
        }
        return state.hasProperty(ACTIVE) && state.getValue(ACTIVE);
    }

    /** Cable-to-cable connection rule shared by rendering, shape calculation and path finding. */
    public static boolean canCablesInteract(BlockGetter level, BlockPos pos, Direction direction) {
        BlockState self = level.getBlockState(pos);
        BlockPos otherPos = pos.relative(direction);
        BlockState other = level.getBlockState(otherPos);
        if (!(self.getBlock() instanceof CableBlock) || !(other.getBlock() instanceof CableBlock)) {
            return false;
        }
        return isConnectionOpen(level, pos, self) && isConnectionOpen(level, otherPos, other);
    }

    /** Whether this cable should visually/electrically attach to the block on {@code direction}. */
    public static boolean connectsTo(BlockGetter level, BlockPos pos, BlockState self, Direction direction) {
        if (!(self.getBlock() instanceof CableBlock) || !isConnectionOpen(level, pos, self)) {
            return false;
        }

        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof CableBlock) {
            return canCablesInteract(level, pos, direction);
        }

        BlockEntity blockEntity = level.getBlockEntity(neighborPos);
        if (blockEntity instanceof IEuEnergyStorage storage) {
            Direction face = direction.getOpposite();
            return storage.canInsert(face) || storage.canExtract(face);
        }
        return false;
    }

    public static BlockState updateConnections(BlockState state, BlockGetter level, BlockPos pos) {
        if (!(state.getBlock() instanceof CableBlock)) {
            return state;
        }
        BlockState result = state;
        for (Direction direction : Direction.values()) {
            result = result.setValue(connectionProperty(direction), connectsTo(level, pos, state, direction));
        }
        return result;
    }

    /** Server-side splitter state follows the original rule: powered = unloaded/disconnected. */
    public static BlockState updateSplitterState(BlockState state, Level level, BlockPos pos) {
        EuCableVariant cable = variant(state);
        if (cable == null || !cable.isSplitter()) {
            return state;
        }
        return state.setValue(ACTIVE, !level.hasNeighborSignal(pos));
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            BlockPos neighborPos,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.isClientSide()) {
            return;
        }

        BlockState updated = updateSplitterState(state, level, pos);
        updated = updateConnections(updated, level, pos);
        if (!updated.equals(state)) {
            boolean activeChanged = updated.getValue(ACTIVE) != state.getValue(ACTIVE);
            level.setBlock(pos, updated, activeChanged ? Block.UPDATE_ALL : Block.UPDATE_CLIENTS);
        }
        invalidateAround(level, pos);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide() || oldState.is(this)) {
            return;
        }
        BlockState updated = updateConnections(updateSplitterState(state, level, pos), level, pos);
        if (!updated.equals(state)) {
            level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
        }
        invalidateAround(level, pos);
        refreshAdjacentCableStates(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        boolean removed = !newState.is(this);
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (level.isClientSide() || !removed) {
            return;
        }
        invalidateAround(level, pos);
        refreshAdjacentCableStates(level, pos);
    }

    private static void invalidateAround(Level level, BlockPos pos) {
        EuNetwork.invalidate(level, pos);
        for (Direction direction : Direction.values()) {
            EuNetwork.invalidate(level, pos.relative(direction));
        }
    }

    /** Cable-side half of the original cutter + rubber insulation interaction. */
    public static boolean tryAddInsulation(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        EuCableVariant cable = variant(state);
        if (cable == null || cable.insulation() >= cable.maxInsulation()) {
            return false;
        }
        return replaceVariant(level, pos, state, cable.withOneInsulationLayer());
    }

    /** Cable-side half of the original cutter insulation-removal interaction. */
    public static boolean tryRemoveInsulation(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        EuCableVariant cable = variant(state);
        if (cable == null || cable.insulation() <= 0) {
            return false;
        }
        return replaceVariant(level, pos, state, cable.withoutOneInsulationLayer());
    }

    private static boolean replaceVariant(
            Level level, BlockPos pos, BlockState state, EuCableVariant replacement) {
        BlockState updated = state.setValue(VARIANT, replacement.stateVariantIndex());
        updated = updateConnections(updated, level, pos);
        boolean changed = level.setBlock(pos, updated, Block.UPDATE_ALL);
        if (changed) {
            invalidateAround(level, pos);
            refreshAdjacentCableStates(level, pos);
        }
        return changed;
    }

    private static void refreshAdjacentCableStates(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (!(neighbor.getBlock() instanceof CableBlock)) {
                continue;
            }
            BlockState updated = updateConnections(updateSplitterState(neighbor, level, neighborPos), level, neighborPos);
            if (!updated.equals(neighbor)) {
                level.setBlock(neighborPos, updated, Block.UPDATE_CLIENTS);
            }
        }
    }

    /** Called by detector/splitter block entities after their ACTIVE state changes. */
    public static void refreshAfterActiveChange(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CableBlock)) {
            return;
        }
        BlockState updated = updateConnections(state, level, pos);
        if (!updated.equals(state)) {
            level.setBlock(pos, updated, Block.UPDATE_ALL);
        } else {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            level.updateNeighborsAt(pos, state.getBlock());
        }
        refreshAdjacentCableStates(level, pos);
        invalidateAround(level, pos);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return cachedShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return cachedShape(state);
    }

    private static VoxelShape cachedShape(BlockState state) {
        EuCableVariant cable = variant(state);
        if (cable == null) {
            return Shapes.block();
        }
        int mask = connectionMask(state);
        int key = cable.stateVariantIndex() << 6 | mask;
        return SHAPE_CACHE.computeIfAbsent(key, ignored -> buildShape(cable.visualWidth(), mask));
    }

    public static int connectionMask(BlockState state) {
        int mask = 0;
        for (Direction direction : Direction.values()) {
            BooleanProperty property = connectionProperty(direction);
            if (state.hasProperty(property) && state.getValue(property)) {
                mask |= 1 << direction.ordinal();
            }
        }
        return mask;
    }

    private static VoxelShape buildShape(float width, int mask) {
        double min = 8.0D - width * 8.0D;
        double max = 8.0D + width * 8.0D;
        VoxelShape shape = Block.box(min, min, min, max, max, max);
        for (Direction direction : Direction.values()) {
            if ((mask & (1 << direction.ordinal())) == 0) {
                continue;
            }
            VoxelShape arm = switch (direction) {
                case DOWN -> Block.box(min, 0.0D, min, max, min, max);
                case UP -> Block.box(min, max, min, max, 16.0D, max);
                case NORTH -> Block.box(min, min, 0.0D, max, max, min);
                case SOUTH -> Block.box(min, min, max, max, max, 16.0D);
                case WEST -> Block.box(0.0D, min, min, min, max, max);
                case EAST -> Block.box(max, min, min, 16.0D, max, max);
            };
            shape = Shapes.or(shape, arm);
        }
        return shape;
    }

    protected final ItemStack variantStack(BlockState state) {
        EuCableVariant cable = variant(state);
        if (cable == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = variantStackFactory.apply(cable.variantKey());
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack stack = variantStack(state);
        return stack.isEmpty() ? List.of() : List.of(stack);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        EuCableVariant cable = variant(state);
        return cable == null ? null : cableEntityFactory.create(cable, pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof AbstractCableBlockEntity cable) {
                cable.serverTick();
            }
        };
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        EuCableVariant cable = variant(state);
        return cable != null && cable.isDetector();
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        EuCableVariant cable = variant(state);
        return cable != null && cable.isDetector() && state.getValue(ACTIVE) ? 15 : 0;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        EuCableVariant cable = variant(state);
        return cable != null && cable.isDetector();
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof AbstractCableBlockEntity cable ? cable.getComparatorLevel() : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT, ACTIVE, DOWN, UP, NORTH, SOUTH, WEST, EAST);
    }
}
