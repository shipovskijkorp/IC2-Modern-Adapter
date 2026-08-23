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

/** Loader-neutral implementation of IC2's Ore Washing Plant, including its water tank. */
public abstract class AbstractOreWashingPlantBlockEntity extends AbstractElectricMachineBlockEntity {
    public static final int SLOT_FLUID = 7;
    public static final int SLOT_CELL = 8;
    public static final int SLOT_OUTPUT_1 = 9;
    public static final int SLOT_OUTPUT_2 = 10;
    public static final int ORE_WASHING_SLOT_COUNT = 11;
    public static final int WATER_CAPACITY_MB = 8_000;

    private static final int EXTRA_WATER = 0;
    private static final int[] OUTPUT_SLOTS = {SLOT_OUTPUT, SLOT_OUTPUT_1, SLOT_OUTPUT_2};
    private static final int[] TOP_SLOTS = {SLOT_INPUT, SLOT_FLUID};
    private static final int[] SIDE_SLOTS = {
            SLOT_INPUT, SLOT_FLUID, SLOT_DISCHARGE, SLOT_UPGRADE_0, SLOT_UPGRADE_0 + 1, SLOT_UPGRADE_0 + 2, SLOT_UPGRADE_0 + 3};
    private static final int[] BOTTOM_SLOTS = {SLOT_OUTPUT, SLOT_OUTPUT_1, SLOT_OUTPUT_2, SLOT_CELL};

    private int waterMb;
    private String activeRecipeSource = "";

    protected AbstractOreWashingPlantBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, MachineSpec.ORE_WASHING_PLANT, pos, state, ORE_WASHING_SLOT_COUNT);
    }

    @Override
    protected int getExtraDataCount() {
        return 1;
    }

    @Override
    protected int getExtraData(int index) {
        return index == EXTRA_WATER ? waterMb : 0;
    }

    @Override
    protected void setExtraData(int index, int value) {
        if (index == EXTRA_WATER) {
            waterMb = clampWater(value);
        }
    }

    public final int waterMb() {
        return waterMb;
    }

    public final int waterCapacityMb() {
        return WATER_CAPACITY_MB;
    }

    public final void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        boolean changed = chargeFromDischargeSlot();
        changed |= gainFluid();
        boolean active = process();
        if (changed || active) {
            setChanged();
        }
        setMachineActive(active);
    }

    private boolean gainFluid() {
        if (waterMb > WATER_CAPACITY_MB - 1_000) {
            return false;
        }
        ItemStack input = getItem(SLOT_FLUID);
        if (input.isEmpty() || !isWaterContainer(input)) {
            return false;
        }
        ItemStack remainder = LegacyRecipeRuntime.craftingRemainder(input, LegacyRecipeStacks.INSTANCE);
        if (!remainder.isEmpty() && !canInsertStackIntoSlot(SLOT_CELL, remainder, getItem(SLOT_CELL).copy())) {
            return false;
        }
        input.shrink(1);
        waterMb += 1_000;
        if (!remainder.isEmpty()) {
            insertIntoSlot(SLOT_CELL, remainder);
        }
        return true;
    }

    private boolean process() {
        ItemStack input = getItem(SLOT_INPUT);
        LegacyMachineRecipeDefinition recipe = LegacyMachineRecipeRegistry.find(MachineSpec.ORE_WASHING_PLANT, input);
        if (recipe == null || recipe.fluidMb() > waterMb) {
            if (recipe == null && (progress != 0 || !activeRecipeSource.isEmpty())) {
                resetOreWasherProgress();
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
            if (input.getCount() >= recipe.inputCount() && waterMb >= recipe.fluidMb() && canOutputAll(outputs)) {
                input.shrink(recipe.inputCount());
                waterMb = Math.max(0, waterMb - recipe.fluidMb());
                insertOutputs(outputs);
            }
            resetOreWasherProgress();
        }
        return true;
    }

    private void resetOreWasherProgress() {
        resetProgress();
        activeRecipeSource = "";
    }

    protected final void saveOreWashingState(CompoundTag tag) {
        saveMachineState(tag);
        tag.putInt("waterMb", clampWater(waterMb));
        tag.putString("activeRecipeSource", activeRecipeSource);
    }

    protected final void loadOreWashingState(CompoundTag tag) {
        loadMachineState(tag);
        waterMb = clampWater(tag.getInt("waterMb"));
        activeRecipeSource = tag.getString("activeRecipeSource");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new com.shipovskijkorp.ic2modernadapter.menu.OreWashingPlantMenu(
                machineMenuType(), containerId, inventory, this, menuData());
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return switch (slot) {
            case SLOT_INPUT -> LegacyMachineRecipeRegistry.isInput(MachineSpec.ORE_WASHING_PLANT, stack);
            case SLOT_FLUID -> isWaterContainer(stack);
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
        return canPlaceItem(slot, stack) && Arrays.stream(getSlotsForFace(side == null ? Direction.NORTH : side)).anyMatch(value -> value == slot);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.DOWN && (slot == SLOT_OUTPUT || slot == SLOT_OUTPUT_1 || slot == SLOT_OUTPUT_2 || slot == SLOT_CELL);
    }

    private boolean canOutputAll(List<ItemStack> outputs) {
        NonNullList<ItemStack> snapshot = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < snapshot.size(); i++) {
            snapshot.set(i, getItem(i).copy());
        }
        for (ItemStack output : outputs) {
            if (output.isEmpty() || !canInsertIntoAnyOutput(snapshot, output.copy())) {
                return false;
            }
        }
        return true;
    }

    private boolean canInsertIntoAnyOutput(NonNullList<ItemStack> snapshot, ItemStack stack) {
        for (int slot : OUTPUT_SLOTS) {
            if (canInsertStackIntoSlot(slot, stack, snapshot.get(slot))) {
                insertStackIntoSnapshot(snapshot, slot, stack);
                return true;
            }
        }
        return false;
    }

    private boolean canInsertStackIntoSlot(int slot, ItemStack stack, ItemStack current) {
        if (stack.isEmpty()) {
            return false;
        }
        if (current.isEmpty()) {
            return stack.getCount() <= stack.getMaxStackSize();
        }
        return canStacksMerge(current, stack) && current.getCount() + stack.getCount() <= current.getMaxStackSize();
    }

    private void insertStackIntoSnapshot(NonNullList<ItemStack> snapshot, int slot, ItemStack stack) {
        ItemStack current = snapshot.get(slot);
        if (current.isEmpty()) {
            snapshot.set(slot, stack.copy());
        } else {
            current.grow(stack.getCount());
        }
    }

    private void insertOutputs(List<ItemStack> outputs) {
        for (ItemStack output : outputs) {
            for (int slot : OUTPUT_SLOTS) {
                if (canInsertStackIntoSlot(slot, output, getItem(slot))) {
                    insertIntoSlot(slot, output);
                    break;
                }
            }
        }
    }

    private void insertIntoSlot(int slot, ItemStack stack) {
        ItemStack current = getItem(slot);
        if (current.isEmpty()) {
            setItem(slot, stack.copy());
        } else if (canStacksMerge(current, stack)) {
            current.grow(stack.getCount());
            setChanged();
        }
    }

    private static List<ItemStack> createOutputs(LegacyMachineRecipeDefinition recipe) {
        List<ItemStack> outputs = new ArrayList<>(recipe.outputs().size());
        for (LegacyMachineRecipeDefinition.Output output : recipe.outputs()) {
            outputs.add(LegacyRecipeRuntime.createResult(output.item(), output.count(), LegacyRecipeStacks.INSTANCE));
        }
        return outputs;
    }

    private static boolean isWaterContainer(ItemStack stack) {
        return LegacyRecipeRuntime.matchesIngredient("fluid:water", stack, LegacyRecipeStacks.INSTANCE);
    }

    private static int clampWater(int value) {
        return Math.max(0, Math.min(WATER_CAPACITY_MB, value));
    }
}
