package com.shipovskijkorp.ic2modernadapter.menu;

import com.shipovskijkorp.ic2modernadapter.furnace.AbstractElectricFurnaceBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.AbstractElectricMachineBlockEntity;
import com.shipovskijkorp.ic2modernadapter.furnace.FurnaceSpec;
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

/** IC2 Electric Furnace menu using the original slot layout. */
public final class ElectricFurnaceMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = AbstractElectricFurnaceBlockEntity.SLOT_COUNT;
    public static final int DATA_COUNT = AbstractElectricFurnaceBlockEntity.DATA_COUNT;

    private final Container container;
    private final ContainerData data;

    public ElectricFurnaceMenu(MenuType<?> type, int containerId, Inventory playerInventory) {
        this(type, containerId, playerInventory, new SimpleContainer(SLOT_COUNT), defaultClientData());
    }

    public ElectricFurnaceMenu(
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

        addSlot(new Slot(container, AbstractElectricFurnaceBlockEntity.SLOT_INPUT, 56, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AbstractElectricFurnaceBlockEntity.SLOT_INPUT, stack);
            }
        });
        addSlot(new Slot(container, AbstractElectricFurnaceBlockEntity.SLOT_OUTPUT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new Slot(container, AbstractElectricFurnaceBlockEntity.SLOT_DISCHARGE, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AbstractElectricFurnaceBlockEntity.SLOT_DISCHARGE, stack);
            }
        });
        for (int slot = 0; slot < AbstractElectricFurnaceBlockEntity.UPGRADE_SLOTS; slot++) {
            int index = AbstractElectricFurnaceBlockEntity.SLOT_UPGRADE_0 + slot;
            addSlot(new Slot(container, index, 152, 8 + slot * 18) {
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
        data.set(AbstractElectricFurnaceBlockEntity.DATA_CAPACITY, (int) FurnaceSpec.ELECTRIC.capacityEu());
        data.set(AbstractElectricFurnaceBlockEntity.DATA_MAX_PROGRESS, FurnaceSpec.ELECTRIC.operationTicks());
        return data;
    }


    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0 && container instanceof AbstractElectricFurnaceBlockEntity furnace) {
            furnace.collectExperience(player);
            return true;
        }
        return super.clickMenuButton(player, id);
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
        } else if (container.canPlaceItem(AbstractElectricFurnaceBlockEntity.SLOT_INPUT, source)) {
            if (!moveItemStackTo(source, AbstractElectricFurnaceBlockEntity.SLOT_INPUT, AbstractElectricFurnaceBlockEntity.SLOT_INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (container.canPlaceItem(AbstractElectricFurnaceBlockEntity.SLOT_DISCHARGE, source)) {
            if (!moveItemStackTo(source, AbstractElectricFurnaceBlockEntity.SLOT_DISCHARGE, AbstractElectricFurnaceBlockEntity.SLOT_DISCHARGE + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (AbstractElectricMachineBlockEntity.isUpgradeStack(source)) {
            if (!moveItemStackTo(source, AbstractElectricFurnaceBlockEntity.SLOT_UPGRADE_0,
                    AbstractElectricFurnaceBlockEntity.SLOT_UPGRADE_0 + AbstractElectricFurnaceBlockEntity.UPGRADE_SLOTS, false)) {
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
        return data.get(AbstractElectricFurnaceBlockEntity.DATA_ENERGY);
    }

    public int getEuCapacity() {
        return data.get(AbstractElectricFurnaceBlockEntity.DATA_CAPACITY);
    }

    public int getProgress() {
        return data.get(AbstractElectricFurnaceBlockEntity.DATA_PROGRESS);
    }

    public int getMaxProgress() {
        return Math.max(1, data.get(AbstractElectricFurnaceBlockEntity.DATA_MAX_PROGRESS));
    }

    public int getStoredXp() {
        return data.get(AbstractElectricFurnaceBlockEntity.DATA_STORED_XP);
    }
}
