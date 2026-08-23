package com.shipovskijkorp.ic2modernadapter.toolbox;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Original IC2 Tool Box inventory menu: 9 stored tool slots plus the player inventory. */
public final class ToolBoxMenu extends AbstractContainerMenu {
    public static final int TOOLBOX_SLOTS = 9;
    private static final String ROOT_TAG = "ic2ma_toolbox";

    private final SimpleContainer toolbox;
    private final ItemStack toolboxStack;

    public ToolBoxMenu(int containerId, Inventory playerInventory) {
        this(ToolBoxPlatform.menuType(), containerId, playerInventory, ItemStack.EMPTY);
    }

    public ToolBoxMenu(MenuType<?> type, int containerId, Inventory playerInventory, ItemStack toolboxStack) {
        super(type, containerId);
        this.toolboxStack = toolboxStack;
        this.toolbox = new SimpleContainer(TOOLBOX_SLOTS);
        load(toolboxStack);

        for (int column = 0; column < TOOLBOX_SLOTS; column++) {
            addSlot(new Slot(toolbox, column, 8 + column * 18, 41) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return ToolBoxItem.isAllowed(stack);
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
    }

    @Override
    public boolean stillValid(Player player) {
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
        if (index < TOOLBOX_SLOTS) {
            if (!moveItemStackTo(source, TOOLBOX_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ToolBoxItem.isAllowed(source)) {
            if (!moveItemStackTo(source, 0, TOOLBOX_SLOTS, false)) {
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
        save();
    }

    private void load(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return;
        }
        CompoundTag root = stack.getTag().getCompound(ROOT_TAG);
        NonNullList<ItemStack> items = NonNullList.withSize(TOOLBOX_SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(root, items);
        for (int i = 0; i < TOOLBOX_SLOTS; i++) {
            toolbox.setItem(i, items.get(i));
        }
    }

    private void save() {
        if (toolboxStack.isEmpty()) {
            return;
        }
        NonNullList<ItemStack> items = NonNullList.withSize(TOOLBOX_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < TOOLBOX_SLOTS; i++) {
            items.set(i, toolbox.getItem(i));
        }
        CompoundTag root = new CompoundTag();
        ContainerHelper.saveAllItems(root, items);
        toolboxStack.getOrCreateTag().put(ROOT_TAG, root);
    }
}
