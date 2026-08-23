package com.shipovskijkorp.ic2modernadapter.generator;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

/** NeoForge 1.21.1 persistence bridge for the shared Generator behavior. */
public final class GeneratorBlockEntity extends GeneratorBlockEntityBase {
    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(IC2ContentRegistries.blockEntityType("generator").get(), pos, state);
    }

    @Override
    protected MenuType<?> generatorMenuType() {
        return GeneratorPlatform.menuType();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, mutableItems(), registries);
        saveGeneratorState(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, mutableItems(), registries);
        loadGeneratorState(tag);
    }
}
