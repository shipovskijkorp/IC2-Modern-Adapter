package com.shipovskijkorp.ic2modernadapter.menu;

import net.minecraft.world.inventory.ContainerData;

/** Lossless int synchronization on vanilla short-valued container packets, also on loaders
 * which extend the packet to int. Both menu endpoints register the same pair of words.
 * The original block entity and client ContainerData indices remain unchanged. */
public final class WideContainerData implements ContainerData {
    private final ContainerData delegate;

    public WideContainerData(ContainerData delegate) {
        this.delegate = delegate;
    }

    @Override public int get(int index) {
        int value = delegate.get(index >>> 1);
        return (short) (value >>> ((index & 1) * 16));
    }

    @Override public void set(int index, int word) {
        int slot = index >>> 1;
        int shift = (index & 1) * 16;
        int mask = 0xffff << shift;
        delegate.set(slot, (delegate.get(slot) & ~mask) | ((word & 0xffff) << shift));
    }

    @Override public int getCount() {
        return delegate.getCount() * 2;
    }
}
