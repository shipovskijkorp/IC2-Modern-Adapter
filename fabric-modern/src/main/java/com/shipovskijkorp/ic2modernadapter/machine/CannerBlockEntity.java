package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

/** 1.21.1 persistence bridge for the Fluid/Solid Canning Machine. */
public final class CannerBlockEntity extends AbstractCannerBlockEntity {
    public CannerBlockEntity(BlockPos pos, BlockState state) {
        super(IC2ContentRegistries.blockEntityType(MachineSpec.CANNER.blockEntityPath()).get(), pos, state);
    }

    @Override
    protected MenuType<?> machineMenuType() {
        return MachinePlatform.menuType(MachineSpec.CANNER);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, mutableItems(), registries);
        saveCannerState(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, mutableItems(), registries);
        loadCannerState(tag);
    }
}
