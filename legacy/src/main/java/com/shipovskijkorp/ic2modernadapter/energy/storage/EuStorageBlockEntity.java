package com.shipovskijkorp.ic2modernadapter.energy.storage;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

/** 1.20.1 persistence bridge for the shared IC2 electric-storage implementation. */
public final class EuStorageBlockEntity extends AbstractEuStorageBlockEntity {
    public EuStorageBlockEntity(EuStorageSpec spec, BlockPos pos, BlockState state) {
        super(IC2ContentRegistries.blockEntityType(spec.blockEntityPath()).get(), spec, pos, state);
    }

    @Override
    protected MenuType<?> storageMenuType() {
        return EuStoragePlatform.menuType();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, mutableItems());
        saveStorageState(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, mutableItems());
        loadStorageState(tag);
    }
}
