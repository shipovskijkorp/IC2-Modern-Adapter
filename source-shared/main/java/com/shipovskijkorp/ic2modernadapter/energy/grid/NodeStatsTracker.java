package com.shipovskijkorp.ic2modernadapter.energy.grid;

import com.shipovskijkorp.ic2modernadapter.energy.util.EuUtil;
import java.util.HashMap;
import java.util.Map;

final class NodeStatsTracker {
    private static final class Mutable {
        double energyIn;
        double energyOut;
        double maxPacket;

        void add(double supplied, double packet) {
            energyIn += supplied;
            energyOut += supplied;
            maxPacket = Math.max(maxPacket, packet);
        }
    }

    private final Map<Long, Mutable> current = new HashMap<>();
    private final Map<Long, NodeStats> previous = new HashMap<>();

    void recordConduction(long cablePos, double supplied, double packetConducted) {
        if (supplied <= 0.0 && packetConducted <= 0.0) {
            return;
        }
        Mutable mutable = current.computeIfAbsent(cablePos, ignored -> new Mutable());
        if (supplied > 0.0) {
            mutable.add(supplied, packetConducted);
        } else {
            mutable.maxPacket = Math.max(mutable.maxPacket, packetConducted);
        }
    }

    NodeStats getPrevious(long cablePos) {
        return previous.getOrDefault(cablePos, NodeStats.ZERO);
    }

    void endTick() {
        previous.clear();
        current.forEach((pos, mutable) -> previous.put(pos,
                new NodeStats(mutable.energyIn, mutable.energyOut, EuUtil.tierFromPower(mutable.maxPacket))));
        current.clear();
    }
}
