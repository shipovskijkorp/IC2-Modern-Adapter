package com.shipovskijkorp.ic2modernadapter.energy.cable;

import com.shipovskijkorp.ic2modernadapter.energy.grid.NodeStats;
import com.shipovskijkorp.ic2modernadapter.energy.net.EuNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Shared detector/splitter state for placed IC2 cables. */
public abstract class AbstractCableBlockEntity extends BlockEntity {
    private final EuCableVariant initialVariant;
    private int ticker;
    private int comparatorLevel;
    private boolean topologyInitialized;

    protected AbstractCableBlockEntity(
            BlockEntityType<?> type,
            EuCableVariant variant,
            BlockPos pos,
            BlockState state) {
        super(type, pos, state);
        this.initialVariant = variant;
    }

    public final EuCableVariant variant() {
        EuCableVariant current = CableBlock.variant(getBlockState());
        return current == null ? initialVariant : current;
    }

    public final int getComparatorLevel() {
        return comparatorLevel;
    }

    public final void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState state = getBlockState();
        EuCableVariant current = CableBlock.variant(state);
        if (current == null) {
            return;
        }

        // Block-state connection flags are persisted, but neighbours may have changed while this
        // chunk was unloaded. Rebuild the visual/topology projection once after every BE load.
        if (!topologyInitialized) {
            topologyInitialized = true;
            BlockState refreshed = CableBlock.updateConnections(
                    CableBlock.updateSplitterState(state, level, worldPosition), level, worldPosition);
            if (!refreshed.equals(state)) {
                level.setBlock(worldPosition, refreshed, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                state = refreshed;
                current = CableBlock.variant(state);
            }
            EuNetwork.invalidate(level, worldPosition);
        }

        if (current.isSplitter()) {
            boolean active = !level.hasNeighborSignal(worldPosition);
            if (state.getValue(CableBlock.ACTIVE) != active) {
                BlockState updated = CableBlock.updateConnections(
                        state.setValue(CableBlock.ACTIVE, active), level, worldPosition);
                level.setBlock(worldPosition, updated, net.minecraft.world.level.block.Block.UPDATE_ALL);
                CableBlock.refreshAfterActiveChange(level, worldPosition);
                setChanged();
            }
            return;
        }

        if (!current.isDetector() || ++ticker % 32 != 0) {
            return;
        }

        NodeStats stats = EuNetwork.getNodeStats(level, worldPosition);
        double energy = Math.max(0.0D, stats.energyIn());
        boolean active = energy > 0.0D;
        int nextComparator = (int) Math.floor(Math.min(1.0D, energy / Math.max(1.0D, current.capacity())) * 15.0D);
        nextComparator = Math.max(0, Math.min(15, nextComparator));

        boolean activeChanged = state.getValue(CableBlock.ACTIVE) != active;
        boolean comparatorChanged = comparatorLevel != nextComparator;
        if (!activeChanged && !comparatorChanged) {
            return;
        }

        comparatorLevel = nextComparator;
        if (activeChanged) {
            BlockState updated = state.setValue(CableBlock.ACTIVE, active);
            level.setBlock(worldPosition, updated, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
        level.updateNeighborsAt(worldPosition, state.getBlock());
        level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
        setChanged();
    }

    protected final void saveCableState(CompoundTag tag) {
        tag.putInt("ticker", ticker);
        tag.putByte("comparator", (byte) comparatorLevel);
    }

    protected final void loadCableState(CompoundTag tag) {
        ticker = Math.max(0, tag.getInt("ticker"));
        comparatorLevel = Math.max(0, Math.min(15, tag.getByte("comparator") & 0xFF));
    }
}
