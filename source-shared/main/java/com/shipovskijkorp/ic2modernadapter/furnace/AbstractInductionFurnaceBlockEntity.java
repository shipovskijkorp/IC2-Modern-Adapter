package com.shipovskijkorp.ic2modernadapter.furnace;

import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantFacingBlock;
import com.shipovskijkorp.ic2modernadapter.energy.api.IEuEnergyStorage;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageBounds;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageItemHooks;
import com.shipovskijkorp.ic2modernadapter.machine.AbstractElectricMachineBlockEntity;
import com.shipovskijkorp.ic2modernadapter.menu.InductionFurnaceMenu;
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

/** Loader-neutral IC2 Induction Furnace with original heat/progress behavior. */
public abstract class AbstractInductionFurnaceBlockEntity extends BlockEntity
        implements WorldlyContainer, IEuEnergyStorage, MenuProvider {
    public static final int SLOT_INPUT_A = 0;
    public static final int SLOT_INPUT_B = 1;
    public static final int SLOT_OUTPUT_A = 2;
    public static final int SLOT_OUTPUT_B = 3;
    public static final int SLOT_DISCHARGE = 4;
    public static final int SLOT_UPGRADE_0 = 5;
    public static final int UPGRADE_SLOTS = 2;
    public static final int SLOT_COUNT = SLOT_UPGRADE_0 + UPGRADE_SLOTS;

    public static final int DATA_ENERGY = 0;
    public static final int DATA_CAPACITY = 1;
    public static final int DATA_HEAT = 2;
    public static final int DATA_MAX_HEAT = 3;
    public static final int DATA_PROGRESS = 4;
    public static final int DATA_MAX_PROGRESS = 5;
    public static final int DATA_COUNT = 6;

    private static final int[] TOP_SLOTS = {SLOT_INPUT_A, SLOT_INPUT_B};
    private static final int[] SIDE_SLOTS = {SLOT_INPUT_A, SLOT_INPUT_B, SLOT_DISCHARGE, SLOT_UPGRADE_0, SLOT_UPGRADE_0 + 1};
    private static final int[] BOTTOM_SLOTS = {SLOT_OUTPUT_A, SLOT_OUTPUT_B};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private long energy;
    private int heat;
    private int progress;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY -> (int) Math.min(Integer.MAX_VALUE, AbstractInductionFurnaceBlockEntity.this.energy);
                case DATA_CAPACITY -> (int) Math.min(Integer.MAX_VALUE, FurnaceSpec.INDUCTION.capacityEu());
                case DATA_HEAT -> AbstractInductionFurnaceBlockEntity.this.heat;
                case DATA_MAX_HEAT -> FurnaceSpec.INDUCTION_MAX_HEAT;
                case DATA_PROGRESS -> AbstractInductionFurnaceBlockEntity.this.progress;
                case DATA_MAX_PROGRESS -> FurnaceSpec.INDUCTION.operationTicks();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_ENERGY -> AbstractInductionFurnaceBlockEntity.this.energy = EuStorageBounds.clamp(value, FurnaceSpec.INDUCTION.capacityEu());
                case DATA_HEAT -> AbstractInductionFurnaceBlockEntity.this.heat = clampHeat(value);
                case DATA_PROGRESS -> AbstractInductionFurnaceBlockEntity.this.progress = Math.max(0, Math.min(FurnaceSpec.INDUCTION.operationTicks(), value));
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    protected AbstractInductionFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        FurnaceSpec stateSpec = FurnaceSpec.fromBlockState(state);
        if (stateSpec != null && stateSpec != FurnaceSpec.INDUCTION) {
            throw new IllegalArgumentException("Induction furnace block entity does not match block state " + stateSpec.variantKey());
        }
    }

    protected abstract MenuType<?> inductionFurnaceMenuType();

    protected abstract @Nullable SmeltingRecipeMatch findSmeltingRecipe(ItemStack input);

    public final void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        boolean changed = chargeFromDischargeSlot();
        boolean newActive = getBlockState().hasProperty(LegacyVariantFacingBlock.ACTIVE)
                && getBlockState().getValue(LegacyVariantFacingBlock.ACTIVE);
        if (heat == 0) {
            newActive = false;
        }

        if (progress >= FurnaceSpec.INDUCTION.operationTicks()) {
            changed |= operateBoth();
            progress = 0;
            newActive = false;
        }

        boolean canOperate = canOperateAny();
        boolean redstone = level.hasNeighborSignal(worldPosition);
        if ((canOperate || redstone) && useEnergy(FurnaceSpec.INDUCTION_HEATUP_EU_PER_TICK)) {
            if (heat < FurnaceSpec.INDUCTION_MAX_HEAT) {
                heat++;
            }
            newActive = true;
            changed = true;
        } else if (heat > 0) {
            heat -= Math.min(heat, FurnaceSpec.INDUCTION_COOLDOWN_PER_TICK);
            changed = true;
        }

        if (!newActive || progress == 0) {
            if (canOperate) {
                if (energy >= FurnaceSpec.INDUCTION.euPerTick()) {
                    newActive = true;
                }
            } else if (progress != 0) {
                progress = 0;
                changed = true;
            }
        } else if (!canOperate || energy < FurnaceSpec.INDUCTION.euPerTick()) {
            if (!canOperate && progress != 0) {
                progress = 0;
                changed = true;
            }
            newActive = false;
        }

        if (newActive && canOperate) {
            progress += heat / 30;
            if (progress > FurnaceSpec.INDUCTION.operationTicks()) {
                progress = FurnaceSpec.INDUCTION.operationTicks();
            }
            useEnergy(FurnaceSpec.INDUCTION.euPerTick());
            changed = true;
        }

        if (setActive(newActive)) {
            changed = true;
        }
        if (changed) {
            setChanged();
        }
    }

    private boolean canOperateAny() {
        return canOperate(SLOT_INPUT_A, SLOT_OUTPUT_A) || canOperate(SLOT_INPUT_B, SLOT_OUTPUT_B);
    }

    private boolean canOperate(int inputSlot, int outputSlot) {
        ItemStack input = items.get(inputSlot);
        SmeltingRecipeMatch match = findSmeltingRecipe(input);
        return match != null && !match.isEmpty() && FurnaceInventoryUtil.canOutput(items.get(outputSlot), match.output());
    }

    private boolean operateBoth() {
        boolean did = false;
        did |= operate(SLOT_INPUT_A, SLOT_OUTPUT_A);
        did |= operate(SLOT_INPUT_B, SLOT_OUTPUT_B);
        return did;
    }

    private boolean operate(int inputSlot, int outputSlot) {
        ItemStack input = items.get(inputSlot);
        SmeltingRecipeMatch match = findSmeltingRecipe(input);
        if (match == null || match.isEmpty() || !FurnaceInventoryUtil.canOutput(items.get(outputSlot), match.output())) {
            return false;
        }
        input.shrink(1);
        FurnaceInventoryUtil.insertOutput(items, outputSlot, match.output());
        return true;
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
        long extracted = EuStorageItemHooks.dischargeIntoStorage(stack, free, FurnaceSpec.INDUCTION.tier(), false);
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

    private boolean useEnergy(long amount) {
        if (amount <= 0L) {
            return true;
        }
        if (energy < amount) {
            return false;
        }
        energy -= amount;
        return true;
    }

    protected final void saveInductionFurnaceState(CompoundTag tag) {
        tag.putLong("energy", EuStorageBounds.clamp(energy, FurnaceSpec.INDUCTION.capacityEu()));
        tag.putInt("heat", clampHeat(heat));
        tag.putInt("progress", Math.max(0, Math.min(FurnaceSpec.INDUCTION.operationTicks(), progress)));
    }

    protected final void loadInductionFurnaceState(CompoundTag tag) {
        energy = EuStorageBounds.clamp(tag.getLong("energy"), FurnaceSpec.INDUCTION.capacityEu());
        heat = clampHeat(tag.getInt("heat"));
        progress = Math.max(0, Math.min(FurnaceSpec.INDUCTION.operationTicks(), tag.getInt("progress")));
    }

    protected final NonNullList<ItemStack> mutableItems() {
        return items;
    }

    public final int getComparatorLevel() {
        return heat * 15 / FurnaceSpec.INDUCTION_MAX_HEAT;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(FurnaceSpec.INDUCTION.translationKey());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new InductionFurnaceMenu(inductionFurnaceMenuType(), containerId, inventory, this, menuData);
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
            case SLOT_INPUT_A, SLOT_INPUT_B -> getLevel() == null || findSmeltingRecipe(stack) != null;
            case SLOT_DISCHARGE -> EuStorageItemHooks.canDischarge(stack, FurnaceSpec.INDUCTION.tier());
            case SLOT_UPGRADE_0, SLOT_UPGRADE_0 + 1 -> AbstractElectricMachineBlockEntity.isUpgradeStack(stack);
            default -> false;
        };
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
        return canPlaceItem(slot, stack)
                && Arrays.stream(getSlotsForFace(side == null ? Direction.NORTH : side)).anyMatch(value -> value == slot);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return (slot == SLOT_OUTPUT_A || slot == SLOT_OUTPUT_B) && side == Direction.DOWN;
    }

    @Override
    public long getEuStored() {
        return energy;
    }

    @Override
    public long getEuCapacity() {
        return FurnaceSpec.INDUCTION.capacityEu();
    }

    @Override
    public int getSinkTier() {
        return FurnaceSpec.INDUCTION.tier();
    }

    @Override
    public int getSourceTier() {
        return 0;
    }

    @Override
    public long insertEu(long amount, Direction from, boolean simulate) {
        if (amount <= 0L || from == null) {
            return 0L;
        }
        long accepted = EuStorageBounds.accept(energy, FurnaceSpec.INDUCTION.capacityEu(), amount);
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

    private long getEuFree() {
        return EuStorageBounds.free(energy, FurnaceSpec.INDUCTION.capacityEu());
    }

    private boolean setActive(boolean active) {
        Level level = getLevel();
        if (level == null) {
            return false;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(LegacyVariantFacingBlock.ACTIVE)
                || state.getValue(LegacyVariantFacingBlock.ACTIVE) == active) {
            return false;
        }
        level.setBlock(worldPosition, state.setValue(LegacyVariantFacingBlock.ACTIVE, active), 3);
        return true;
    }

    private static int clampHeat(int value) {
        return Math.max(0, Math.min(FurnaceSpec.INDUCTION_MAX_HEAT, value));
    }
}
