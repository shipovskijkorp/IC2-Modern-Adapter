package com.shipovskijkorp.ic2modernadapter.energy.grid;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Cached best-loss route from a starting cable to an EU sink. */
public final class RoutePath {
    private final BlockPos sinkPos;
    private final Direction intoSink;
    private final double loss;
    private final List<BlockPos> cables;
    final double minEffectEnergy;

    private long lastTick = Long.MIN_VALUE;
    private double energySupplied;
    private double maxPacketConducted;

    RoutePath(
            BlockPos sinkPos,
            Direction intoSink,
            double loss,
            List<BlockPos> cables,
            double minConductorBreakdownEnergy,
            double minInsulationBreakdownEnergy,
            double minInsulationEnergyAbsorption) {
        this.sinkPos = sinkPos.immutable();
        this.intoSink = intoSink;
        this.loss = loss;
        this.cables = List.copyOf(cables);
        this.minEffectEnergy = Math.min(
                minConductorBreakdownEnergy,
                Math.min(minInsulationBreakdownEnergy, minInsulationEnergyAbsorption));
    }

    public BlockPos sinkPos() {
        return sinkPos;
    }

    public Direction intoSink() {
        return intoSink;
    }

    public double loss() {
        return loss;
    }

    public List<BlockPos> cables() {
        return cables;
    }

    public double energySupplied(long tick) {
        return lastTick == tick ? energySupplied : 0.0;
    }

    public double maxPacketConducted(long tick) {
        return lastTick == tick ? maxPacketConducted : 0.0;
    }

    void record(long tick, double supplied, double packetConducted) {
        if (lastTick != tick) {
            lastTick = tick;
            energySupplied = 0.0;
            maxPacketConducted = 0.0;
        }
        if (supplied > 0.0) {
            energySupplied += supplied;
        }
        maxPacketConducted = Math.max(maxPacketConducted, packetConducted);
    }
}
