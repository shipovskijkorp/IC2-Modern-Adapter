package com.shipovskijkorp.ic2modernadapter.generator;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

/** Fabric 1.20.1 persistence bridge for the shared Generator behavior. */
public final class GeneratorBlockEntity extends GeneratorBlockEntityBase {
    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(IC2ContentRegistries.blockEntityType("generator").get(), pos, state);
    }

    @Override
    protected MenuType<?> generatorMenuType() {
        return GeneratorPlatform.menuType();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, mutableItems());
        saveGeneratorState(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, mutableItems());
        loadGeneratorState(tag);
    }
}
