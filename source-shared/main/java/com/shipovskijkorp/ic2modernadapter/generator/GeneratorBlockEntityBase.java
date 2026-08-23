package com.shipovskijkorp.ic2modernadapter.generator;

import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantFacingBlock;
import com.shipovskijkorp.ic2modernadapter.energy.api.IEuEnergyStorage;
import com.shipovskijkorp.ic2modernadapter.energy.item.IEuElectricItem;
import com.shipovskijkorp.ic2modernadapter.energy.net.EuNetwork;
import com.shipovskijkorp.ic2modernadapter.energy.util.EuUtil;
import com.shipovskijkorp.ic2modernadapter.menu.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
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

/**
 * Loader-neutral behavior of the IC2 Experimental basic Generator.
 *
 * <p>Reference behavior preserved from 2.8.222-ex112: 10 EU/t, tier 1 (32 EU packet), 4000 EU
 * internal storage, furnace fuel time / 4, no lava bucket, source-only EU on every side, one total
 * tier-sized packet per tick.</p>
 */
public abstract class GeneratorBlockEntityBase extends BlockEntity implements WorldlyContainer, IEuEnergyStorage, MenuProvider {
    public static final int SLOT_CHARGE = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_COUNT = 2;
    public static final int DATA_COUNT = 4;

