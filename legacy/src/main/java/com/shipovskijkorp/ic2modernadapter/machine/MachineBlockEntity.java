package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

/** Forge 1.20.1 persistence bridge for shared IC2 standard machines. */
public final class MachineBlockEntity extends AbstractStandardMachineBlockEntity {
    public MachineBlockEntity(MachineSpec spec, BlockPos pos, BlockState state) {
        super(IC2ContentRegistries.blockEntityType(spec.blockEntityPath()).get(), spec, pos, state);
    }

    @Override
    protected MenuType<?> machineMenuType() {
        return MachinePlatform.menuType(spec());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, mutableItems());
        saveStandardMachineState(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, mutableItems());
        loadStandardMachineState(tag);
    }
}
