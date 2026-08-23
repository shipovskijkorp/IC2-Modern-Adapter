package com.shipovskijkorp.ic2modernadapter.menu;

import com.shipovskijkorp.ic2modernadapter.furnace.AbstractInductionFurnaceBlockEntity;
import com.shipovskijkorp.ic2modernadapter.furnace.FurnaceSpec;
import com.shipovskijkorp.ic2modernadapter.machine.AbstractElectricMachineBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** IC2 Induction Furnace menu using the original dual-input/dual-output layout. */
public final class InductionFurnaceMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = AbstractInductionFurnaceBlockEntity.SLOT_COUNT;
    public static final int DATA_COUNT = AbstractInductionFurnaceBlockEntity.DATA_COUNT;

    private final Container container;
    private final ContainerData data;

    public InductionFurnaceMenu(MenuType<?> type, int containerId, Inventory playerInventory) {
        this(type, containerId, playerInventory, new SimpleContainer(SLOT_COUNT), defaultClientData());
    }

    public InductionFurnaceMenu(
            MenuType<?> type,
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerData data) {
        super(type, containerId);
        checkContainerSize(container, SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        container.startOpen(playerInventory.player);

        addSlot(new Slot(container, AbstractInductionFurnaceBlockEntity.SLOT_INPUT_A, 43, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AbstractInductionFurnaceBlockEntity.SLOT_INPUT_A, stack);
            }
        });
        addSlot(new Slot(container, AbstractInductionFurnaceBlockEntity.SLOT_INPUT_B, 59, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AbstractInductionFurnaceBlockEntity.SLOT_INPUT_B, stack);
            }
        });
        addSlot(new Slot(container, AbstractInductionFurnaceBlockEntity.SLOT_OUTPUT_A, 113, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new Slot(container, AbstractInductionFurnaceBlockEntity.SLOT_OUTPUT_B, 129, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new Slot(container, AbstractInductionFurnaceBlockEntity.SLOT_DISCHARGE, 51, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AbstractInductionFurnaceBlockEntity.SLOT_DISCHARGE, stack);
            }
        });
        for (int slot = 0; slot < AbstractInductionFurnaceBlockEntity.UPGRADE_SLOTS; slot++) {
            int index = AbstractInductionFurnaceBlockEntity.SLOT_UPGRADE_0 + slot;
            addSlot(new Slot(container, index, 152, 26 + slot * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return container.canPlaceItem(index, stack);
                }
            });
        }

        int startX = 8;
        int startY = 84;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, startX + column * 18, startY + row * 18));
            }
        }
        int hotbarY = startY + 58;
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, startX + column * 18, hotbarY));
        }
        addDataSlots(data);
    }

    private static ContainerData defaultClientData() {
        SimpleContainerData data = new SimpleContainerData(DATA_COUNT);
        data.set(AbstractInductionFurnaceBlockEntity.DATA_CAPACITY, (int) FurnaceSpec.INDUCTION.capacityEu());
        data.set(AbstractInductionFurnaceBlockEntity.DATA_MAX_HEAT, FurnaceSpec.INDUCTION_MAX_HEAT);
        data.set(AbstractInductionFurnaceBlockEntity.DATA_MAX_PROGRESS, FurnaceSpec.INDUCTION.operationTicks());
        return data;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        if (index < SLOT_COUNT) {
            if (!moveItemStackTo(source, SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (container.canPlaceItem(AbstractInductionFurnaceBlockEntity.SLOT_INPUT_A, source)) {
            if (!moveItemStackTo(source, AbstractInductionFurnaceBlockEntity.SLOT_INPUT_A,
                    AbstractInductionFurnaceBlockEntity.SLOT_INPUT_B + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (container.canPlaceItem(AbstractInductionFurnaceBlockEntity.SLOT_DISCHARGE, source)) {
            if (!moveItemStackTo(source, AbstractInductionFurnaceBlockEntity.SLOT_DISCHARGE,
                    AbstractInductionFurnaceBlockEntity.SLOT_DISCHARGE + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (AbstractElectricMachineBlockEntity.isUpgradeStack(source)) {
            if (!moveItemStackTo(source, AbstractInductionFurnaceBlockEntity.SLOT_UPGRADE_0,
                    AbstractInductionFurnaceBlockEntity.SLOT_UPGRADE_0 + AbstractInductionFurnaceBlockEntity.UPGRADE_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (source.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, source);
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    public int getEuStored() {
        return data.get(AbstractInductionFurnaceBlockEntity.DATA_ENERGY);
    }

    public int getEuCapacity() {
        return data.get(AbstractInductionFurnaceBlockEntity.DATA_CAPACITY);
    }

    public int getHeat() {
        return data.get(AbstractInductionFurnaceBlockEntity.DATA_HEAT);
    }

    public int getMaxHeat() {
        return Math.max(1, data.get(AbstractInductionFurnaceBlockEntity.DATA_MAX_HEAT));
    }

    public int getHeatPercent() {
        return getHeat() * 100 / getMaxHeat();
    }

    public int getProgress() {
        return data.get(AbstractInductionFurnaceBlockEntity.DATA_PROGRESS);
    }

    public int getMaxProgress() {
        return Math.max(1, data.get(AbstractInductionFurnaceBlockEntity.DATA_MAX_PROGRESS));
    }
}
