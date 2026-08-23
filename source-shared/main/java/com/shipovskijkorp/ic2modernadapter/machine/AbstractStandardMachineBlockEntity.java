package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantFacingBlock;
import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeRuntime;
import com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeStacks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Shared TileEntityStandardMachine-style processing loop for Macerator and Compressor. */
public abstract class AbstractStandardMachineBlockEntity extends AbstractElectricMachineBlockEntity {
    private String activeRecipeSource = "";

    protected AbstractStandardMachineBlockEntity(
            BlockEntityType<?> type,
            MachineSpec spec,
            BlockPos pos,
            BlockState state) {
        super(type, spec, pos, state);
    }

    public final void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        boolean changed = chargeFromDischargeSlot();
        boolean active = processStandardMachine();
        if (changed || active) {
            setChanged();
        }
        setActive(active);
    }

    private boolean processStandardMachine() {
        ItemStack input = getItem(SLOT_INPUT);
        LegacyMachineRecipeDefinition recipe = LegacyMachineRecipeRegistry.find(spec(), input);
        if (recipe == null) {
            if (progress != 0 || !activeRecipeSource.isEmpty()) {
                resetMachineProgress();
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
            resetMachineProgress();
        }
        return true;
    }

    private void resetMachineProgress() {
        resetProgress();
        activeRecipeSource = "";
    }

    protected final void saveStandardMachineState(CompoundTag tag) {
        saveMachineState(tag);
        tag.putString("activeRecipeSource", activeRecipeSource);
    }

    protected final void loadStandardMachineState(CompoundTag tag) {
        loadMachineState(tag);
        activeRecipeSource = tag.getString("activeRecipeSource");
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
