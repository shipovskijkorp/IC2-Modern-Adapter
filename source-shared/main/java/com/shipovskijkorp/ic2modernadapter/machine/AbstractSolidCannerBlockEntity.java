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

/** Loader-neutral implementation of IC2's TileEntitySolidCanner. */
public abstract class AbstractSolidCannerBlockEntity extends AbstractElectricMachineBlockEntity {
    public static final int SLOT_CAN_INPUT = AbstractElectricMachineBlockEntity.SLOT_COUNT;
    public static final int SOLID_CANNER_SLOT_COUNT = SLOT_CAN_INPUT + 1;

    private static final int[] TOP_SLOTS = {SLOT_INPUT, SLOT_CAN_INPUT};
    private static final int[] SIDE_SLOTS = {SLOT_INPUT, SLOT_CAN_INPUT, SLOT_DISCHARGE, SLOT_UPGRADE_0, SLOT_UPGRADE_0 + 1, SLOT_UPGRADE_0 + 2, SLOT_UPGRADE_0 + 3};
    private static final int[] BOTTOM_SLOTS = {SLOT_OUTPUT};

    private String activeRecipeId = "";

    protected AbstractSolidCannerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, MachineSpec.SOLID_CANNER, pos, state, SOLID_CANNER_SLOT_COUNT);
    }

    public final void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        boolean changed = chargeFromDischargeSlot();
        boolean active = process();
        if (changed || active) {
            setChanged();
        }
        setMachineActive(active);
    }

    private boolean process() {
        ItemStack container = getItem(SLOT_CAN_INPUT);
        ItemStack fill = getItem(SLOT_INPUT);
        CannerBottleRecipeDefinition recipe = CannerRecipeRegistry.findBottleRecipe(container, fill);
        if (recipe == null) {
            if (progress != 0 || !activeRecipeId.isEmpty()) {
                resetSolidCannerProgress();
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
            resetSolidCannerProgress();
        }
        return true;
    }

    private void resetSolidCannerProgress() {
        resetProgress();
        activeRecipeId = "";
    }

    protected final void saveSolidCannerState(CompoundTag tag) {
        saveMachineState(tag);
        tag.putString("activeRecipeId", activeRecipeId);
    }

    protected final void loadSolidCannerState(CompoundTag tag) {
        loadMachineState(tag);
        activeRecipeId = tag.getString("activeRecipeId");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new com.shipovskijkorp.ic2modernadapter.menu.SolidCannerMenu(
                machineMenuType(), containerId, inventory, this, menuData());
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return switch (slot) {
            case SLOT_INPUT -> CannerRecipeRegistry.acceptsBottleFill(stack, getItem(SLOT_CAN_INPUT));
            case SLOT_CAN_INPUT -> CannerRecipeRegistry.acceptsBottleContainer(stack, getItem(SLOT_INPUT));
            case SLOT_DISCHARGE -> EuStorageItemHooks.canDischarge(stack, spec().tier());
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
        return side == Direction.DOWN && slot == SLOT_OUTPUT;
    }
}