    private static final int[] ALL_SLOTS = {SLOT_CHARGE, SLOT_FUEL};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private long energy;
    private int fuel;
    private int totalFuel;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) Math.min(Integer.MAX_VALUE, GeneratorBlockEntityBase.this.energy);
                case 1 -> (int) Math.min(Integer.MAX_VALUE, GeneratorConstants.CAPACITY_EU);
                case 2 -> GeneratorBlockEntityBase.this.fuel;
                case 3 -> GeneratorBlockEntityBase.this.totalFuel;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> GeneratorBlockEntityBase.this.energy = clampEnergy(value);
                case 2 -> GeneratorBlockEntityBase.this.fuel = Math.max(0, value);
                case 3 -> GeneratorBlockEntityBase.this.totalFuel = Math.max(0, value);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    protected GeneratorBlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract MenuType<?> generatorMenuType();

    public final void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        // IC2's Energy component processes its managed charging slot before the generator's own
        // server update. Keep that order so a battery cannot consume EU produced later in the same
        // machine tick.
        boolean charged = chargeItem();

        boolean inventoryChanged = false;
        if (needsFuel()) {
            inventoryChanged = gainFuel();
        }

        boolean active = gainEnergy();
        long emitted = emitEnergy();

        if (inventoryChanged || charged || emitted > 0L || active) {
            setChanged();
        }
        setActive(active);
    }

    private boolean needsFuel() {
        return fuel <= 0 && getEuFree() >= GeneratorConstants.PRODUCTION_EU_PER_TICK;
    }

    private boolean gainFuel() {
        ItemStack stack = items.get(SLOT_FUEL);
        int fuelValue = GeneratorFuelHooks.getFuelTicks(stack);
        if (fuelValue <= 0) {
            return false;
        }

        ItemStack consumed = stack.copyWithCount(1);
        ItemStack remainder = GeneratorFuelHooks.getCraftingRemainder(consumed);
        // InvSlotConsumable in IC2 refuses to consume a stacked container-item because there is no
        // second output slot for the container. Preserve that edge case instead of deleting it.
        if (!remainder.isEmpty() && stack.getCount() != 1) {
            return false;
        }
        stack.shrink(1);
        if (stack.isEmpty()) {
            items.set(SLOT_FUEL, remainder.copy());
        }

        fuel += fuelValue;
        totalFuel = fuelValue;
        return true;
    }

    /** TileEntityGenerator overrides isConverting() to fuel > 0, so already loaded fuel keeps burning at full storage. */
    private boolean gainEnergy() {
        long production = GeneratorConstants.PRODUCTION_EU_PER_TICK;
        if (fuel <= 0 || production <= 0L) {
            return false;
        }
        energy = Math.min(GeneratorConstants.CAPACITY_EU, energy + production);
        fuel--;
        return true;
    }

    private boolean chargeItem() {
        ItemStack stack = items.get(SLOT_CHARGE);
        if (!(stack.getItem() instanceof IEuElectricItem electricItem)) {
            return false;
        }
        if (!electricItem.canChargeFromTier(stack, GeneratorConstants.TIER) || energy <= 0L) {
            return false;
        }

        long chargeBudget = Math.min(energy, Math.max(0L, electricItem.getEuTransferLimit(stack)));
        if (chargeBudget <= 0L) {
            return false;
        }
        long accepted = electricItem.insertEu(stack, chargeBudget, false);
        if (accepted <= 0L) {
            return false;
        }
        energy = Math.max(0L, energy - Math.min(energy, accepted));
        return true;
    }

    /**
     * Emits one total tier-1 packet per server tick. The packet may be split over multiple routes,
     * but trying another face does not create another 32-EU packet.
     */
    private long emitEnergy() {
        Level level = getLevel();
        if (level == null || energy <= 0L) {
            return 0L;
        }

        long remaining = Math.min(energy, EuUtil.powerFromTier(GeneratorConstants.TIER));
        long spentTotal = 0L;
        for (Direction direction : Direction.values()) {
            if (remaining <= 0L) {
                break;
            }
            long spent = EuNetwork.route(level, worldPosition, this, direction, remaining);
            if (spent > 0L) {
                remaining -= spent;
                spentTotal += spent;
            }
        }
        return spentTotal;
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

    public final boolean isActive() {
        BlockState state = getBlockState();
        return state.hasProperty(LegacyVariantFacingBlock.ACTIVE)
                && state.getValue(LegacyVariantFacingBlock.ACTIVE);
    }

    public final long getProductionEuPerTick() {
        return GeneratorConstants.PRODUCTION_EU_PER_TICK;
    }

    public final int getFuel() {
        return fuel;
    }

    public final int getTotalFuel() {
        return totalFuel;
    }

    public final double getFuelRatio() {
        return fuel <= 0 || totalFuel <= 0 ? 0.0 : (double) fuel / (double) totalFuel;
    }

    public final ContainerData menuData() {
        return menuData;
    }

    public static boolean isChargeItem(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof IEuElectricItem electricItem)) {
            return false;
        }
        return electricItem.canChargeFromTier(stack, GeneratorConstants.TIER);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("ic2.te.generator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new GeneratorMenu(generatorMenuType(), containerId, inventory, this, menuData);
    }

    @Override
    public int getContainerSize() {
        return items.size();
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
        if (stack.getCount() > getMaxStackSize()) {
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
        return switch (slot) {
            case SLOT_CHARGE -> isChargeItem(stack);
            case SLOT_FUEL -> GeneratorFuelHooks.isFuel(stack);
            default -> false;
        };
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        // IC2's TileEntityInventory exposes every external slot on every face. InvSide is a
        // preference used only to arbitrate ambiguous items, not a hard sided-inventory filter.
        return ALL_SLOTS.clone();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        if (stack.isEmpty() || !canPlaceItem(slot, stack)) {
            return false;
        }
        if (direction == null || preferredInputSideMatches(slot, direction)) {
            return true;
        }

        // Match TileEntityInventory.canInsertItem(): a non-preferred target only loses when some
        // other accepting input slot explicitly prefers this face. This matters for addon items
        // that could validly be both fuel and EU-chargeable.
        for (int otherSlot : ALL_SLOTS) {
            if (otherSlot == slot) {
                continue;
            }
            if (preferredInputSideMatches(otherSlot, direction) && canPlaceItem(otherSlot, stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        if (!canOutputSlot(slot)) {
            return false;
        }
        if (preferredOutputSideMatches(slot, direction)) {
            return true;
        }

        // Match TileEntityInventory.canExtractItem(): the preferred output on a face wins only if
        // that competing slot currently has something it is allowed to expose.
        for (int otherSlot : ALL_SLOTS) {
            if (otherSlot == slot || !preferredOutputSideMatches(otherSlot, direction)) {
                continue;
            }
            if (!items.get(otherSlot).isEmpty() && canOutputSlot(otherSlot)) {
                return false;
            }
        }
        return true;
    }

    private boolean canOutputSlot(int slot) {
        if (slot == SLOT_CHARGE) {
            // InvSlotCharge uses Access.IO.
            return true;
        }
        if (slot == SLOT_FUEL) {
            // InvSlotConsumableFuel is input-only, except that an invalid item already present in
            // the slot may be extracted so automation can recover it.
            ItemStack fuelStack = items.get(SLOT_FUEL);
            return !fuelStack.isEmpty() && !GeneratorFuelHooks.isFuel(fuelStack);
        }
        return false;
    }

    private static boolean preferredInputSideMatches(int slot, Direction side) {
        return slot == SLOT_CHARGE ? side == Direction.UP : slot == SLOT_FUEL && side.getAxis().isHorizontal();
    }

    private static boolean preferredOutputSideMatches(int slot, Direction side) {
        return preferredInputSideMatches(slot, side);
    }

    @Override
    public long getEuStored() {
        return energy;
    }

    @Override
    public long getEuCapacity() {
        return GeneratorConstants.CAPACITY_EU;
    }

    @Override
    public int getSinkTier() {
        return 0;
    }

    @Override
    public int getSourceTier() {
        return GeneratorConstants.TIER;
    }

    @Override
    public long insertEu(long amount, Direction from, boolean simulate) {
        return 0L;
    }

    @Override
    public long extractEu(long amount, Direction to, boolean simulate) {
        if (amount <= 0L || energy <= 0L) {
            return 0L;
        }
        long extracted = Math.min(Math.min(amount, EuUtil.powerFromTier(GeneratorConstants.TIER)), energy);
        if (!simulate) {
            energy -= extracted;
            setChanged();
        }
        return extracted;
    }

    @Override
    public boolean canInsert(Direction from) {
        return false;
    }

    @Override
    public boolean canExtract(Direction to) {
        return true;
    }

    protected final NonNullList<ItemStack> mutableItems() {
        return items;
    }

    protected final void saveGeneratorState(CompoundTag tag) {
        tag.putLong("energy", energy);
        tag.putInt("fuel", fuel);
        tag.putInt("totalFuel", totalFuel);
    }

    protected final void loadGeneratorState(CompoundTag tag) {
        energy = clampEnergy(tag.getLong("energy"));
        fuel = Math.max(0, tag.getInt("fuel"));
        totalFuel = Math.max(0, tag.getInt("totalFuel"));
    }

    private static long clampEnergy(long value) {
        return Math.max(0L, Math.min(GeneratorConstants.CAPACITY_EU, value));
    }

    private long getEuFree() {
        return Math.max(0L, GeneratorConstants.CAPACITY_EU - energy);
    }
}
