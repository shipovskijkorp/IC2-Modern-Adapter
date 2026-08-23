package com.shipovskijkorp.ic2modernadapter.energy.storage;

/** Pure capacity-bound helpers for EU buffers. */
public final class EuStorageBounds {
    private EuStorageBounds() {
    }

    public static long clamp(long stored, long capacity) {
        long boundedCapacity = Math.max(0L, capacity);
        return Math.max(0L, Math.min(boundedCapacity, stored));
    }

    public static long free(long stored, long capacity) {
        long boundedCapacity = Math.max(0L, capacity);
        return boundedCapacity - clamp(stored, boundedCapacity);
    }

    public static long accept(long stored, long capacity, long offered) {
        if (offered <= 0L) {
            return 0L;
        }
        return Math.min(offered, free(stored, capacity));
    }
}
