package com.shipovskijkorp.ic2modernadapter.furnace;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** 1.20.x persistence bridge for the IC2 Iron Furnace. */
public final class IronFurnaceBlockEntity extends AbstractIronFurnaceBlockEntity {
    public IronFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(IC2ContentRegistries.blockEntityType(FurnaceSpec.IRON.blockEntityPath()).get(), pos, state);
    }

    @Override
    protected @Nullable SmeltingRecipeMatch findSmeltingRecipe(ItemStack input) {
        return SmeltingRecipeSupport.find(getLevel(), input);
    }

    @Override
    protected MenuType<?> ironFurnaceMenuType() {
        return FurnacePlatform.ironMenuType();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, mutableItems());
        saveIronFurnaceState(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, mutableItems());
        loadIronFurnaceState(tag);
    }
}
