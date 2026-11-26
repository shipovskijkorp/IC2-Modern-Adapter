package com.shipovskijkorp.ic2modernadapter.energy.item;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EuChargeMathTest {
    @Test void everyRegisteredSpecHonorsCapacityTierAndTransferLimit() {
        assertEquals(36, EuElectricItemSpec.values().length);
        for (EuElectricItemSpec spec : EuElectricItemSpec.values()) {
            long max = spec.capacityEu(), limit = spec.transferLimitEu();
            assertSame(spec, EuElectricItemSpec.fromItemPath(spec.itemPath()));
            assertEquals(0, EuChargeMath.budget(Long.MAX_VALUE, limit, spec.tier(), spec.tier() - 1, true));
            assertEquals(limit, EuChargeMath.budget(Long.MAX_VALUE, limit, spec.tier(), spec.tier(), false));
            assertEquals(Long.MAX_VALUE, EuChargeMath.budget(Long.MAX_VALUE, limit, spec.tier(), spec.tier(), true));
            assertEquals(max, EuChargeMath.charge(0, max, Long.MAX_VALUE));
            assertEquals(0, EuChargeMath.charge(max, max, Long.MAX_VALUE));
            assertEquals(1, EuChargeMath.charge(max - 1, max, Long.MAX_VALUE));
            assertEquals(max, EuChargeMath.discharge(max, max, Long.MAX_VALUE));
            assertEquals(0, EuChargeMath.discharge(0, max, Long.MAX_VALUE));
            assertEquals(0, EuChargeMath.modelLevel(0, max));
            assertEquals(4, EuChargeMath.modelLevel(max, max));
            assertEquals(0, EuChargeMath.charge(0, max, -1));
            assertEquals(0, EuChargeMath.discharge(max, max, -1));
        }
    }
    @Test void chargingBatteryVisualLevelsAreMonotonic() {
        int previous = 0;
        for (int charge = 0; charge <= 10000; charge++) {
            int level = EuChargeMath.modelLevel(charge, 10000);
            assertTrue(level >= previous && level <= 4);
            previous = level;
        }
    }
}
