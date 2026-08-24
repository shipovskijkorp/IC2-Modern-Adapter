package com.shipovskijkorp.ic2modernadapter.menu;

import com.shipovskijkorp.ic2modernadapter.machine.AbstractCannerBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.AbstractElectricMachineBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.CannerMode;
import com.shipovskijkorp.ic2modernadapter.machine.Ic2FluidKind;
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

/** Original IC2 Fluid/Solid Canning Machine menu layout. */
public final class CannerMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = AbstractCannerBlockEntity.CANNER_SLOT_COUNT;
    public static final int DATA_COUNT = AbstractElectricMachineBlockEntity.DATA_COUNT + 5;
    public static final int BUTTON_MODE_BASE = 0;
    public static final int BUTTON_SWAP_TANKS = BUTTON_MODE_BASE + CannerMode.values().length + 1;

    private final Container container;
    private final ContainerData data;
    private final AbstractCannerBlockEntity blockEntity;

    public CannerMenu(MenuType<?> type, int containerId, Inventory playerInventory) {
        this(type, containerId, playerInventory, new SimpleContainer(SLOT_COUNT), defaultClientData(), null);
    }

    public CannerMenu(MenuType<?> type, int containerId, Inventory playerInventory, Container container, ContainerData data) {
        this(type, containerId, playerInventory, container, data, container instanceof AbstractCannerBlockEntity canner ? canner : null);
    }

    private CannerMenu(MenuType<?> type, int containerId, Inventory playerInventory, Container container, ContainerData data, AbstractCannerBlockEntity blockEntity) {
        super(type, containerId);
        checkContainerSize(container, SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        this.blockEntity = blockEntity;
        container.startOpen(playerInventory.player);

        addSlot(new Slot(container, AbstractCannerBlockEntity.SLOT_CAN_INPUT, 41, 17) {
            @Override public boolean mayPlace(ItemStack stack) { return container.canPlaceItem(AbstractCannerBlockEntity.SLOT_CAN_INPUT, stack); }
        });
        addSlot(new Slot(container, AbstractElectricMachineBlockEntity.SLOT_INPUT, 80, 44) {
            @Override public boolean mayPlace(ItemStack stack) { return container.canPlaceItem(AbstractElectricMachineBlockEntity.SLOT_INPUT, stack); }
        });
        addSlot(new Slot(container, AbstractElectricMachineBlockEntity.SLOT_OUTPUT, 119, 17) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        addSlot(new Slot(container, AbstractElectricMachineBlockEntity.SLOT_DISCHARGE, 8, 80) {
            @Override public boolean mayPlace(ItemStack stack) { return container.canPlaceItem(AbstractElectricMachineBlockEntity.SLOT_DISCHARGE, stack); }
        });
        for (int slot = 0; slot < AbstractElectricMachineBlockEntity.UPGRADE_SLOTS; slot++) {
            int index = AbstractElectricMachineBlockEntity.SLOT_UPGRADE_0 + slot;
            addSlot(new Slot(container, index, 152, 26 + slot * 18) {
                @Override public boolean mayPlace(ItemStack stack) { return container.canPlaceItem(index, stack); }
            });
        }

        int startX = 8;
        int startY = 102;
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
        data.set(AbstractElectricMachineBlockEntity.DATA_CAPACITY, (int) MachineSpec.CANNER.capacityEu());
        data.set(AbstractElectricMachineBlockEntity.DATA_MAX_PROGRESS, MachineSpec.CANNER.operationTicks());
        data.set(AbstractElectricMachineBlockEntity.DATA_MACHINE, MachineSpec.CANNER.ordinal());
        data.set(AbstractElectricMachineBlockEntity.DATA_COUNT, CannerMode.BOTTLE_SOLID.id());
        return data;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (blockEntity != null) {
            if (id >= BUTTON_MODE_BASE && id < BUTTON_MODE_BASE + CannerMode.values().length) {
                blockEntity.setMode(CannerMode.byId(id - BUTTON_MODE_BASE));
                return true;
            }
            if (id == BUTTON_SWAP_TANKS) {
                blockEntity.swapTanks();
                return true;
            }
        }
        return super.clickMenuButton(player, id);
    }

    @Override public boolean stillValid(Player player) { return container.stillValid(player); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        if (index < SLOT_COUNT) {
            if (!moveItemStackTo(source, SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else if (container.canPlaceItem(AbstractCannerBlockEntity.SLOT_CAN_INPUT, source)) {
            if (!moveItemStackTo(source, AbstractCannerBlockEntity.SLOT_CAN_INPUT, AbstractCannerBlockEntity.SLOT_CAN_INPUT + 1, false)) return ItemStack.EMPTY;
        } else if (container.canPlaceItem(AbstractElectricMachineBlockEntity.SLOT_INPUT, source)) {
            if (!moveItemStackTo(source, AbstractElectricMachineBlockEntity.SLOT_INPUT, AbstractElectricMachineBlockEntity.SLOT_INPUT + 1, false)) return ItemStack.EMPTY;
        } else if (container.canPlaceItem(AbstractElectricMachineBlockEntity.SLOT_DISCHARGE, source)) {
            if (!moveItemStackTo(source, AbstractElectricMachineBlockEntity.SLOT_DISCHARGE, AbstractElectricMachineBlockEntity.SLOT_DISCHARGE + 1, false)) return ItemStack.EMPTY;
        } else if (AbstractElectricMachineBlockEntity.isUpgradeStack(source)) {
            if (!moveItemStackTo(source, AbstractElectricMachineBlockEntity.SLOT_UPGRADE_0, AbstractElectricMachineBlockEntity.SLOT_UPGRADE_0 + AbstractElectricMachineBlockEntity.UPGRADE_SLOTS, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (source.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, source);
        return original;
    }

    @Override public void removed(Player player) { super.removed(player); container.stopOpen(player); }

    public int getEuStored() { return data.get(AbstractElectricMachineBlockEntity.DATA_ENERGY); }
    public int getEuCapacity() { return data.get(AbstractElectricMachineBlockEntity.DATA_CAPACITY); }
    public int getProgress() { return data.get(AbstractElectricMachineBlockEntity.DATA_PROGRESS); }
    public int getMaxProgress() { return Math.max(1, data.get(AbstractElectricMachineBlockEntity.DATA_MAX_PROGRESS)); }
    public CannerMode getMode() { return CannerMode.byId(data.get(AbstractElectricMachineBlockEntity.DATA_COUNT)); }
    public int getInputTankAmount() { return data.get(AbstractElectricMachineBlockEntity.DATA_COUNT + 1); }
    public int getOutputTankAmount() { return data.get(AbstractElectricMachineBlockEntity.DATA_COUNT + 2); }
    public Ic2FluidKind getInputTankFluid() { return Ic2FluidKind.byId(data.get(AbstractElectricMachineBlockEntity.DATA_COUNT + 3)); }
    public Ic2FluidKind getOutputTankFluid() { return Ic2FluidKind.byId(data.get(AbstractElectricMachineBlockEntity.DATA_COUNT + 4)); }
    public int getTankCapacity() { return AbstractCannerBlockEntity.TANK_CAPACITY_MB; }
}
