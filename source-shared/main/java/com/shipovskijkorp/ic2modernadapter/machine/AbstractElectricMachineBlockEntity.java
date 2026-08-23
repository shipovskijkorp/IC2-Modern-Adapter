package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.energy.api.IEuEnergyStorage;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageBounds;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageItemHooks;
import java.util.Arrays;
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
 * Loader-neutral base for IC2 electric processing machines.
 *
 * <p>This mirrors IndustrialLegacy's split between an electric-machine base and a standard
 * processing loop: inventory, EU buffer, discharge slot, sided IO and menu sync are centralized,
 * while individual machines supply only their {@link MachineSpec} and recipe lookup.</p>
 */
public abstract class AbstractElectricMachineBlockEntity extends BlockEntity
        implements WorldlyContainer, IEuEnergyStorage, MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_DISCHARGE = 2;
    public static final int SLOT_UPGRADE_0 = 3;
    public static final int UPGRADE_SLOTS = 4;
    public static final int SLOT_COUNT = SLOT_UPGRADE_0 + UPGRADE_SLOTS;

    public static final int DATA_ENERGY = 0;
    public static final int DATA_CAPACITY = 1;
    public static final int DATA_PROGRESS = 2;
    public static final int DATA_MAX_PROGRESS = 3;
    public static final int DATA_MACHINE = 4;
    public static final int DATA_COUNT = 5;

    private static final int[] TOP_SLOTS = {SLOT_INPUT};
    private static final int[] SIDE_SLOTS = {SLOT_INPUT, SLOT_DISCHARGE, SLOT_UPGRADE_0, SLOT_UPGRADE_0 + 1, SLOT_UPGRADE_0 + 2, SLOT_UPGRADE_0 + 3};
    private static final int[] BOTTOM_SLOTS = {SLOT_OUTPUT};

    private final MachineSpec spec;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private long energy;
    protected int progress;
    protected int maxProgress;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY -> (int) Math.min(Integer.MAX_VALUE, AbstractElectricMachineBlockEntity.this.energy);
                case DATA_CAPACITY -> (int) Math.min(Integer.MAX_VALUE, spec.capacityEu());
                case DATA_PROGRESS -> AbstractElectricMachineBlockEntity.this.progress;
                case DATA_MAX_PROGRESS -> AbstractElectricMachineBlockEntity.this.maxProgress;
                case DATA_MACHINE -> spec.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_ENERGY -> AbstractElectricMachineBlockEntity.this.energy = EuStorageBounds.clamp(value, spec.capacityEu());
                case DATA_PROGRESS -> AbstractElectricMachineBlockEntity.this.progress = Math.max(0, value);
                case DATA_MAX_PROGRESS -> AbstractElectricMachineBlockEntity.this.maxProgress = Math.max(1, value);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    protected AbstractElectricMachineBlockEntity(
            BlockEntityType<?> type,
            MachineSpec spec,
            BlockPos pos,
            BlockState state) {
        super(type, pos, state);
        this.spec = spec;
        this.maxProgress = spec.operationTicks();
        MachineSpec stateSpec = MachineSpec.fromBlockState(state);
        if (stateSpec != null && stateSpec != spec) {
            throw new IllegalArgumentException(
                    "Machine block entity type " + spec.blockEntityPath()
                            + " does not match block state variant " + stateSpec.variantKey());
        }
    }

    protected abstract MenuType<?> machineMenuType();

    public final MachineSpec spec() {
        return spec;
    }

    public final ContainerData menuData() {
        return menuData;
    }

    protected final boolean chargeFromDischargeSlot() {
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

    protected final boolean useEnergy(long amount) {
        if (amount <= 0L) {
            return true;
        }
        if (energy < amount) {
            return false;
        }
        energy -= amount;
        return true;
    }

    protected final void resetProgress() {
        progress = 0;
        maxProgress = spec.operationTicks();
    }

    protected final boolean canOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return stack.getCount() <= stack.getMaxStackSize();
        }
        if (!canStacksMerge(output, stack)) {
            return false;
        }
        return output.getCount() + stack.getCount() <= output.getMaxStackSize();
    }

    protected final void insertOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            items.set(SLOT_OUTPUT, stack.copy());
        } else if (canStacksMerge(output, stack)) {
            output.grow(stack.getCount());
        }
    }

    protected final boolean canStacksMerge(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty() || left.getItem() != right.getItem()) {
            return false;
        }
        String leftVariant = com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeStacks.INSTANCE.variantKey(left);
        String rightVariant = com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeStacks.INSTANCE.variantKey(right);
        if (leftVariant == null ? rightVariant != null : !leftVariant.equals(rightVariant)) {
            return false;
        }
        return left.getDamageValue() == right.getDamageValue();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(spec.translationKey());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new com.shipovskijkorp.ic2modernadapter.menu.MachineMenu(
                machineMenuType(), containerId, inventory, this, menuData);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
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
        for (int slot = 0; slot < items.size(); slot++) {
            items.set(slot, ItemStack.EMPTY);
        }
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return switch (slot) {
            case SLOT_INPUT -> LegacyMachineRecipeRegistry.isInput(spec, stack);
            case SLOT_DISCHARGE -> EuStorageItemHooks.canDischarge(stack, spec.tier());
            case SLOT_UPGRADE_0, SLOT_UPGRADE_0 + 1, SLOT_UPGRADE_0 + 2, SLOT_UPGRADE_0 + 3 -> isUpgradeStack(stack);
            default -> false;
        };
    }

    public static boolean isUpgradeStack(ItemStack stack) {
        net.minecraft.resources.ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && "ic2".equals(id.getNamespace()) && "upgrade".equals(id.getPath());
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP) {
            return TOP_SLOTS.clone();
        }
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS.clone();
        }
        return SIDE_SLOTS.clone();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack) && Arrays.stream(getSlotsForFace(side == null ? Direction.NORTH : side)).anyMatch(value -> value == slot);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == SLOT_OUTPUT && side == Direction.DOWN;
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
        return 0;
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
        return 0L;
    }

    @Override
    public boolean canInsert(Direction from) {
        return from != null;
    }

    @Override
    public boolean canExtract(Direction to) {
        return false;
    }

    protected final NonNullList<ItemStack> mutableItems() {
        return items;
    }

    protected final void saveMachineState(CompoundTag tag) {
        tag.putLong("energy", EuStorageBounds.clamp(energy, spec.capacityEu()));
        tag.putInt("progress", Math.max(0, progress));
        tag.putInt("maxProgress", Math.max(1, maxProgress));
    }

    protected final void loadMachineState(CompoundTag tag) {
        energy = EuStorageBounds.clamp(tag.getLong("energy"), spec.capacityEu());
        progress = Math.max(0, tag.getInt("progress"));
        maxProgress = Math.max(1, tag.contains("maxProgress") ? tag.getInt("maxProgress") : spec.operationTicks());
    }

    private long getEuFree() {
        return EuStorageBounds.free(energy, spec.capacityEu());
    }
}
