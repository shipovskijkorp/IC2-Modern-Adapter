package com.shipovskijkorp.ic2modernadapter.furnace;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** 1.21.x persistence bridge for the IC2 Electric Furnace. */
public final class ElectricFurnaceBlockEntity extends AbstractElectricFurnaceBlockEntity {
    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(IC2ContentRegistries.blockEntityType(FurnaceSpec.ELECTRIC.blockEntityPath()).get(), pos, state);
    }

    @Override
    protected MenuType<?> electricFurnaceMenuType() {
        return FurnacePlatform.electricMenuType();
    }

    @Override
    protected @Nullable SmeltingRecipeMatch findSmeltingRecipe(ItemStack input) {
        return SmeltingRecipeSupport.find(getLevel(), input);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, mutableItems(), registries);
        saveElectricFurnaceState(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, mutableItems(), registries);
        loadElectricFurnaceState(tag);
    }
}
