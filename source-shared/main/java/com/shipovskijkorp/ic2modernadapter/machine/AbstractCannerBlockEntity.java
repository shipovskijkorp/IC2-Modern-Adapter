package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageItemHooks;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

/** Loader-neutral implementation of IC2's Fluid/Solid Canning Machine. */
public abstract class AbstractCannerBlockEntity extends AbstractElectricMachineBlockEntity {
    public static final int SLOT_CAN_INPUT = AbstractElectricMachineBlockEntity.SLOT_COUNT;
    public static final int CANNER_SLOT_COUNT = SLOT_CAN_INPUT + 1;
    public static final int TANK_CAPACITY_MB = 8000;

    private static final int EXTRA_MODE = 0;
    private static final int EXTRA_INPUT_AMOUNT = 1;
    private static final int EXTRA_OUTPUT_AMOUNT = 2;
    private static final int EXTRA_INPUT_FLUID = 3;
    private static final int EXTRA_OUTPUT_FLUID = 4;

    private static final int[] TOP_SLOTS = {SLOT_INPUT, SLOT_CAN_INPUT};
    private static final int[] SIDE_SLOTS = {SLOT_INPUT, SLOT_CAN_INPUT, SLOT_DISCHARGE, SLOT_UPGRADE_0, SLOT_UPGRADE_0 + 1, SLOT_UPGRADE_0 + 2, SLOT_UPGRADE_0 + 3};
    private static final int[] BOTTOM_SLOTS = {SLOT_OUTPUT};

    private CannerMode mode = CannerMode.BOTTLE_SOLID;
    private Ic2FluidKind inputFluid = Ic2FluidKind.EMPTY;
    private int inputAmountMb;
    private Ic2FluidKind outputFluid = Ic2FluidKind.EMPTY;
    private int outputAmountMb;
    private String activeRecipeId = "";

