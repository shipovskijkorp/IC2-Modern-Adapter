package com.shipovskijkorp.ic2modernadapter.energy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shipovskijkorp.ic2modernadapter.energy.util.EuUtil;
import org.junit.jupiter.api.Test;

class EuUtilTest {
    @Test
    void preservesIc2PacketTierTable() {
        assertEquals(8L, EuUtil.powerFromTier(0));
        assertEquals(32L, EuUtil.powerFromTier(1));
        assertEquals(128L, EuUtil.powerFromTier(2));
        assertEquals(512L, EuUtil.powerFromTier(3));
        assertEquals(2048L, EuUtil.powerFromTier(4));
        assertEquals(8192L, EuUtil.powerFromTier(5));
    }

    @Test
    void mapsPacketSizesBackToIc2Tiers() {
        assertEquals(0, EuUtil.tierFromPower(8));
        assertEquals(1, EuUtil.tierFromPower(32));
        assertEquals(2, EuUtil.tierFromPower(128));
        assertEquals(3, EuUtil.tierFromPower(512));
        assertEquals(5, EuUtil.tierFromPower(8192));
    }
}
