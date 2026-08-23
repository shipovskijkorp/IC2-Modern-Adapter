package com.shipovskijkorp.ic2modernadapter.furnace;

import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantFacingBlock;
import com.shipovskijkorp.ic2modernadapter.energy.api.IEuEnergyStorage;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageBounds;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageItemHooks;
import com.shipovskijkorp.ic2modernadapter.machine.AbstractElectricMachineBlockEntity;
import com.shipovskijkorp.ic2modernadapter.menu.ElectricFurnaceMenu;
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
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Loader-neutral IC2 Electric Furnace: vanilla smelting powered by native EU. */
public abstract class AbstractElectricFurnaceBlockEntity extends BlockEntity
        implements WorldlyContainer, IEuEnergyStorage, MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_DISCHARGE = 2;
    public static final int SLOT_UPGRADE_0 = 3;
    public static final int UPGRADE_SLOTS = 4;
    public static final int SLOT_COUNT = SLOT_UPGRADE_0 + UPGRADE_SLOTS;

    public static final int DATA_ENERGY = 0;
    public static final int DATA_CAPACITY = 1;
    public static final int DATA_PROGRESS = 2;
    public static final int DATA_MAX_PROGRESS = 3;
    public static final int DATA_STORED_XP = 4;
    public static final int DATA_COUNT = 5;

    private static final int[] TOP_SLOTS = {SLOT_INPUT};
    private static final int[] SIDE_SLOTS = {SLOT_INPUT, SLOT_DISCHARGE, SLOT_UPGRADE_0, SLOT_UPGRADE_0 + 1, SLOT_UPGRADE_0 + 2, SLOT_UPGRADE_0 + 3};
    private static final int[] BOTTOM_SLOTS = {SLOT_OUTPUT};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private long energy;
    private int progress;
    private double storedXp;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY -> (int) Math.min(Integer.MAX_VALUE, AbstractElectricFurnaceBlockEntity.this.energy);
                case DATA_CAPACITY -> (int) Math.min(Integer.MAX_VALUE, FurnaceSpec.ELECTRIC.capacityEu());
                case DATA_PROGRESS -> AbstractElectricFurnaceBlockEntity.this.progress;
                case DATA_MAX_PROGRESS -> FurnaceSpec.ELECTRIC.operationTicks();
                case DATA_STORED_XP -> (int) Math.min(Integer.MAX_VALUE, Math.floor(AbstractElectricFurnaceBlockEntity.this.storedXp));
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_ENERGY -> AbstractElectricFurnaceBlockEntity.this.energy = EuStorageBounds.clamp(value, FurnaceSpec.ELECTRIC.capacityEu());
                case DATA_PROGRESS -> AbstractElectricFurnaceBlockEntity.this.progress = Math.max(0, value);
                case DATA_STORED_XP -> AbstractElectricFurnaceBlockEntity.this.storedXp = Math.max(0, value);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    protected AbstractElectricFurnaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        FurnaceSpec stateSpec = FurnaceSpec.fromBlockState(state);
        if (stateSpec != null && stateSpec != FurnaceSpec.ELECTRIC) {
            throw new IllegalArgumentException("Electric furnace block entity does not match block state " + stateSpec.variantKey());
        }
    }

    protected abstract MenuType<?> electricFurnaceMenuType();

    protected abstract @Nullable SmeltingRecipeMatch findSmeltingRecipe(ItemStack input);

    public final void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        boolean changed = chargeFromDischargeSlot();
        boolean active = process();
        if (changed || active) {
            setChanged();
        }
        setActive(active);
    }

    private boolean process() {
        ItemStack input = items.get(SLOT_INPUT);
        SmeltingRecipeMatch match = findSmeltingRecipe(input);
        if (match == null || match.isEmpty()) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            return false;
        }
        if (!FurnaceInventoryUtil.canOutput(items.get(SLOT_OUTPUT), match.output()) || !useEnergy(FurnaceSpec.ELECTRIC.euPerTick())) {
            return false;
        }
        progress++;
        if (progress >= FurnaceSpec.ELECTRIC.operationTicks()) {
            if (!input.isEmpty() && FurnaceInventoryUtil.canOutput(items.get(SLOT_OUTPUT), match.output())) {
                input.shrink(1);
                FurnaceInventoryUtil.insertOutput(items, SLOT_OUTPUT, match.output());
                storedXp += Math.max(0.0F, match.experience());
            }
            progress = 0;
        }
        return true;
    }

    protected final boolean chargeFromDischargeSlot() {
        if (getEuFree() <= 0L) {
            return false;
        }
        ItemStack stack = items.get(SLOT_DISCHARGE);
        if (stack.isEmpty()) {
            return false;
        }
        long free = getEuFree();
        long extracted = EuStorageItemHooks.dischargeIntoStorage(stack, free, FurnaceSpec.ELECTRIC.tier(), false);
        long accepted = Math.min(free, Math.max(0L, extracted));
        if (accepted <= 0L) {
            return false;
        }
        energy += accepted;
        if (stack.isEmpty()) {
            items.set(SLOT_DISCHARGE, ItemStack.EMPTY);
        }
        return true;
    }

    private boolean useEnergy(long amount) {
        if (amount <= 0L) {
            return true;
        }
        if (energy < amount) {
            return false;
        }
        energy -= amount;
        return true;
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

    protected final void saveElectricFurnaceState(CompoundTag tag) {
        tag.putLong("energy", EuStorageBounds.clamp(energy, FurnaceSpec.ELECTRIC.capacityEu()));
        tag.putInt("progress", Math.max(0, progress));
        tag.putDouble("xp", Math.max(0.0D, storedXp));
    }

    protected final void loadElectricFurnaceState(CompoundTag tag) {
        energy = EuStorageBounds.clamp(tag.getLong("energy"), FurnaceSpec.ELECTRIC.capacityEu());
        progress = Math.max(0, tag.getInt("progress"));
        storedXp = Math.max(0.0D, tag.getDouble("xp"));
    }

    protected final NonNullList<ItemStack> mutableItems() {
        return items;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(FurnaceSpec.ELECTRIC.translationKey());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ElectricFurnaceMenu(electricFurnaceMenuType(), containerId, inventory, this, menuData);
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
            case SLOT_DISCHARGE -> EuStorageItemHooks.canDischarge(stack, FurnaceSpec.ELECTRIC.tier());
            case SLOT_UPGRADE_0, SLOT_UPGRADE_0 + 1, SLOT_UPGRADE_0 + 2, SLOT_UPGRADE_0 + 3 ->
                    AbstractElectricMachineBlockEntity.isUpgradeStack(stack);
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

    @Override
    public long getEuStored() {
        return energy;
    }

    @Override
    public long getEuCapacity() {
        return FurnaceSpec.ELECTRIC.capacityEu();
    }

    @Override
    public int getSinkTier() {
        return FurnaceSpec.ELECTRIC.tier();
    }

    @Override
    public int getSourceTier() {
        return 0;
    }

    @Override
    public long insertEu(long amount, Direction from, boolean simulate) {
        if (amount <= 0L || from == null) {
            return 0L;
        }
        long accepted = EuStorageBounds.accept(energy, FurnaceSpec.ELECTRIC.capacityEu(), amount);
        if (accepted <= 0L) {
            return 0L;
        }
        if (!simulate) {
            energy += accepted;
            setChanged();
        }
        return accepted;
    }

    @Override
    public long extractEu(long amount, Direction to, boolean simulate) {
        return 0L;
    }

    @Override
    public boolean canInsert(Direction from) {
        return from != null;
    }

    @Override
    public boolean canExtract(Direction to) {
        return false;
    }

    private long getEuFree() {
        return EuStorageBounds.free(energy, FurnaceSpec.ELECTRIC.capacityEu());
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
