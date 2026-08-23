package com.shipovskijkorp.ic2modernadapter.energy.storage;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

/** 1.21.1 persistence bridge for the shared IC2 electric-storage implementation. */
public final class EuStorageBlockEntity extends AbstractEuStorageBlockEntity {
    public EuStorageBlockEntity(EuStorageSpec spec, BlockPos pos, BlockState state) {
        super(IC2ContentRegistries.blockEntityType(spec.blockEntityPath()).get(), spec, pos, state);
    }

    @Override
    protected MenuType<?> storageMenuType() {
        return EuStoragePlatform.menuType();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, mutableItems(), registries);
        saveStorageState(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, mutableItems(), registries);
        loadStorageState(tag);
    }
}
