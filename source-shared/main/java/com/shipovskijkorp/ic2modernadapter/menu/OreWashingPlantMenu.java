package com.shipovskijkorp.ic2modernadapter.menu;

import com.shipovskijkorp.ic2modernadapter.machine.AbstractElectricMachineBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.AbstractOreWashingPlantBlockEntity;
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

/** Original IC2 Ore Washing Plant menu layout. */
public final class OreWashingPlantMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = AbstractOreWashingPlantBlockEntity.ORE_WASHING_SLOT_COUNT;
    public static final int DATA_COUNT = AbstractElectricMachineBlockEntity.DATA_COUNT + 1;

    private final Container container;
    private final ContainerData data;

    public OreWashingPlantMenu(MenuType<?> type, int containerId, Inventory playerInventory) {
        this(type, containerId, playerInventory, new SimpleContainer(SLOT_COUNT), defaultClientData());
    }

    public OreWashingPlantMenu(
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

        addSlot(new Slot(container, AbstractElectricMachineBlockEntity.SLOT_INPUT, 104, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AbstractElectricMachineBlockEntity.SLOT_INPUT, stack);
            }
        });
        addSlot(new Slot(container, AbstractOreWashingPlantBlockEntity.SLOT_FLUID, 38, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AbstractOreWashingPlantBlockEntity.SLOT_FLUID, stack);
            }
        });
        addSlot(new Slot(container, AbstractElectricMachineBlockEntity.SLOT_DISCHARGE, 11, 54) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AbstractElectricMachineBlockEntity.SLOT_DISCHARGE, stack);
            }
        });
        addSlot(new Slot(container, AbstractOreWashingPlantBlockEntity.SLOT_CELL, 38, 62) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        int[] outputs = {AbstractElectricMachineBlockEntity.SLOT_OUTPUT,
                AbstractOreWashingPlantBlockEntity.SLOT_OUTPUT_1,
                AbstractOreWashingPlantBlockEntity.SLOT_OUTPUT_2};
        for (int i = 0; i < outputs.length; i++) {
            int index = outputs[i];
            addSlot(new Slot(container, index, 86 + i * 18, 62) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
        for (int slot = 0; slot < AbstractElectricMachineBlockEntity.UPGRADE_SLOTS; slot++) {
            int index = AbstractElectricMachineBlockEntity.SLOT_UPGRADE_0 + slot;
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
        data.set(AbstractElectricMachineBlockEntity.DATA_CAPACITY, (int) MachineSpec.ORE_WASHING_PLANT.capacityEu());
        data.set(AbstractElectricMachineBlockEntity.DATA_MAX_PROGRESS, MachineSpec.ORE_WASHING_PLANT.operationTicks());
        data.set(AbstractElectricMachineBlockEntity.DATA_MACHINE, MachineSpec.ORE_WASHING_PLANT.ordinal());
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
        } else if (container.canPlaceItem(AbstractOreWashingPlantBlockEntity.SLOT_FLUID, source)) {
            if (!moveItemStackTo(source, AbstractOreWashingPlantBlockEntity.SLOT_FLUID,
                    AbstractOreWashingPlantBlockEntity.SLOT_FLUID + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (container.canPlaceItem(AbstractElectricMachineBlockEntity.SLOT_INPUT, source)) {
            if (!moveItemStackTo(source, AbstractElectricMachineBlockEntity.SLOT_INPUT,
                    AbstractElectricMachineBlockEntity.SLOT_INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (container.canPlaceItem(AbstractElectricMachineBlockEntity.SLOT_DISCHARGE, source)) {
            if (!moveItemStackTo(source, AbstractElectricMachineBlockEntity.SLOT_DISCHARGE,
                    AbstractElectricMachineBlockEntity.SLOT_DISCHARGE + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (AbstractElectricMachineBlockEntity.isUpgradeStack(source)) {
            if (!moveItemStackTo(source, AbstractElectricMachineBlockEntity.SLOT_UPGRADE_0,
                    AbstractElectricMachineBlockEntity.SLOT_UPGRADE_0 + AbstractElectricMachineBlockEntity.UPGRADE_SLOTS, false)) {
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

    public int getWaterMb() {
        return data.get(AbstractElectricMachineBlockEntity.DATA_COUNT);
    }

    public int getWaterCapacityMb() {
        return AbstractOreWashingPlantBlockEntity.WATER_CAPACITY_MB;
    }
}
