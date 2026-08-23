package com.shipovskijkorp.ic2modernadapter.energy.storage;

import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantFacingBlock;
import com.shipovskijkorp.ic2modernadapter.energy.api.IEuEnergyStorage;
import com.shipovskijkorp.ic2modernadapter.energy.item.IEuElectricItem;
import com.shipovskijkorp.ic2modernadapter.energy.net.EuNetwork;
import com.shipovskijkorp.ic2modernadapter.energy.util.EuUtil;
import com.shipovskijkorp.ic2modernadapter.menu.EuStorageMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Loader-neutral implementation shared by the IC2 Experimental electric storage family.
 *
 * <p>BatBox, CESU, MFE and MFSU all use the same mechanics: one charge slot, one discharge slot,
 * input on every side except the facing/output side, one full tier-sized EU packet per tick, and
 * the original seven redstone modes. The concrete variants only supply their canonical tier,
 * output and capacity through {@link EuStorageSpec}.</p>
 */
public abstract class AbstractEuStorageBlockEntity extends BlockEntity
        implements WorldlyContainer, IEuEnergyStorage, MenuProvider {
    public static final int SLOT_CHARGE = 0;
    public static final int SLOT_DISCHARGE = 1;
    public static final int SLOT_COUNT = 2;

    public static final int DATA_ENERGY = 0;
    public static final int DATA_CAPACITY = 1;
    public static final int DATA_OUTPUT = 2;
    public static final int DATA_REDSTONE_MODE = 3;
    public static final int DATA_TIER = 4;
    public static final int DATA_COUNT = 5;

    public static final int REDSTONE_MODE_COUNT = 7;

    private static final int[] ALL_SLOTS = {SLOT_CHARGE, SLOT_DISCHARGE};

    private final EuStorageSpec spec;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private long energy;
    private int redstoneMode;
    private int cachedRedstoneOutput = -1;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY -> (int) Math.min(Integer.MAX_VALUE, AbstractEuStorageBlockEntity.this.energy);
                case DATA_CAPACITY -> (int) Math.min(Integer.MAX_VALUE, spec.capacityEu());
                case DATA_OUTPUT -> (int) Math.min(Integer.MAX_VALUE, spec.outputEuPerTick());
                case DATA_REDSTONE_MODE -> redstoneMode;
                case DATA_TIER -> spec.tier();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_ENERGY -> AbstractEuStorageBlockEntity.this.energy = EuStorageBounds.clamp(value, spec.capacityEu());
                case DATA_REDSTONE_MODE -> redstoneMode = normalizeRedstoneMode(value);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    protected AbstractEuStorageBlockEntity(
            BlockEntityType<?> type,
            EuStorageSpec spec,
            BlockPos pos,
            BlockState state) {
        super(type, pos, state);
        this.spec = spec;
        EuStorageSpec stateSpec = EuStorageSpec.fromBlockState(state);
        if (stateSpec != null && stateSpec != spec) {
            throw new IllegalArgumentException(
                    "EU storage block entity type " + spec.blockEntityPath()
                            + " does not match block state variant " + stateSpec.variantKey());
        }
    }

    protected abstract MenuType<?> storageMenuType();

    public final EuStorageSpec spec() {
        return spec;
    }

    public final void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        long boundedEnergy = EuStorageBounds.clamp(energy, spec.capacityEu());
        boolean changed = boundedEnergy != energy;
        energy = boundedEnergy;

        changed |= processChargeSlot();
        changed |= processDischargeSlot();

        long emitted = emitEnergy();
        if (emitted > 0L) {
            changed = true;
        }

        updateRedstoneOutput();
        if (changed) {
            setChanged();
        }
    }

    private boolean processChargeSlot() {
        ItemStack stack = items.get(SLOT_CHARGE);
        if (stack.isEmpty() || energy <= 0L || !(stack.getItem() instanceof IEuElectricItem electricItem)) {
            return false;
        }
        if (!electricItem.canChargeFromTier(stack, spec.tier())) {
            return false;
        }

        long transfer = Math.min(energy, Math.max(0L, electricItem.getEuTransferLimit(stack)));
        if (transfer <= 0L) {
            return false;
        }
        long accepted = Math.max(0L, electricItem.insertEu(stack, transfer, false));
        if (accepted <= 0L) {
            return false;
        }
        energy = Math.max(0L, energy - Math.min(energy, accepted));
        return true;
    }

    private boolean processDischargeSlot() {
        if (getEuFree() <= 0L) {
            return false;
        }
        ItemStack stack = items.get(SLOT_DISCHARGE);
        if (stack.isEmpty()) {
            return false;
        }

        long free = getEuFree();
        long extracted = EuStorageItemHooks.dischargeIntoStorage(stack, free, spec.tier(), false);
        long accepted = Math.min(free, Math.max(0L, extracted));
        if (accepted <= 0L) {
            return false;
        }

        energy += accepted;
        if (stack.isEmpty()) {
            items.set(SLOT_DISCHARGE, ItemStack.EMPTY);
        }
        return true;
    }

    private long emitEnergy() {
        Level level = getLevel();
        if (level == null || !shouldEmitEnergy()) {
            return 0L;
        }

        long packet = EuUtil.powerFromTier(spec.tier());
        if (energy < packet) {
            return 0L;
        }
        Direction outputSide = getOutputSide();
        return EuNetwork.route(level, worldPosition, this, outputSide, packet);
    }

    public final Direction getOutputSide() {
        BlockState state = getBlockState();
        return state.hasProperty(LegacyVariantFacingBlock.FACING)
                ? state.getValue(LegacyVariantFacingBlock.FACING)
                : Direction.DOWN;
    }

    /** Mirrors IC2 TileEntityElectricBlock.shouldEmitEnergy(). */
    public final boolean shouldEmitEnergy() {
        Level level = getLevel();
        if (level == null) {
            return true;
        }
        boolean powered = level.hasNeighborSignal(worldPosition);
        if (redstoneMode == 5) {
            return !powered;
        }
        if (redstoneMode == 6) {
            return !powered || energy > spec.capacityEu() - spec.outputEuPerTick() * 20L;
        }
        return true;
    }

    /** Mirrors IC2 TileEntityElectricBlock.shouldEmitRedstone(). */
    public final boolean shouldEmitRedstone() {
        long capacity = spec.capacityEu();
        long output = spec.outputEuPerTick();
        return switch (redstoneMode) {
            case 1 -> energy >= capacity - output * 20L;
            case 2 -> energy > output && energy < capacity - output;
            case 3 -> energy < capacity - output;
            case 4 -> energy < output;
            default -> false;
        };
    }

    public final int getRedstoneOutputLevel() {
        return shouldEmitRedstone() ? 15 : 0;
    }

    public final int getComparatorLevel() {
        if (spec.capacityEu() <= 0L || energy <= 0L) {
            return 0;
        }
        return Math.min(15, (int) Math.floor((double) energy * 15.0D / (double) spec.capacityEu()));
    }

    private void updateRedstoneOutput() {
        Level level = getLevel();
        if (level == null) {
            return;
        }
        int next = getRedstoneOutputLevel();
        if (next == cachedRedstoneOutput) {
            return;
        }
        cachedRedstoneOutput = next;
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }

    public final int getRedstoneMode() {
        return redstoneMode;
    }

    public final void cycleRedstoneMode() {
        cycleRedstoneMode(null);
    }

    /** Cycles the original seven IC2 storage redstone modes and reports the new mode to the player. */
    public final void cycleRedstoneMode(@Nullable Player player) {
        redstoneMode = (redstoneMode + 1) % REDSTONE_MODE_COUNT;
        setChanged();
        cachedRedstoneOutput = -1;
        updateRedstoneOutput();
        if (player != null && !player.level().isClientSide()) {
            player.displayClientMessage(
                    Component.translatable("ic2.EUStorage.gui.mod.redstone" + redstoneMode),
                    false);
        }
    }

    public final ContainerData menuData() {
        return menuData;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(spec.translationKey());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new EuStorageMenu(storageMenuType(), containerId, inventory, this, menuData);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return items.get(SLOT_CHARGE).isEmpty() && items.get(SLOT_DISCHARGE).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        Level level = getLevel();
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        double dx = player.getX() - (worldPosition.getX() + 0.5D);
        double dy = player.getY() - (worldPosition.getY() + 0.5D);
        double dz = player.getZ() - (worldPosition.getZ() + 0.5D);
        return dx * dx + dy * dy + dz * dz <= 64.0D;
    }

    @Override
    public void clearContent() {
        items.set(SLOT_CHARGE, ItemStack.EMPTY);
        items.set(SLOT_DISCHARGE, ItemStack.EMPTY);
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_CHARGE -> EuStorageItemHooks.canCharge(stack, spec.tier());
            case SLOT_DISCHARGE -> EuStorageItemHooks.canDischarge(stack, spec.tier());
            default -> false;
        };
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        // IC2 InvSide values are preferences used to arbitrate ambiguous inputs, not hard sided
        // filters. Both external slots remain visible on every face.
        return ALL_SLOTS.clone();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (stack.isEmpty() || !canPlaceItem(slot, stack)) {
            return false;
        }
        if (side == null || preferredSideMatches(slot, side)) {
            return true;
        }
        for (int other : ALL_SLOTS) {
            if (other != slot && preferredSideMatches(other, side) && canPlaceItem(other, stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (slot != SLOT_CHARGE && slot != SLOT_DISCHARGE) {
            return false;
        }
        if (preferredSideMatches(slot, side)) {
            return true;
        }
        for (int other : ALL_SLOTS) {
            if (other == slot || !preferredSideMatches(other, side)) {
                continue;
            }
            if (!items.get(other).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean preferredSideMatches(int slot, Direction side) {
        return slot == SLOT_CHARGE ? side == Direction.UP : slot == SLOT_DISCHARGE && side == Direction.DOWN;
    }

    @Override
    public long getEuStored() {
        return energy;
    }

    @Override
    public long getEuCapacity() {
        return spec.capacityEu();
    }

    @Override
    public int getSinkTier() {
        return spec.tier();
    }

    @Override
    public int getSourceTier() {
        return spec.tier();
    }

    @Override
    public long insertEu(long amount, Direction from, boolean simulate) {
        if (amount <= 0L || !canInsert(from)) {
            return 0L;
        }
        long accepted = EuStorageBounds.accept(energy, spec.capacityEu(), amount);
        if (accepted <= 0L) {
            return 0L;
        }
        if (!simulate) {
            energy += accepted;
            setChanged();
        }
        return accepted;
    }

    @Override
    public long extractEu(long amount, Direction to, boolean simulate) {
        if (amount <= 0L || !canExtract(to) || energy <= 0L) {
            return 0L;
        }
        long extracted = Math.min(amount, energy);
        if (!simulate) {
            energy -= extracted;
            setChanged();
        }
        return extracted;
    }

    @Override
    public boolean canInsert(Direction from) {
        return from != null && from != getOutputSide();
    }

    @Override
    public boolean canExtract(Direction to) {
        return to != null && to == getOutputSide();
    }

    @Override
    public boolean isFullEnergyOutput() {
        return true;
    }

    @Override
    public void setStoredEnergyFromItem(long amount) {
        energy = EuStorageBounds.clamp(amount, spec.capacityEu());
        setChanged();
    }

    protected final NonNullList<ItemStack> mutableItems() {
        return items;
    }

    protected final void saveStorageState(CompoundTag tag) {
        tag.putLong("energy", EuStorageBounds.clamp(energy, spec.capacityEu()));
        tag.putByte("redstoneMode", (byte) redstoneMode);
    }

    protected final void loadStorageState(CompoundTag tag) {
        energy = EuStorageBounds.clamp(tag.getLong("energy"), spec.capacityEu());
        redstoneMode = normalizeRedstoneMode(tag.getByte("redstoneMode"));
        cachedRedstoneOutput = -1;
    }

    private long getEuFree() {
        return EuStorageBounds.free(energy, spec.capacityEu());
    }

    private static int normalizeRedstoneMode(int mode) {
        return mode >= 0 && mode < REDSTONE_MODE_COUNT ? mode : 0;
    }
}
