package com.shipovskijkorp.ic2modernadapter.furnace;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** 1.20.x persistence bridge for the IC2 Electric Furnace. */
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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, mutableItems());
        saveElectricFurnaceState(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, mutableItems());
        loadElectricFurnaceState(tag);
    }
}
