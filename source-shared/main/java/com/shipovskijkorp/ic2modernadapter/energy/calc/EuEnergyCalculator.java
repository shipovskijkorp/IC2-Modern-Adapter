package com.shipovskijkorp.ic2modernadapter.energy.calc;

import com.shipovskijkorp.ic2modernadapter.energy.api.IEuEnergyStorage;
import com.shipovskijkorp.ic2modernadapter.energy.cable.EuCableVariant;
import com.shipovskijkorp.ic2modernadapter.energy.grid.EnergyNetLocal;
import com.shipovskijkorp.ic2modernadapter.energy.grid.RoutePath;
import com.shipovskijkorp.ic2modernadapter.energy.util.EuUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** IC2 packet router with per-cable loss, sink demand, tier limits and over-voltage handling. */
public final class EuEnergyCalculator {
    private EuEnergyCalculator() {
    }

    /** @return EU actually drawn from the source. */
    public static long route(
            Level level, BlockPos sourcePos, IEuEnergyStorage source, Direction outSide, long maxAmount) {
        if (level == null || level.isClientSide() || source == null || sourcePos == null || outSide == null) {
            return 0L;
        }
        if (maxAmount <= 0L || !source.canExtract(outSide)) {
            return 0L;
        }

        BlockPos firstPos = sourcePos.relative(outSide);
        long maxPacket = EuUtil.powerFromTier(source.getSourceTier(outSide));
        EnergyNetLocal net = EnergyNetLocal.get(level);

        BlockEntity directEntity = level.getBlockEntity(firstPos);
        if (directEntity instanceof IEuEnergyStorage directSink) {
            Direction intoSink = outSide.getOpposite();
            return moveEnergyDirect(level, net, firstPos, source, outSide, directSink, intoSink, maxAmount, 0.0);
        }

        EuCableVariant firstCable = EuCableVariant.fromBlockState(level.getBlockState(firstPos));
        if (firstCable == null) {
            return 0L;
        }

        long extractable = source.extractEu(maxAmount, outSide, true);
        if (extractable <= 0L) {
            return 0L;
        }
        if (source.isFullEnergyOutput() && extractable < maxPacket) {
            return 0L;
        }

        List<RoutePath> paths = net.getOrComputeRoutes(level, sourcePos, firstPos);
        if (paths.isEmpty()) {
            return 0L;
        }

        int pathCount = paths.size();
        int startIndex = ((level.getGameTime() & 3L) != 0L && pathCount > 1)
                ? level.random.nextInt(pathCount)
                : 0;
        long spentTotal = 0L;
        int maxPackets = source.sendMultipleEnergyPackets()
                ? Math.max(1, source.getMaxEnergyPacketCount())
                : 1;

        for (int packetIndex = 0; packetIndex < maxPackets; packetIndex++) {
            long remainingTotal = extractable - spentTotal;
            if (remainingTotal <= 0L) {
                break;
            }
            long packetBudget = Math.min(remainingTotal, maxPacket);
            if (source.isFullEnergyOutput() && packetBudget < maxPacket) {
                break;
            }
            if (source.isFullEnergyOutput()) {
                packetBudget = maxPacket;
            }

            long remainingPacketBudget = packetBudget;
            for (int i = 0; i < pathCount && remainingPacketBudget > 0L; i++) {
                RoutePath path = paths.get((startIndex + i) % pathCount);
                BlockEntity sinkEntity = level.getBlockEntity(path.sinkPos());
                if (!(sinkEntity instanceof IEuEnergyStorage sink)) {
                    continue;
                }
                Direction intoSink = path.intoSink();
                if (!sink.canInsert(intoSink)) {
                    continue;
                }

                long spent = moveEnergy(
                        level,
                        net,
                        path,
                        source,
                        outSide,
                        sink,
                        intoSink,
                        remainingPacketBudget,
                        Math.floor(path.loss()));
                if (spent <= 0L) {
                    continue;
                }
                spentTotal += spent;
                remainingPacketBudget -= spent;
                if (spentTotal >= extractable) {
                    break;
                }
            }
        }

        return spentTotal;
    }

    private static long moveEnergy(
            Level level,
            EnergyNetLocal net,
            RoutePath path,
            IEuEnergyStorage source,
            Direction outSide,
            IEuEnergyStorage sink,
            Direction intoSink,
            long budget,
            double loss) {
        if (budget <= 0L || !source.canExtract(outSide) || !sink.canInsert(intoSink)) {
            return 0L;
        }

        long deliveredMax = (long) Math.floor(Math.max(0.0, budget - loss));
        long demanded = (long) Math.floor(Math.max(0.0, sink.getDemandedEnergy(intoSink)));
        long offer = Math.min(deliveredMax, demanded);
        if (offer <= 0L) {
            return 0L;
        }

        boolean overVoltage = offer > EuUtil.powerFromTier(sink.getSinkTier(intoSink));
        long acceptedSimulated = sink.insertEu(offer, intoSink, true);
        if (acceptedSimulated <= 0L) {
            return 0L;
        }

        long wantedSpend = Math.min(budget, (long) Math.ceil(acceptedSimulated + loss));
        long extracted = source.extractEu(wantedSpend, outSide, false);
        if (extracted <= 0L) {
            return 0L;
        }

        long deliveredActual = (long) Math.floor(Math.max(0.0, extracted - loss));
        deliveredActual = Math.min(deliveredActual, acceptedSimulated);
        long inserted = deliveredActual > 0L ? sink.insertEu(deliveredActual, intoSink, false) : 0L;

        double effectivePacket = Math.min((double) extracted, Math.max(0.0, inserted + loss));
        net.recordPathTransfer(level, path, inserted, effectivePacket);
        if (overVoltage) {
            net.scheduleSinkExplosion(path.sinkPos(), offer);
        }
        return extracted;
    }

    private static long moveEnergyDirect(
            Level level,
            EnergyNetLocal net,
            BlockPos sinkPos,
            IEuEnergyStorage source,
            Direction outSide,
            IEuEnergyStorage sink,
            Direction intoSink,
            long budget,
            double loss) {
        if (budget <= 0L || !source.canExtract(outSide) || !sink.canInsert(intoSink)) {
            return 0L;
        }

        long deliveredMax = (long) Math.floor(Math.max(0.0, budget - loss));
        long demanded = (long) Math.floor(Math.max(0.0, sink.getDemandedEnergy(intoSink)));
        long offer = Math.min(deliveredMax, demanded);
        if (offer <= 0L) {
            return 0L;
        }

        boolean overVoltage = offer > EuUtil.powerFromTier(sink.getSinkTier(intoSink));
        long acceptedSimulated = sink.insertEu(offer, intoSink, true);
        if (acceptedSimulated <= 0L) {
            return 0L;
        }

        long wantedSpend = Math.min(budget, (long) Math.ceil(acceptedSimulated + loss));
        long extracted = source.extractEu(wantedSpend, outSide, false);
        if (extracted <= 0L) {
            return 0L;
        }

        long deliveredActual = Math.min(
                acceptedSimulated, (long) Math.floor(Math.max(0.0, extracted - loss)));
        if (deliveredActual > 0L) {
            sink.insertEu(deliveredActual, intoSink, false);
        }
        if (overVoltage) {
            net.scheduleSinkExplosion(sinkPos, offer);
        }
        return extracted;
    }
}
