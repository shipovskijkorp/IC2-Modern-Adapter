package com.shipovskijkorp.ic2modernadapter.furnace;

import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantFacingBlock;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import com.shipovskijkorp.ic2modernadapter.menu.IronFurnaceMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Loader-neutral implementation of the original fuel-burning IC2 Iron Furnace. */
public abstract class AbstractIronFurnaceBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

    public static final int DATA_FUEL = 0;
    public static final int DATA_TOTAL_FUEL = 1;
    public static final int DATA_PROGRESS = 2;
    public static final int DATA_MAX_PROGRESS = 3;
    public static final int DATA_COUNT = 4;

    private static final int[] TOP_SLOTS = {SLOT_INPUT};
    private static final int[] SIDE_SLOTS = {SLOT_FUEL, SLOT_INPUT};
    private static final int[] BOTTOM_SLOTS = {SLOT_OUTPUT};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int fuel;
    private int totalFuel;
    private int progress;
    private double storedXp;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_FUEL -> AbstractIronFurnaceBlockEntity.this.fuel;
                case DATA_TOTAL_FUEL -> AbstractIronFurnaceBlockEntity.this.totalFuel;
                case DATA_PROGRESS -> AbstractIronFurnaceBlockEntity.this.progress;
                case DATA_MAX_PROGRESS -> FurnaceSpec.IRON.operationTicks();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_FUEL -> AbstractIronFurnaceBlockEntity.this.fuel = Math.max(0, value);
                case DATA_TOTAL_FUEL -> AbstractIronFurnaceBlockEntity.this.totalFuel = Math.max(0, value);
                case DATA_PROGRESS -> AbstractIronFurnaceBlockEntity.this.progress = Math.max(0, value);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    protected AbstractIronFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        FurnaceSpec stateSpec = FurnaceSpec.fromBlockState(state);
        if (stateSpec != null && stateSpec != FurnaceSpec.IRON) {
            throw new IllegalArgumentException("Iron furnace block entity does not match block state " + stateSpec.variantKey());
        }
    }

    protected abstract @Nullable SmeltingRecipeMatch findSmeltingRecipe(ItemStack input);

    protected abstract MenuType<?> ironFurnaceMenuType();

    public final void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        boolean changed = false;
        boolean canOperate = canOperate();
        if (fuel <= 0 && canOperate) {
            int burn = consumeFuel();
            if (burn > 0) {
                fuel = burn;
                totalFuel = burn;
                changed = true;
            }
        }

        if (fuel > 0 && canOperate) {
            progress++;
            if (progress >= FurnaceSpec.IRON.operationTicks()) {
                progress = 0;
                operate();
                changed = true;
            }
        } else if (progress != 0) {
            progress = 0;
            changed = true;
        }

        if (fuel > 0) {
            fuel--;
            setActive(true);
        } else {
            setActive(false);
        }

        if (changed) {
            setChanged();
        }
    }

    private int consumeFuel() {
        ItemStack fuelStack = items.get(SLOT_FUEL);
        int burn = FurnaceFuelHooks.getBurnTime(fuelStack);
        if (burn <= 0) {
            return 0;
        }
        ItemStack remainder = FurnaceFuelHooks.getCraftingRemainder(fuelStack.copy());
        fuelStack.shrink(1);
        if (fuelStack.isEmpty()) {
            items.set(SLOT_FUEL, remainder);
        } else if (!remainder.isEmpty()) {
            // Match vanilla container behavior as closely as this simple one-slot inventory allows.
            FurnaceInventoryUtil.insertOutput(items, SLOT_OUTPUT, remainder);
        }
        return burn;
    }

    private boolean canOperate() {
        ItemStack input = items.get(SLOT_INPUT);
        SmeltingRecipeMatch match = findSmeltingRecipe(input);
        return match != null && !match.isEmpty() && FurnaceInventoryUtil.canOutput(items.get(SLOT_OUTPUT), match.output());
    }

    private void operate() {
        ItemStack input = items.get(SLOT_INPUT);
        SmeltingRecipeMatch match = findSmeltingRecipe(input);
        if (match == null || match.isEmpty() || !FurnaceInventoryUtil.canOutput(items.get(SLOT_OUTPUT), match.output())) {
            return;
        }
        input.shrink(1);
        FurnaceInventoryUtil.insertOutput(items, SLOT_OUTPUT, match.output());
        storedXp += Math.max(0.0F, match.experience());
    }


    public final void collectExperience(Player player) {
        Level level = getLevel();
        if (!(level instanceof ServerLevel serverLevel) || player == null || storedXp < 1.0D) {
            return;
        }
        double awardable = Math.floor(storedXp);
        int wholeXp = (int) Math.min(Integer.MAX_VALUE, awardable);
        if (wholeXp <= 0) {
            return;
        }
        ExperienceOrb.award(serverLevel, player.position().add(0.0D, 0.5D, 0.0D), wholeXp);
        storedXp -= wholeXp;
        if (storedXp < 0.0D) {
            storedXp = 0.0D;
        }
        setChanged();
    }

    protected final void saveIronFurnaceState(CompoundTag tag) {
        tag.putInt("fuel", Math.max(0, fuel));
        tag.putInt("totalFuel", Math.max(0, totalFuel));
        tag.putInt("progress", Math.max(0, progress));
        tag.putDouble("xp", Math.max(0.0D, storedXp));
    }

    protected final void loadIronFurnaceState(CompoundTag tag) {
        fuel = Math.max(0, tag.getInt("fuel"));
        totalFuel = Math.max(0, tag.getInt("totalFuel"));
        progress = Math.max(0, tag.getInt("progress"));
        storedXp = Math.max(0.0D, tag.getDouble("xp"));
    }

    protected final NonNullList<ItemStack> mutableItems() {
        return items;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(FurnaceSpec.IRON.translationKey());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new IronFurnaceMenu(ironFurnaceMenuType(), containerId, inventory, this, data);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        Level level = getLevel();
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        double dx = player.getX() - (worldPosition.getX() + 0.5D);
        double dy = player.getY() - (worldPosition.getY() + 0.5D);
        double dz = player.getZ() - (worldPosition.getZ() + 0.5D);
        return dx * dx + dy * dy + dz * dz <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < items.size(); slot++) {
            items.set(slot, ItemStack.EMPTY);
        }
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return switch (slot) {
            case SLOT_INPUT -> getLevel() == null || findSmeltingRecipe(stack) != null;
            case SLOT_FUEL -> FurnaceFuelHooks.isFuel(stack);
            default -> false;
        };
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP) {
            return TOP_SLOTS.clone();
        }
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS.clone();
        }
        return SIDE_SLOTS.clone();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack)
                && Arrays.stream(getSlotsForFace(side == null ? Direction.NORTH : side)).anyMatch(value -> value == slot);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == SLOT_OUTPUT && side == Direction.DOWN;
    }

    private void setActive(boolean active) {
        Level level = getLevel();
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(LegacyVariantFacingBlock.ACTIVE)
                || state.getValue(LegacyVariantFacingBlock.ACTIVE) == active) {
            return;
        }
        level.setBlock(worldPosition, state.setValue(LegacyVariantFacingBlock.ACTIVE, active), 3);
    }
}
