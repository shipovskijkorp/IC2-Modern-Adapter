package com.shipovskijkorp.ic2modernadapter.energy.api;

import com.shipovskijkorp.ic2modernadapter.energy.util.EuUtil;
import net.minecraft.core.Direction;

/**
 * Loader-neutral IC2 EU endpoint contract.
 *
 * <p>This deliberately does not expose Forge Energy, NeoForge energy capabilities, Fabric
 * Transfer API storage, or any other external power abstraction. IC2MA's energy net talks only
 * to block entities implementing this interface.</p>
 */
public interface IEuEnergyStorage {
    long getEuStored();

    long getEuCapacity();

    /** IC2 packet tier accepted by this sink. */
    int getSinkTier();

    /** IC2 packet tier emitted by this source. */
    int getSourceTier();

    default int getSinkTier(Direction side) {
        return getSinkTier();
    }

    default int getSourceTier(Direction side) {
        return getSourceTier();
    }

    /** @return amount accepted in EU. */
    long insertEu(long amount, Direction from, boolean simulate);

    /** @return amount extracted in EU. */
    long extractEu(long amount, Direction to, boolean simulate);

    boolean canInsert(Direction from);

    boolean canExtract(Direction to);

    /** Storage blocks in IC2 only emit when a complete tier-sized packet is available. */
    default boolean isFullEnergyOutput() {
        return false;
    }

    /** Generators/storage may opt into more than one packet per tick. */
    default boolean sendMultipleEnergyPackets() {
        return false;
    }

    default int getMaxEnergyPacketCount() {
        return 1;
    }

    default double getOfferedEnergy() {
        if (getEuStored() <= 0) {
            return 0.0;
        }
        double stored = getEuStored();
        if (isFullEnergyOutput()) {
            double packet = EuUtil.powerFromTierD(getSourceTier());
            return stored >= packet ? stored : 0.0;
        }
        return stored;
    }

    default double getOfferedEnergy(Direction to) {
        return canExtract(to) ? getOfferedEnergy() : 0.0;
    }

    default double getDemandedEnergy() {
        if (getEuCapacity() <= 0) {
            return 0.0;
        }
        return Math.max(0.0, (double) getEuCapacity() - (double) getEuStored());
    }

    default double getDemandedEnergy(Direction from) {
        return canInsert(from) ? getDemandedEnergy() : 0.0;
    }

    /** IC2-style injection: return the amount that was rejected by the sink. */
    default double injectEnergy(Direction from, double amount, int voltageTier, boolean simulate) {
        if (amount <= 0.0 || !canInsert(from)) {
            return Math.max(0.0, amount);
        }
        long requested = (long) Math.floor(amount);
        if (requested <= 0) {
            return amount;
        }
        long accepted = insertEu(requested, from, simulate);
        return Math.max(0.0, amount - accepted);
    }

    /** IC2-style source draw helper. */
    default double drawEnergy(double amount, boolean simulate) {
        if (amount <= 0.0) {
            return 0.0;
        }
        long requested = (long) Math.ceil(amount);
        return extractEu(requested, Direction.UP, simulate);
    }

    /** Allows charged block items to seed a freshly placed storage block. */
    default void setStoredEnergyFromItem(long amount) {
    }
}
