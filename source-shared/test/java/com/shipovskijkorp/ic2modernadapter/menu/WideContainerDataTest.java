package com.shipovskijkorp.ic2modernadapter.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Random;
import net.minecraft.world.inventory.SimpleContainerData;
import org.junit.jupiter.api.Test;

class WideContainerDataTest {
    @Test void roundTripsFullIntegersOverSignedShortPackets() {
        var source = new SimpleContainerData(3);
        var target = new SimpleContainerData(3);
        var wireSource = new WideContainerData(source);
        var wireTarget = new WideContainerData(target);
        assertEquals(6, wireSource.getCount());
        Random random = new Random(222);
        int[] edge = {0, 32767, 32768, 40000, 65535, 65536, 300000, 4000000, 40000000, -1, Integer.MAX_VALUE, Integer.MIN_VALUE};
        for (int i = 0; i < 10000; i++) {
            for (int j = 0; j < 3; j++) source.set(j, i < edge.length ? edge[i] : random.nextInt());
            for (int j = 0; j < wireSource.getCount(); j++) wireTarget.set(j, (short) wireSource.get(j));
            for (int j = 0; j < 3; j++) assertEquals(source.get(j), target.get(j));
        }
    }
}
