package com.shipovskijkorp.ic2modernadapter.energy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.shipovskijkorp.ic2modernadapter.energy.util.OriginalEnergyFormat;
import org.junit.jupiter.api.Test;

class OriginalEnergyFormatTest {
    @Test void originalItemAndGaugePrecision() {
        assertEquals("0 /10 k EU", OriginalEnergyFormat.item(0, 10000));
        assertEquals("12.3 k/1 M EU", OriginalEnergyFormat.item(12345, 1000000));
        assertEquals("12.35 k/1 M ", OriginalEnergyFormat.gauge(12345, 1000000));
        assertEquals("32 /300 k EU", OriginalEnergyFormat.item(32, 300000));
        assertEquals("40 M/40 M EU", OriginalEnergyFormat.item(40000000, 40000000));
    }
    @Test void originalSiSuffixAndRounding() {
        assertEquals("1.23 m", OriginalEnergyFormat.toSiString(0.001234, 3));
        assertEquals("-12.35 k", OriginalEnergyFormat.toSiString(-12345, 4));
        assertEquals("\u221e ", OriginalEnergyFormat.toSiString(Double.POSITIVE_INFINITY, 3));
        assertEquals("NaN ", OriginalEnergyFormat.toSiString(Double.NaN, 3));
    }
}