    protected AbstractCannerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, MachineSpec.CANNER, pos, state, CANNER_SLOT_COUNT);
    }

    @Override
    protected int getExtraDataCount() {
        return 5;
    }

    @Override
    protected int getExtraData(int index) {
        return switch (index) {
            case EXTRA_MODE -> mode.id();
            case EXTRA_INPUT_AMOUNT -> inputAmountMb;
            case EXTRA_OUTPUT_AMOUNT -> outputAmountMb;
            case EXTRA_INPUT_FLUID -> inputFluid.ordinal();
            case EXTRA_OUTPUT_FLUID -> outputFluid.ordinal();
            default -> 0;
        };
    }

    @Override
    protected void setExtraData(int index, int value) {
        switch (index) {
            case EXTRA_MODE -> mode = CannerMode.byId(value);
            case EXTRA_INPUT_AMOUNT -> inputAmountMb = clampAmount(value);
            case EXTRA_OUTPUT_AMOUNT -> outputAmountMb = clampAmount(value);
            case EXTRA_INPUT_FLUID -> inputFluid = Ic2FluidKind.byId(value);
            case EXTRA_OUTPUT_FLUID -> outputFluid = Ic2FluidKind.byId(value);
            default -> { }
        }
        sanitizeTanks();
    }

    public final CannerMode mode() {
        return mode;
    }

    public final Ic2FluidKind inputFluid() {
        return inputFluid;
    }

    public final int inputAmountMb() {
        return inputAmountMb;
    }

    public final Ic2FluidKind outputFluid() {
        return outputFluid;
    }

    public final int outputAmountMb() {
        return outputAmountMb;
    }

    public final void setMode(CannerMode mode) {
        CannerMode next = mode == null ? CannerMode.BOTTLE_SOLID : mode;
        if (this.mode == next) {
            return;
        }
        this.mode = next;
        resetCannerProgress();
        setChanged();
    }

    public final void cycleMode() {
        setMode(mode.next());
    }

    public final void swapTanks() {
        if (progress != 0) {
            return;
        }
        Ic2FluidKind fluid = inputFluid;
        int amount = inputAmountMb;
        inputFluid = outputFluid;
        inputAmountMb = outputAmountMb;
        outputFluid = fluid;
        outputAmountMb = amount;
        sanitizeTanks();
        setChanged();
    }

    public final void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        boolean changed = chargeFromDischargeSlot();
        boolean active = switch (mode) {
            case BOTTLE_SOLID -> processBottleSolid();
            case EMPTY_LIQUID -> processEmptyLiquid();
            case BOTTLE_LIQUID -> processBottleLiquid();
            case ENRICH_LIQUID -> processEnrichLiquid();
        };
        if (changed || active) {
            setChanged();
        }
        setMachineActive(active);
    }

    private boolean processBottleSolid() {
        ItemStack container = getItem(SLOT_CAN_INPUT);
        ItemStack fill = getItem(SLOT_INPUT);
        CannerBottleRecipeDefinition recipe = CannerRecipeRegistry.findBottleRecipe(container, fill);
        if (recipe == null) {
            if (progress != 0 || !activeRecipeId.isEmpty()) {
                resetCannerProgress();
                setChanged();
            }
            return false;
        }
        if (!recipe.id().equals(activeRecipeId)) {
            resetProgress();
            activeRecipeId = recipe.id();
        }
        ItemStack output = recipe.createOutput();
        if (!canOutput(output) || !useEnergy(spec().euPerTick())) {
            return false;
        }
        maxProgress = spec().operationTicks();
        progress++;
        if (progress >= maxProgress) {
            if (container.getCount() >= recipe.containerCount()
                    && fill.getCount() >= recipe.fillCount()
                    && canOutput(output)) {
                container.shrink(recipe.containerCount());
                fill.shrink(recipe.fillCount());
                insertOutput(output);
            }
            resetCannerProgress();
        }
        return true;
    }

    private boolean processEmptyLiquid() {
        ItemStack container = getItem(SLOT_CAN_INPUT);
        CannerFluidContainers.DrainResult drain = CannerFluidContainers.drain(container);
        if (drain == null || !canAddInputFluid(drain.fluid(), drain.amountMb()) || !canOutput(drain.emptyContainer())) {
            resetIfProgressing();
            return false;
        }
        if (!useEnergy(spec().euPerTick())) {
            return false;
        }
        progressTowardsOperation();
        if (progress >= maxProgress) {
            container.shrink(1);
            addInputFluid(drain.fluid(), drain.amountMb());
            insertOutput(drain.emptyContainer());
            resetCannerProgress();
        }
        return true;
    }

    private boolean processBottleLiquid() {
        ItemStack container = getItem(SLOT_CAN_INPUT);
        CannerFluidContainers.FillResult fill = CannerFluidContainers.fill(container, inputFluid, inputAmountMb);
        if (fill == null || !canOutput(fill.filledContainer())) {
            resetIfProgressing();
            return false;
        }
        if (!useEnergy(spec().euPerTick())) {
            return false;
        }
        progressTowardsOperation();
        if (progress >= maxProgress) {
            container.shrink(1);
            inputAmountMb -= fill.amountMb();
            sanitizeTanks();
            insertOutput(fill.filledContainer());
            resetCannerProgress();
        }
        return true;
    }

    private boolean processEnrichLiquid() {
        ItemStack additive = getItem(SLOT_INPUT);
        CannerEnrichRecipeDefinition recipe = CannerRecipeRegistry.findEnrichRecipe(inputFluid, inputAmountMb, additive);
        if (recipe == null || !canAcceptEnrichOutput(recipe)) {
            resetIfProgressing();
            return false;
        }
        if (!recipe.id().equals(activeRecipeId)) {
            resetProgress();
            activeRecipeId = recipe.id();
        }
        if (!useEnergy(spec().euPerTick())) {
            return false;
        }
        maxProgress = spec().operationTicks();
        progress++;
        if (progress >= maxProgress) {
            additive.shrink(recipe.additiveCount());
            inputAmountMb -= recipe.inputAmountMb();
            sanitizeTanks();
            produceEnrichedFluid(recipe.outputFluid(), recipe.outputAmountMb());
            resetCannerProgress();
        }
        return true;
    }

    private boolean canAcceptEnrichOutput(CannerEnrichRecipeDefinition recipe) {
        ItemStack container = getItem(SLOT_CAN_INPUT);
        CannerFluidContainers.FillResult directFill = CannerFluidContainers.fill(container, recipe.outputFluid(), recipe.outputAmountMb());
        if (directFill != null && canOutput(directFill.filledContainer())) {
            return true;
        }
        return canAddOutputFluid(recipe.outputFluid(), recipe.outputAmountMb());
    }

    private void produceEnrichedFluid(Ic2FluidKind fluid, int amountMb) {
        ItemStack container = getItem(SLOT_CAN_INPUT);
        CannerFluidContainers.FillResult directFill = CannerFluidContainers.fill(container, fluid, amountMb);
        if (directFill != null && canOutput(directFill.filledContainer())) {
            container.shrink(1);
            insertOutput(directFill.filledContainer());
            int remaining = amountMb - directFill.amountMb();
            if (remaining > 0) {
                addOutputFluid(fluid, remaining);
            }
            return;
        }
        addOutputFluid(fluid, amountMb);
    }

    private void progressTowardsOperation() {
        activeRecipeId = mode.name();
        maxProgress = spec().operationTicks();
        progress++;
    }

    private void resetIfProgressing() {
        if (progress != 0 || !activeRecipeId.isEmpty()) {
            resetCannerProgress();
            setChanged();
        }
    }

    private void resetCannerProgress() {
        resetProgress();
        activeRecipeId = "";
    }

    protected final void saveCannerState(CompoundTag tag) {
        saveMachineState(tag);
        tag.putInt("mode", mode.id());
        tag.putString("inputFluid", inputFluid.key());
        tag.putInt("inputAmountMb", inputAmountMb);
        tag.putString("outputFluid", outputFluid.key());
        tag.putInt("outputAmountMb", outputAmountMb);
        tag.putString("activeRecipeId", activeRecipeId);
    }

    protected final void loadCannerState(CompoundTag tag) {
        loadMachineState(tag);
        mode = CannerMode.byId(tag.getInt("mode"));
        inputFluid = Ic2FluidKind.byKey(tag.getString("inputFluid"));
        inputAmountMb = clampAmount(tag.getInt("inputAmountMb"));
        outputFluid = Ic2FluidKind.byKey(tag.getString("outputFluid"));
        outputAmountMb = clampAmount(tag.getInt("outputAmountMb"));
        activeRecipeId = tag.getString("activeRecipeId");
        sanitizeTanks();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new com.shipovskijkorp.ic2modernadapter.menu.CannerMenu(
                machineMenuType(), containerId, inventory, this, menuData());
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return switch (slot) {
            case SLOT_INPUT -> mode == CannerMode.ENRICH_LIQUID
                    ? CannerRecipeRegistry.acceptsEnrichAdditive(stack, inputFluid, inputAmountMb)
                    : CannerRecipeRegistry.acceptsBottleFill(stack, getItem(SLOT_CAN_INPUT));
            case SLOT_CAN_INPUT -> acceptsCannerContainer(stack);
            case SLOT_DISCHARGE -> EuStorageItemHooks.canDischarge(stack, spec().tier());
            case SLOT_UPGRADE_0, SLOT_UPGRADE_0 + 1, SLOT_UPGRADE_0 + 2, SLOT_UPGRADE_0 + 3 -> isUpgradeStack(stack);
            default -> false;
        };
    }

    private boolean acceptsCannerContainer(ItemStack stack) {
        return switch (mode) {
            case BOTTLE_SOLID -> CannerRecipeRegistry.acceptsBottleContainer(stack, getItem(SLOT_INPUT));
            case EMPTY_LIQUID -> CannerFluidContainers.drain(stack) != null;
            case BOTTLE_LIQUID -> CannerFluidContainers.fill(stack, inputFluid, inputAmountMb) != null;
            case ENRICH_LIQUID -> CannerFluidContainers.isEmptyContainer(stack);
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
        return side == Direction.DOWN && slot == SLOT_OUTPUT;
    }

    private boolean canAddInputFluid(Ic2FluidKind fluid, int amountMb) {
        return canAddFluid(inputFluid, inputAmountMb, fluid, amountMb);
    }

    private boolean canAddOutputFluid(Ic2FluidKind fluid, int amountMb) {
        return canAddFluid(outputFluid, outputAmountMb, fluid, amountMb);
    }

    private void addInputFluid(Ic2FluidKind fluid, int amountMb) {
        if (inputAmountMb <= 0) {
            inputFluid = fluid;
        }
        inputAmountMb = clampAmount(inputAmountMb + amountMb);
        sanitizeTanks();
    }

    private void addOutputFluid(Ic2FluidKind fluid, int amountMb) {
        if (outputAmountMb <= 0) {
            outputFluid = fluid;
        }
        outputAmountMb = clampAmount(outputAmountMb + amountMb);
        sanitizeTanks();
    }

    private static boolean canAddFluid(Ic2FluidKind currentFluid, int currentAmount, Ic2FluidKind incomingFluid, int incomingAmount) {
        return incomingFluid != null
                && !incomingFluid.isEmpty()
                && incomingAmount > 0
                && currentAmount + incomingAmount <= TANK_CAPACITY_MB
                && (currentAmount <= 0 || currentFluid == incomingFluid);
    }

    private void sanitizeTanks() {
        inputAmountMb = clampAmount(inputAmountMb);
        outputAmountMb = clampAmount(outputAmountMb);
        if (inputAmountMb == 0) {
            inputFluid = Ic2FluidKind.EMPTY;
        }
        if (outputAmountMb == 0) {
            outputFluid = Ic2FluidKind.EMPTY;
        }
    }

    private static int clampAmount(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(TANK_CAPACITY_MB, value);
    }
}
