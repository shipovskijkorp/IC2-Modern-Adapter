package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

/** 1.21.1 persistence bridge for the Solid Canning Machine. */
public final class SolidCannerBlockEntity extends AbstractSolidCannerBlockEntity {
    public SolidCannerBlockEntity(BlockPos pos, BlockState state) {
        super(IC2ContentRegistries.blockEntityType(MachineSpec.SOLID_CANNER.blockEntityPath()).get(), pos, state);
    }

    @Override
    protected MenuType<?> machineMenuType() {
        return MachinePlatform.menuType(MachineSpec.SOLID_CANNER);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, mutableItems(), registries);
        saveSolidCannerState(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, mutableItems(), registries);
        loadSolidCannerState(tag);
    }
}
