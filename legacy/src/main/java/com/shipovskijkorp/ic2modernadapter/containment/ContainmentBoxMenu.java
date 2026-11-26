package com.shipovskijkorp.ic2modernadapter.containment;

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

/** Original IC2 Containment Box inventory menu: 12 nuclear-resource slots plus the player inventory. */
public final class ContainmentBoxMenu extends AbstractContainerMenu {
    public static final int BOX_SLOTS = 12;
    private static final String ROOT_TAG = "ic2ma_containment_box";

    private final SimpleContainer box;
    private final ItemStack boxStack;

    public ContainmentBoxMenu(int containerId, Inventory playerInventory) {
        this(ContainmentBoxPlatform.menuType(), containerId, playerInventory, ItemStack.EMPTY);
    }

    public ContainmentBoxMenu(MenuType<?> type, int containerId, Inventory playerInventory, ItemStack boxStack) {
        super(type, containerId);
        this.boxStack = boxStack;
        this.box = new SimpleContainer(BOX_SLOTS);
        load(boxStack);
        box.addListener(container -> save());

        for (int column = 0; column < BOX_SLOTS; column++) {
            addSlot(new Slot(box, column, 53 + (column % 4) * 18, 19 + (column / 4) * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return ContainmentBoxItem.isAllowed(stack);
                }
            });
        }

        int startX = 8;
        int startY = 84;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(playerSlot(playerInventory, column + row * 9 + 9, startX + column * 18, startY + row * 18));
            }
        }
        int hotbarY = startY + 58;
        for (int column = 0; column < 9; column++) {
            addSlot(playerSlot(playerInventory, column, startX + column * 18, hotbarY));
        }
    }

    private Slot playerSlot(Inventory inventory, int index, int x, int y) {
        return new Slot(inventory, index, x, y) {
            @Override public boolean mayPickup(Player player) { return getItem() != boxStack && super.mayPickup(player); }
        };
    }
    @Override
    public boolean stillValid(Player player) {
        return boxStack.isEmpty() ? player.level().isClientSide() : player.getMainHandItem() == boxStack || player.getOffhandItem() == boxStack;
    }

    @Override
    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType type, Player player) {
        if (!stillValid(player)) return;
        if (!boxStack.isEmpty()) {
            if (slotId >= 0 && slotId < slots.size() && slots.get(slotId).getItem() == boxStack) return;
            if (type == net.minecraft.world.inventory.ClickType.SWAP && button >= 0 && button < player.getInventory().getContainerSize() && player.getInventory().getItem(button) == boxStack) return;
        }
        super.clicked(slotId, button, type, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem() || slot.getItem() == boxStack) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        if (index < BOX_SLOTS) {
            if (!moveItemStackTo(source, BOX_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ContainmentBoxItem.isAllowed(source)) {
            if (!moveItemStackTo(source, 0, BOX_SLOTS, false)) {
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
    }

    private void load(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return;
        }
        CompoundTag root = stack.getTag().getCompound(ROOT_TAG);
        NonNullList<ItemStack> items = NonNullList.withSize(BOX_SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(root, items);
        for (int i = 0; i < BOX_SLOTS; i++) {
            box.setItem(i, items.get(i));
        }
    }

    private void save() {
        if (boxStack.isEmpty()) {
            return;
        }
        NonNullList<ItemStack> items = NonNullList.withSize(BOX_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < BOX_SLOTS; i++) {
            items.set(i, box.getItem(i));
        }
        CompoundTag root = new CompoundTag();
        ContainerHelper.saveAllItems(root, items);
        boxStack.getOrCreateTag().put(ROOT_TAG, root);
    }
}
