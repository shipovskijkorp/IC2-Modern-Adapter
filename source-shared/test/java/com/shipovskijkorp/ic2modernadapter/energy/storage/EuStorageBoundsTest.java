package com.shipovskijkorp.ic2modernadapter.energy.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EuStorageBoundsTest {
    @Test
    void clampsStoredEnergyToThePhysicalCapacity() {
        assertEquals(0L, EuStorageBounds.clamp(-1L, 40_000L));
        assertEquals(39_999L, EuStorageBounds.clamp(39_999L, 40_000L));
        assertEquals(40_000L, EuStorageBounds.clamp(40_000L, 40_000L));
        assertEquals(40_000L, EuStorageBounds.clamp(40_720L, 40_000L));
    }

    @Test
    void anAlreadyOverfilledBufferHasNoFreeSpace() {
        assertEquals(0L, EuStorageBounds.free(40_720L, 40_000L));
    }

    @Test
    void acceptedEnergyNeverExceedsRemainingCapacity() {
        assertEquals(80L, EuStorageBounds.accept(39_920L, 40_000L, 800L));
        assertEquals(0L, EuStorageBounds.accept(40_000L, 40_000L, 800L));
        assertEquals(1_000L, EuStorageBounds.accept(20_000L, 40_000L, 1_000L));
    }
}
