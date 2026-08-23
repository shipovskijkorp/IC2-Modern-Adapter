package com.shipovskijkorp.ic2modernadapter.energy.cable;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/** 1.20.1 persistence bridge for the shared cable implementation. */
public final class CableBlockEntity extends AbstractCableBlockEntity {
    public CableBlockEntity(EuCableVariant variant, BlockPos pos, BlockState state) {
        super(IC2ContentRegistries.blockEntityType(variant.blockEntityPath()).get(), variant, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveCableState(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadCableState(tag);
    }
}
