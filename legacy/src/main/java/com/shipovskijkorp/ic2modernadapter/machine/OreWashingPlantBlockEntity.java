package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

/** 1.20.1 persistence bridge for the Ore Washing Plant. */
public final class OreWashingPlantBlockEntity extends AbstractOreWashingPlantBlockEntity {
    public OreWashingPlantBlockEntity(BlockPos pos, BlockState state) {
        super(IC2ContentRegistries.blockEntityType(MachineSpec.ORE_WASHING_PLANT.blockEntityPath()).get(), pos, state);
    }

    @Override
    protected MenuType<?> machineMenuType() {
        return MachinePlatform.menuType(MachineSpec.ORE_WASHING_PLANT);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, mutableItems());
        saveOreWashingState(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, mutableItems());
        loadOreWashingState(tag);
    }
}
