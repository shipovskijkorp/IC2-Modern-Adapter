package com.shipovskijkorp.ic2modernadapter.menu;

import com.shipovskijkorp.ic2modernadapter.energy.storage.AbstractEuStorageBlockEntity;
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

/** Shared menu for BatBox/CESU/MFE/MFSU. */
public final class EuStorageMenu extends AbstractContainerMenu {
    public static final int BUTTON_CYCLE_REDSTONE = 0;

    private final Container container;
    private final ContainerData data;

    public EuStorageMenu(MenuType<?> type, int containerId, Inventory playerInventory) {
        this(type, containerId, playerInventory,
                new SimpleContainer(AbstractEuStorageBlockEntity.SLOT_COUNT),
                new SimpleContainerData(AbstractEuStorageBlockEntity.DATA_COUNT));
    }

    public EuStorageMenu(
            MenuType<?> type,
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerData data) {
        super(type, containerId);
        checkContainerSize(container, AbstractEuStorageBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, AbstractEuStorageBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;
        container.startOpen(playerInventory.player);

        // ContainerElectricBlock: charge at 56/17, discharge at 56/53.
        addSlot(new Slot(container, AbstractEuStorageBlockEntity.SLOT_CHARGE, 56, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AbstractEuStorageBlockEntity.SLOT_CHARGE, stack);
            }
        });
        addSlot(new Slot(container, AbstractEuStorageBlockEntity.SLOT_DISCHARGE, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AbstractEuStorageBlockEntity.SLOT_DISCHARGE, stack);
            }
        });

        // Original ContainerFullInv layout for a 196-pixel-tall GUI.
        int inventoryX = 8;
        int inventoryY = 114;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        inventoryX + column * 18,
                        inventoryY + row * 18));
            }
        }
        int hotbarY = 172;
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, inventoryX + column * 18, hotbarY));
        }

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_CYCLE_REDSTONE) {
            return false;
        }
        if (container instanceof AbstractEuStorageBlockEntity storage) {
            storage.cycleRedstoneMode(player);
            return true;
        }
        return true;
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
        int machineSlots = AbstractEuStorageBlockEntity.SLOT_COUNT;
        if (index < machineSlots) {
            if (!moveItemStackTo(source, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (container.canPlaceItem(AbstractEuStorageBlockEntity.SLOT_CHARGE, source)) {
            if (!moveItemStackTo(
                    source,
                    AbstractEuStorageBlockEntity.SLOT_CHARGE,
                    AbstractEuStorageBlockEntity.SLOT_CHARGE + 1,
                    false)) {
                return ItemStack.EMPTY;
            }
        } else if (container.canPlaceItem(AbstractEuStorageBlockEntity.SLOT_DISCHARGE, source)) {
            if (!moveItemStackTo(
                    source,
                    AbstractEuStorageBlockEntity.SLOT_DISCHARGE,
                    AbstractEuStorageBlockEntity.SLOT_DISCHARGE + 1,
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
        return data.get(AbstractEuStorageBlockEntity.DATA_ENERGY);
    }

    public int getEuCapacity() {
        return data.get(AbstractEuStorageBlockEntity.DATA_CAPACITY);
    }

    public int getOutputEuPerTick() {
        return data.get(AbstractEuStorageBlockEntity.DATA_OUTPUT);
    }

    public int getRedstoneMode() {
        return data.get(AbstractEuStorageBlockEntity.DATA_REDSTONE_MODE);
    }

    public int getTier() {
        return data.get(AbstractEuStorageBlockEntity.DATA_TIER);
    }

    public int containerId() {
        return containerId;
    }
}
