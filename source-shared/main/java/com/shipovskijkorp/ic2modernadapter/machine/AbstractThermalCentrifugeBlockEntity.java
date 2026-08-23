package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeRuntime;
import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeStacks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Loader-neutral implementation of IC2's Thermal Centrifuge. */
public abstract class AbstractThermalCentrifugeBlockEntity extends AbstractElectricMachineBlockEntity {
    public static final int SLOT_OUTPUT_1 = 7;
    public static final int SLOT_OUTPUT_2 = 8;
    public static final int THERMAL_CENTRIFUGE_SLOT_COUNT = 9;
    public static final int MAX_HEAT = 5_000;

    private static final int EXTRA_HEAT = 0;
    private static final int EXTRA_WORK_HEAT = 1;
    private static final int[] OUTPUT_SLOTS = {SLOT_OUTPUT, SLOT_OUTPUT_1, SLOT_OUTPUT_2};
    private static final int[] TOP_SLOTS = {SLOT_INPUT};
    private static final int[] SIDE_SLOTS = {
            SLOT_INPUT, SLOT_DISCHARGE, SLOT_UPGRADE_0, SLOT_UPGRADE_0 + 1, SLOT_UPGRADE_0 + 2, SLOT_UPGRADE_0 + 3};
    private static final int[] BOTTOM_SLOTS = {SLOT_OUTPUT, SLOT_OUTPUT_1, SLOT_OUTPUT_2};

    private int heat;
    private int workHeat = MAX_HEAT;
    private String activeRecipeSource = "";

    protected AbstractThermalCentrifugeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, MachineSpec.THERMAL_CENTRIFUGE, pos, state, THERMAL_CENTRIFUGE_SLOT_COUNT);
    }

    @Override
    protected int getExtraDataCount() {
        return 2;
    }

    @Override
    protected int getExtraData(int index) {
        return switch (index) {
            case EXTRA_HEAT -> heat;
            case EXTRA_WORK_HEAT -> workHeat;
            default -> 0;
        };
    }

    @Override
    protected void setExtraData(int index, int value) {
        switch (index) {
            case EXTRA_HEAT -> heat = clampHeat(value);
            case EXTRA_WORK_HEAT -> workHeat = clampWorkHeat(value);
            default -> {
            }
        }
    }

    public final int heat() {
        return heat;
    }

    public final int workHeat() {
        return workHeat;
    }

    public final void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        boolean changed = chargeFromDischargeSlot();
        changed |= updateHeat();
        boolean active = process();
        if (changed || active) {
            setChanged();
        }
        setMachineActive(active || heat > 0);
    }

    private boolean updateHeat() {
        int previousHeat = heat;
        int previousWorkHeat = workHeat;
        LegacyMachineRecipeDefinition recipe = LegacyMachineRecipeRegistry.find(
                MachineSpec.THERMAL_CENTRIFUGE, getItem(SLOT_INPUT));
        int targetHeat = recipe == null ? 0 : Math.min(MAX_HEAT, recipe.heat());
        workHeat = targetHeat <= 0 ? MAX_HEAT : targetHeat;
        if (targetHeat > 0) {
            if (heat > targetHeat) {
                heat = targetHeat;
            } else if (heat < targetHeat && useEnergy(1L)) {
                heat++;
            }
        } else if (heat > 0) {
            heat--;
        }
        return previousHeat != heat || previousWorkHeat != workHeat;
    }

    private boolean process() {
        ItemStack input = getItem(SLOT_INPUT);
        LegacyMachineRecipeDefinition recipe = LegacyMachineRecipeRegistry.find(MachineSpec.THERMAL_CENTRIFUGE, input);
        if (recipe == null || heat < recipe.heat()) {
            if (recipe == null && (progress != 0 || !activeRecipeSource.isEmpty())) {
                resetThermalCentrifugeProgress();
                setChanged();
            }
            return false;
        }
        if (!recipe.source().equals(activeRecipeSource)) {
            resetProgress();
            activeRecipeSource = recipe.source();
        }
        List<ItemStack> outputs = createOutputs(recipe);
        if (!canOutputAll(outputs) || !useEnergy(spec().euPerTick())) {
            return false;
        }
        maxProgress = spec().operationTicks();
        progress++;
        if (progress >= maxProgress) {
            if (input.getCount() >= recipe.inputCount() && canOutputAll(outputs)) {
                input.shrink(recipe.inputCount());
                insertOutputs(outputs);
            }
            resetThermalCentrifugeProgress();
        }
        return true;
    }

    private void resetThermalCentrifugeProgress() {
        resetProgress();
        activeRecipeSource = "";
    }

    protected final void saveThermalCentrifugeState(CompoundTag tag) {
        saveMachineState(tag);
        tag.putInt("heat", heat);
        tag.putInt("workHeat", workHeat);
        tag.putString("activeRecipeSource", activeRecipeSource);
    }

    protected final void loadThermalCentrifugeState(CompoundTag tag) {
        loadMachineState(tag);
        heat = clampHeat(tag.getInt("heat"));
        workHeat = clampWorkHeat(tag.getInt("workHeat"));
        activeRecipeSource = tag.getString("activeRecipeSource");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new com.shipovskijkorp.ic2modernadapter.menu.ThermalCentrifugeMenu(
                machineMenuType(), containerId, inventory, this, menuData());
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return switch (slot) {
            case SLOT_INPUT -> LegacyMachineRecipeRegistry.isInput(spec(), stack);
            case SLOT_DISCHARGE -> com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageItemHooks.canDischarge(stack, spec().tier());
            case SLOT_UPGRADE_0, SLOT_UPGRADE_0 + 1, SLOT_UPGRADE_0 + 2, SLOT_UPGRADE_0 + 3 -> isUpgradeStack(stack);
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
        return Arrays.stream(BOTTOM_SLOTS).anyMatch(value -> value == slot);
    }

    private static List<ItemStack> createOutputs(LegacyMachineRecipeDefinition recipe) {
        List<ItemStack> outputs = new ArrayList<>(recipe.outputs().size());
        for (LegacyMachineRecipeDefinition.Output output : recipe.outputs()) {
            outputs.add(LegacyRecipeRuntime.createResult(output.item(), output.count(), LegacyRecipeStacks.INSTANCE));
        }
        return outputs;
    }

    private boolean canOutputAll(List<ItemStack> outputs) {
        NonNullList<ItemStack> snapshot = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < getContainerSize(); slot++) {
            snapshot.set(slot, getItem(slot).copy());
        }
        for (ItemStack output : outputs) {
            if (output.isEmpty() || !canInsertIntoAnyOutput(output, snapshot)) {
                return false;
            }
            insertIntoSnapshot(output, snapshot);
        }
        return true;
    }

    private static boolean canInsertIntoAnyOutput(ItemStack stack, NonNullList<ItemStack> snapshot) {
        for (int slot : OUTPUT_SLOTS) {
            ItemStack current = snapshot.get(slot);
            if (current.isEmpty()) {
                return stack.getCount() <= stack.getMaxStackSize();
            }
            if (canStacksMergeStatic(current, stack)
                    && current.getCount() + stack.getCount() <= current.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private static void insertIntoSnapshot(ItemStack stack, NonNullList<ItemStack> snapshot) {
        for (int slot : OUTPUT_SLOTS) {
            ItemStack current = snapshot.get(slot);
            if (current.isEmpty()) {
                snapshot.set(slot, stack.copy());
                return;
            }
            if (canStacksMergeStatic(current, stack)
                    && current.getCount() + stack.getCount() <= current.getMaxStackSize()) {
                current.grow(stack.getCount());
                return;
            }
        }
    }

    private void insertOutputs(List<ItemStack> outputs) {
        for (ItemStack output : outputs) {
            for (int slot : OUTPUT_SLOTS) {
                ItemStack current = getItem(slot);
                if (current.isEmpty()) {
                    setItem(slot, output.copy());
                    break;
                }
                if (canStacksMerge(current, output)
                        && current.getCount() + output.getCount() <= current.getMaxStackSize()) {
                    current.grow(output.getCount());
                    setChanged();
                    break;
                }
            }
        }
    }

    private static boolean canStacksMergeStatic(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty() || left.getItem() != right.getItem()) {
            return false;
        }
        String leftVariant = LegacyRecipeStacks.INSTANCE.variantKey(left);
        String rightVariant = LegacyRecipeStacks.INSTANCE.variantKey(right);
        if (leftVariant == null ? rightVariant != null : !leftVariant.equals(rightVariant)) {
            return false;
        }
        return left.getDamageValue() == right.getDamageValue();
    }

    private static int clampHeat(int value) {
        return Math.max(0, Math.min(MAX_HEAT, value));
    }

    private static int clampWorkHeat(int value) {
        return value <= 0 ? MAX_HEAT : clampHeat(value);
    }
}
