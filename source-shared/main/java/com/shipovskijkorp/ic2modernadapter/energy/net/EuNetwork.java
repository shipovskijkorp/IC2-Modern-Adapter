package com.shipovskijkorp.ic2modernadapter.energy.net;

import com.shipovskijkorp.ic2modernadapter.energy.api.IEuEnergyStorage;
import com.shipovskijkorp.ic2modernadapter.energy.calc.EuEnergyCalculator;
import com.shipovskijkorp.ic2modernadapter.energy.grid.EnergyNetLocal;
import com.shipovskijkorp.ic2modernadapter.energy.grid.NodeStats;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/** Public, loader-neutral facade for IC2MA's native EU network. */
public final class EuNetwork {
    private EuNetwork() {
    }

    /** @return amount spent by the source in EU. */
    public static long route(
            Level level, BlockPos sourcePos, IEuEnergyStorage source, Direction outSide, long maxAmount) {
        return EuEnergyCalculator.route(level, sourcePos, source, outSide, maxAmount);
    }

    public static void invalidate(Level level) {
        if (level != null) {
            EnergyNetLocal.get(level).invalidateAll();
        }
    }

    public static void invalidate(Level level, BlockPos pos) {
        if (level != null) {
            EnergyNetLocal.get(level).invalidateAt(pos);
        }
    }

    public static NodeStats getNodeStats(Level level, BlockPos cablePos) {
        return level == null ? NodeStats.ZERO : EnergyNetLocal.get(level).getNodeStats(cablePos);
    }

    /** Loader glue calls this once at the end of each server-level tick. */
    public static void onLevelTickEnd(Level level) {
        if (level != null && !level.isClientSide()) {
            EnergyNetLocal.get(level).onLevelTickEnd(level);
        }
    }
}
