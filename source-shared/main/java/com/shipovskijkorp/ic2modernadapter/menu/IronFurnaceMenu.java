package com.shipovskijkorp.ic2modernadapter.menu;

import com.shipovskijkorp.ic2modernadapter.furnace.AbstractIronFurnaceBlockEntity;
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

/** IC2 Iron Furnace menu using the original IC2 slot layout. */
public final class IronFurnaceMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = AbstractIronFurnaceBlockEntity.SLOT_COUNT;
    public static final int DATA_COUNT = AbstractIronFurnaceBlockEntity.DATA_COUNT;

    private final Container container;
    private final ContainerData data;

    public IronFurnaceMenu(MenuType<?> type, int containerId, Inventory playerInventory) {
        this(type, containerId, playerInventory, new SimpleContainer(SLOT_COUNT), defaultClientData());
    }

    public IronFurnaceMenu(
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

        addSlot(new Slot(container, AbstractIronFurnaceBlockEntity.SLOT_INPUT, 56, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AbstractIronFurnaceBlockEntity.SLOT_INPUT, stack);
            }
        });
        addSlot(new Slot(container, AbstractIronFurnaceBlockEntity.SLOT_FUEL, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(AbstractIronFurnaceBlockEntity.SLOT_FUEL, stack);
            }
        });
        addSlot(new Slot(container, AbstractIronFurnaceBlockEntity.SLOT_OUTPUT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

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
        data.set(AbstractIronFurnaceBlockEntity.DATA_MAX_PROGRESS, 160);
        return data;
    }


    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0 && container instanceof AbstractIronFurnaceBlockEntity furnace) {
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
        } else if (container.canPlaceItem(AbstractIronFurnaceBlockEntity.SLOT_INPUT, source)) {
            if (!moveItemStackTo(source, AbstractIronFurnaceBlockEntity.SLOT_INPUT,
                    AbstractIronFurnaceBlockEntity.SLOT_INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (container.canPlaceItem(AbstractIronFurnaceBlockEntity.SLOT_FUEL, source)) {
            if (!moveItemStackTo(source, AbstractIronFurnaceBlockEntity.SLOT_FUEL,
                    AbstractIronFurnaceBlockEntity.SLOT_FUEL + 1, false)) {
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

    public int getFuel() {
        return data.get(AbstractIronFurnaceBlockEntity.DATA_FUEL);
    }

    public int getTotalFuel() {
        return Math.max(1, data.get(AbstractIronFurnaceBlockEntity.DATA_TOTAL_FUEL));
    }

    public int getProgress() {
        return data.get(AbstractIronFurnaceBlockEntity.DATA_PROGRESS);
    }

    public int getMaxProgress() {
        return Math.max(1, data.get(AbstractIronFurnaceBlockEntity.DATA_MAX_PROGRESS));
    }
}
