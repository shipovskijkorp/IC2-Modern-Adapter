package com.shipovskijkorp.ic2modernadapter.energy.grid;

import com.shipovskijkorp.ic2modernadapter.energy.api.IEuEnergyStorage;
import com.shipovskijkorp.ic2modernadapter.energy.cable.EuCableVariant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Dijkstra routing over IC2 cable blocks using IC2 2.8.222 loss semantics. */
final class EnergyGridPathFinder {
    private static final int MAX_NODES = 4096;
    private static final double INF = 1.0E100;
    private static final double ENDPOINT_INNER_LOSS = 0.002;

    private EnergyGridPathFinder() {
    }

    private record Node(BlockPos pos, double loss, double innerLoss) {
    }

    static boolean isCableDisabled(Level level, BlockPos pos, EuCableVariant cable) {
        return cable.isSplitter() && level.hasNeighborSignal(pos);
    }

    static List<RoutePath> findRoutes(Level level, BlockPos sourcePos, BlockPos startCablePos) {
        if (level == null || startCablePos == null) {
            return List.of();
        }

        EuCableVariant startCable = EuCableVariant.fromBlockState(level.getBlockState(startCablePos));
        if (startCable == null || isCableDisabled(level, startCablePos, startCable)) {
            return List.of();
        }

        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(Node::loss));
        Map<Long, Double> distance = new HashMap<>();
        Map<Long, Long> previous = new HashMap<>();

        double startInnerLoss = startCable.loss();
        double startLoss = (ENDPOINT_INNER_LOSS + startInnerLoss) / 2.0;
        long startKey = startCablePos.asLong();
        distance.put(startKey, startLoss);
        queue.add(new Node(startCablePos.immutable(), startLoss, startInnerLoss));

        Map<Long, RoutePath> bestPerSinkAndSide = new HashMap<>();
        int visited = 0;

        while (!queue.isEmpty() && visited++ < MAX_NODES) {
            Node current = queue.poll();
            long currentKey = current.pos.asLong();
            if (current.loss > distance.getOrDefault(currentKey, INF)) {
                continue;
            }

            EuCableVariant currentCable = EuCableVariant.fromBlockState(level.getBlockState(current.pos));
            if (currentCable == null || isCableDisabled(level, current.pos, currentCable)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos nextPos = current.pos.relative(direction);
                if (sourcePos != null && nextPos.equals(sourcePos)) {
                    continue;
                }

                BlockEntity neighborEntity = level.getBlockEntity(nextPos);
                if (neighborEntity instanceof IEuEnergyStorage) {
                    double endLinkLoss = (current.innerLoss + ENDPOINT_INNER_LOSS) / 2.0;
                    double totalLoss = current.loss + endLinkLoss;
                    Direction intoSink = direction.getOpposite();
                    List<BlockPos> cables = reconstruct(startCablePos, current.pos, previous);
                    RoutePath route = buildPath(level, nextPos, intoSink, totalLoss, cables);
                    long sinkKey = mixSinkKey(nextPos.asLong(), intoSink.ordinal());
                    RoutePath existing = bestPerSinkAndSide.get(sinkKey);
                    if (existing == null || totalLoss < existing.loss()) {
                        bestPerSinkAndSide.put(sinkKey, route);
                    }
                    continue;
                }

                BlockState nextState = level.getBlockState(nextPos);
                EuCableVariant nextCable = EuCableVariant.fromBlockState(nextState);
                if (nextCable == null || isCableDisabled(level, nextPos, nextCable)) {
                    continue;
                }

                double nextInnerLoss = nextCable.loss();
                double linkLoss = (current.innerLoss + nextInnerLoss) / 2.0;
                double nextLoss = current.loss + linkLoss;
                long nextKey = nextPos.asLong();
                if (nextLoss < distance.getOrDefault(nextKey, INF)) {
                    distance.put(nextKey, nextLoss);
                    previous.put(nextKey, currentKey);
                    queue.add(new Node(nextPos.immutable(), nextLoss, nextInnerLoss));
                }
            }
        }

        ArrayList<RoutePath> routes = new ArrayList<>(bestPerSinkAndSide.values());
        routes.sort(Comparator.comparingDouble(RoutePath::loss));
        return List.copyOf(routes);
    }

    private static RoutePath buildPath(
            Level level, BlockPos sinkPos, Direction intoSink, double loss, List<BlockPos> cables) {
        double minConductor = Double.POSITIVE_INFINITY;
        double minInsulationBreak = Double.POSITIVE_INFINITY;
        double minAbsorption = Double.POSITIVE_INFINITY;

        for (BlockPos cablePos : cables) {
            EuCableVariant cable = EuCableVariant.fromBlockState(level.getBlockState(cablePos));
            if (cable == null) {
                continue;
            }
            minConductor = Math.min(minConductor, cable.conductorBreakdownEnergy());
            minInsulationBreak = Math.min(minInsulationBreak, cable.insulationBreakdownEnergy());
            minAbsorption = Math.min(minAbsorption, cable.insulationEnergyAbsorption());
        }

        return new RoutePath(sinkPos, intoSink, loss, cables, minConductor, minInsulationBreak, minAbsorption);
    }

    private static List<BlockPos> reconstruct(BlockPos start, BlockPos end, Map<Long, Long> previous) {
        ArrayList<BlockPos> route = new ArrayList<>();
        long current = end.asLong();
        long startKey = start.asLong();
        while (true) {
            route.add(BlockPos.of(current));
            if (current == startKey) {
                break;
            }
            Long parent = previous.get(current);
            if (parent == null) {
                break;
            }
            current = parent;
        }
        Collections.reverse(route);
        return route;
    }

    private static long mixSinkKey(long pos, int side) {
        return pos * 31L ^ side;
    }
}
