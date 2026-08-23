package com.shipovskijkorp.ic2modernadapter.energy.grid;

/** Previous-tick IC2 EU conduction statistics for a cable node. */
public record NodeStats(double energyIn, double energyOut, int voltageTier) {
    public static final NodeStats ZERO = new NodeStats(0.0, 0.0, 0);
}
