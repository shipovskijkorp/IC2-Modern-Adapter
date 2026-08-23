package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeRuntime;
import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeStacks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Loader-neutral implementation of IC2's TileEntityMetalFormer. */
public abstract class AbstractMetalFormerBlockEntity extends AbstractElectricMachineBlockEntity {
    private static final int EXTRA_MODE = 0;
    private MetalFormerMode mode = MetalFormerMode.EXTRUDING;
    private String activeRecipeSource = "";

    protected AbstractMetalFormerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, MachineSpec.METAL_FORMER, pos, state);
    }

    @Override
    protected int getExtraDataCount() {
        return 1;
    }

    @Override
    protected int getExtraData(int index) {
        return index == EXTRA_MODE ? mode.id() : 0;
    }

    @Override
    protected void setExtraData(int index, int value) {
        if (index == EXTRA_MODE) {
            mode = MetalFormerMode.byId(value);
        }
    }

    public final MetalFormerMode mode() {
        return mode;
    }

    public final void cycleMode() {
        mode = mode.next();
        resetMetalFormerProgress();
        setChanged();
    }

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
        setMachineActive(active);
    }

    private boolean process() {
        ItemStack input = getItem(SLOT_INPUT);
        LegacyMachineRecipeDefinition recipe = LegacyMachineRecipeRegistry.find(
                MachineSpec.METAL_FORMER, input, mode.sourcePrefix());
        if (recipe == null) {
            if (progress != 0 || !activeRecipeSource.isEmpty()) {
                resetMetalFormerProgress();
                setChanged();
            }
            return false;
        }
        if (!recipe.source().equals(activeRecipeSource)) {
            resetProgress();
            activeRecipeSource = recipe.source();
        }
        List<ItemStack> outputs = createOutputs(recipe);
        if (!canOutputAll(outputs) || !useEnergy(spec().euPerTick())) {
            return false;
        }
        maxProgress = spec().operationTicks();
        progress++;
        if (progress >= maxProgress) {
            if (input.getCount() >= recipe.inputCount() && canOutputAll(outputs)) {
                input.shrink(recipe.inputCount());
                for (ItemStack output : outputs) {
                    insertOutput(output);
                }
            }
            resetMetalFormerProgress();
        }
        return true;
    }

    private void resetMetalFormerProgress() {
        resetProgress();
        activeRecipeSource = "";
    }

    protected final void saveMetalFormerState(CompoundTag tag) {
        saveMachineState(tag);
        tag.putInt("mode", mode.id());
        tag.putString("activeRecipeSource", activeRecipeSource);
    }

    protected final void loadMetalFormerState(CompoundTag tag) {
        loadMachineState(tag);
        mode = MetalFormerMode.byId(tag.getInt("mode"));
        activeRecipeSource = tag.getString("activeRecipeSource");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new com.shipovskijkorp.ic2modernadapter.menu.MetalFormerMenu(
                machineMenuType(), containerId, inventory, this, menuData());
    }

    private static List<ItemStack> createOutputs(LegacyMachineRecipeDefinition recipe) {
        List<ItemStack> outputs = new ArrayList<>(recipe.outputs().size());
        for (LegacyMachineRecipeDefinition.Output output : recipe.outputs()) {
            outputs.add(LegacyRecipeRuntime.createResult(output.item(), output.count(), LegacyRecipeStacks.INSTANCE));
        }
        return outputs;
    }

    private boolean canOutputAll(List<ItemStack> outputs) {
        ItemStack snapshot = getItem(SLOT_OUTPUT).copy();
        for (ItemStack output : outputs) {
            if (output.isEmpty()) {
                return false;
            }
            if (snapshot.isEmpty()) {
                if (output.getCount() > output.getMaxStackSize()) {
                    return false;
                }
                snapshot = output.copy();
                continue;
            }
            if (!canStacksMerge(snapshot, output)) {
                return false;
            }
            int next = snapshot.getCount() + output.getCount();
            if (next > snapshot.getMaxStackSize()) {
                return false;
            }
            snapshot.setCount(next);
        }
        return true;
    }
}
