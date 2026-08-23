package com.shipovskijkorp.ic2modernadapter.energy.grid;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Per-level IC2 EU network state.
 *
 * <p>The topology/path cache is intentionally tick-scoped. That gives all four loader targets the
 * same behavior without relying on loader-specific block-change events, while still avoiding
 * repeated Dijkstra walks when one source emits several packets in the same tick.</p>
 */
public final class EnergyNetLocal {
    private static final Map<Level, EnergyNetLocal> INSTANCES = new WeakHashMap<>();

    public static EnergyNetLocal get(Level level) {
        if (level == null) {
            throw new IllegalArgumentException("level");
        }
        synchronized (INSTANCES) {
            return INSTANCES.computeIfAbsent(level, ignored -> new EnergyNetLocal());
        }
    }

    private final Map<Long, List<RoutePath>> routesByStartAndSource = new HashMap<>();
    private final NodeStatsTracker statsTracker = new NodeStatsTracker();
    private final Set<RoutePath> touchedPaths = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Long, Double> pendingSinkExplosions = new HashMap<>();
    private long cacheTick = Long.MIN_VALUE;

    private EnergyNetLocal() {
    }

    public List<RoutePath> getOrComputeRoutes(Level level, BlockPos sourcePos, BlockPos startCablePos) {
        ensureCurrentTick(level);
        long key = mixStartSource(startCablePos.asLong(), sourcePos == null ? 0L : sourcePos.asLong());
        return routesByStartAndSource.computeIfAbsent(
                key, ignored -> EnergyGridPathFinder.findRoutes(level, sourcePos, startCablePos));
    }

    public void recordPathTransfer(Level level, RoutePath path, double supplied, double packetConducted) {
        if (path == null) {
            return;
        }
        ensureCurrentTick(level);
        long tick = level.getGameTime();
        path.record(tick, supplied, packetConducted);
        touchedPaths.add(path);
        for (BlockPos cable : path.cables()) {
            statsTracker.recordConduction(cable.asLong(), supplied, packetConducted);
        }
    }

    public void scheduleSinkExplosion(BlockPos sinkPos, double packet) {
        if (sinkPos == null || packet <= 0.0) {
            return;
        }
        pendingSinkExplosions.merge(sinkPos.asLong(), packet, Math::max);
    }

    public NodeStats getNodeStats(BlockPos cablePos) {
        return cablePos == null ? NodeStats.ZERO : statsTracker.getPrevious(cablePos.asLong());
    }

    public void invalidateAll() {
        routesByStartAndSource.clear();
    }

    public void invalidateAt(BlockPos ignored) {
        // Tick-scoped routes are cheap enough that a topology change simply drops the whole level cache.
        invalidateAll();
    }

    /** Called from the tiny loader-specific end-of-level-tick hook. */
    public void onLevelTickEnd(Level level) {
        if (level == null || level.isClientSide()) {
            return;
        }

        long tick = level.getGameTime();
        if (!touchedPaths.isEmpty()) {
            IdentityHashMap<LivingEntity, Double> shockEnergy = new IdentityHashMap<>();
            for (RoutePath path : touchedPaths) {
                double packet = path.maxPacketConducted(tick);
                if (packet > 0.0 && packet > path.minEffectEnergy) {
                    OverVoltageProcessor.applyCableEffects(level, path.cables(), packet, shockEnergy);
                }
            }
            OverVoltageProcessor.applyAccumulatedShockDamage(level, shockEnergy);
            touchedPaths.clear();
        }

        if (!pendingSinkExplosions.isEmpty()) {
            pendingSinkExplosions.forEach((pos, packet) ->
                    OverVoltageProcessor.explodeSink(level, BlockPos.of(pos), packet));
            pendingSinkExplosions.clear();
        }

        statsTracker.endTick();
        routesByStartAndSource.clear();
        cacheTick = tick + 1L;
    }

    private void ensureCurrentTick(Level level) {
        long tick = level.getGameTime();
        if (cacheTick != tick) {
            routesByStartAndSource.clear();
            cacheTick = tick;
        }
    }

    private static long mixStartSource(long startCable, long source) {
        return startCable * 31L ^ source;
    }
}
