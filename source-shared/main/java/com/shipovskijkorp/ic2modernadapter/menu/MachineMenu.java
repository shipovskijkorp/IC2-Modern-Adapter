package com.shipovskijkorp.ic2modernadapter.menu;

import com.shipovskijkorp.ic2modernadapter.machine.AbstractElectricMachineBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.MachineSpec;
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

/** Shared ContainerStandardMachine-style menu for Macerator and Compressor. */
public final class MachineMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = AbstractElectricMachineBlockEntity.SLOT_COUNT;
    public static final int DATA_COUNT = AbstractElectricMachineBlockEntity.DATA_COUNT;

    private final Container container;
    private final ContainerData data;

    public MachineMenu(MenuType<?> type, int containerId, Inventory playerInventory) {
        this(type, containerId, playerInventory, MachineSpec.MACERATOR);
    }

    public MachineMenu(MenuType<?> type, int containerId, Inventory playerInventory, MachineSpec machineSpec) {
        this(type, containerId, playerInventory,
                new SimpleContainer(SLOT_COUNT),
                defaultClientData(machineSpec));
    }

    public MachineMenu(
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

        // IC2 guidef standard-machine layout. Slot positions are frame coordinates + 1.
        addSlot(new Slot(container, AbstractElectricMachineBlockEntity.SLOT_INPUT, 56, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AbstractElectricMachineBlockEntity.SLOT_INPUT, stack);
            }
        });
        addSlot(new Slot(container, AbstractElectricMachineBlockEntity.SLOT_OUTPUT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new Slot(container, AbstractElectricMachineBlockEntity.SLOT_DISCHARGE, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AbstractElectricMachineBlockEntity.SLOT_DISCHARGE, stack);
            }
        });
        for (int slot = 0; slot < AbstractElectricMachineBlockEntity.UPGRADE_SLOTS; slot++) {
            int index = AbstractElectricMachineBlockEntity.SLOT_UPGRADE_0 + slot;
            addSlot(new Slot(container, index, 152, 8 + slot * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return container.canPlaceItem(index, stack);
                }
            });
        }

        int startX = 7;
        int startY = 83;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        startX + column * 18,
                        startY + row * 18));
            }
        }
        int hotbarY = startY + 58;
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, startX + column * 18, hotbarY));
        }
        addDataSlots(data);
    }


    private static ContainerData defaultClientData(MachineSpec machineSpec) {
        SimpleContainerData data = new SimpleContainerData(DATA_COUNT);
        MachineSpec safeSpec = machineSpec == null ? MachineSpec.MACERATOR : machineSpec;
        data.set(AbstractElectricMachineBlockEntity.DATA_CAPACITY, (int) Math.min(Integer.MAX_VALUE, safeSpec.capacityEu()));
        data.set(AbstractElectricMachineBlockEntity.DATA_MAX_PROGRESS, safeSpec.operationTicks());
        data.set(AbstractElectricMachineBlockEntity.DATA_MACHINE, safeSpec.ordinal());
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
        } else if (container.canPlaceItem(AbstractElectricMachineBlockEntity.SLOT_INPUT, source)) {
            if (!moveItemStackTo(
                    source,
                    AbstractElectricMachineBlockEntity.SLOT_INPUT,
                    AbstractElectricMachineBlockEntity.SLOT_INPUT + 1,
                    false)) {
                return ItemStack.EMPTY;
            }
        } else if (container.canPlaceItem(AbstractElectricMachineBlockEntity.SLOT_DISCHARGE, source)) {
            if (!moveItemStackTo(
                    source,
                    AbstractElectricMachineBlockEntity.SLOT_DISCHARGE,
                    AbstractElectricMachineBlockEntity.SLOT_DISCHARGE + 1,
                    false)) {
                return ItemStack.EMPTY;
            }
        } else if (AbstractElectricMachineBlockEntity.isUpgradeStack(source)) {
            if (!moveItemStackTo(
                    source,
                    AbstractElectricMachineBlockEntity.SLOT_UPGRADE_0,
                    AbstractElectricMachineBlockEntity.SLOT_UPGRADE_0 + AbstractElectricMachineBlockEntity.UPGRADE_SLOTS,
                    false)) {
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
        return data.get(AbstractElectricMachineBlockEntity.DATA_ENERGY);
    }

    public int getEuCapacity() {
        return data.get(AbstractElectricMachineBlockEntity.DATA_CAPACITY);
    }

    public int getProgress() {
        return data.get(AbstractElectricMachineBlockEntity.DATA_PROGRESS);
    }

    public int getMaxProgress() {
        return Math.max(1, data.get(AbstractElectricMachineBlockEntity.DATA_MAX_PROGRESS));
    }

    public MachineSpec getMachineSpec() {
        int ordinal = data.get(AbstractElectricMachineBlockEntity.DATA_MACHINE);
        MachineSpec[] values = MachineSpec.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : MachineSpec.MACERATOR;
    }
}
