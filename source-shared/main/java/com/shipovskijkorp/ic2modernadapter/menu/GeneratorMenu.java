package com.shipovskijkorp.ic2modernadapter.menu;

import com.shipovskijkorp.ic2modernadapter.generator.GeneratorBlockEntityBase;
import com.shipovskijkorp.ic2modernadapter.generator.GeneratorFuelHooks;
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

/** Original IC2 Generator slot layout and synced EU/fuel state. */
public final class GeneratorMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = GeneratorBlockEntityBase.SLOT_COUNT;
    public static final int DATA_COUNT = GeneratorBlockEntityBase.DATA_COUNT;

    private final Container container;
    private final ContainerData data;

    public GeneratorMenu(MenuType<?> type, int containerId, Inventory playerInventory) {
        this(type, containerId, playerInventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT));
    }

    public GeneratorMenu(
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

        // guidef/generator.xml stores the 18x18 frame at x=56/y=16 and x=56/y=52.
        addSlot(new Slot(container, GeneratorBlockEntityBase.SLOT_CHARGE, 57, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return GeneratorBlockEntityBase.isChargeItem(stack);
            }
        });
        addSlot(new Slot(container, GeneratorBlockEntityBase.SLOT_FUEL, 57, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return GeneratorFuelHooks.isFuel(stack);
            }
        });

        int startX = 8;
        int startY = 84;
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

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        if (index < SLOT_COUNT) {
            if (!moveItemStackTo(source, SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (GeneratorFuelHooks.isFuel(source)) {
            if (!moveItemStackTo(source, GeneratorBlockEntityBase.SLOT_FUEL,
                    GeneratorBlockEntityBase.SLOT_FUEL + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (GeneratorBlockEntityBase.isChargeItem(source)) {
            if (!moveItemStackTo(source, GeneratorBlockEntityBase.SLOT_CHARGE,
                    GeneratorBlockEntityBase.SLOT_CHARGE + 1, false)) {
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
        return data.get(0);
    }

    public int getEuCapacity() {
        return data.get(1);
    }

    public int getFuel() {
        return data.get(2);
    }

    public int getTotalFuel() {
        return data.get(3);
    }
}
