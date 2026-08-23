package com.shipovskijkorp.ic2modernadapter.energy.cable;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/** 1.21.1 persistence bridge for the shared cable implementation. */
public final class CableBlockEntity extends AbstractCableBlockEntity {
    public CableBlockEntity(EuCableVariant variant, BlockPos pos, BlockState state) {
        super(IC2ContentRegistries.blockEntityType(variant.blockEntityPath()).get(), variant, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveCableState(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadCableState(tag);
    }
}
