package com.shipovskijkorp.ic2modernadapter.furnace;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.menu.ElectricFurnaceMenu;
import com.shipovskijkorp.ic2modernadapter.menu.IronFurnaceMenu;
import com.shipovskijkorp.ic2modernadapter.menu.InductionFurnaceMenu;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/** Fabric registration and interaction glue for IC2 furnace variants. */
public final class FurnacePlatform {
    private static MenuType<IronFurnaceMenu> ironMenu;
    private static MenuType<ElectricFurnaceMenu> electricMenu;
    private static MenuType<InductionFurnaceMenu> inductionMenu;

    public static void register() {
        ironMenu = Registry.register(
                BuiltInRegistries.MENU,
                Objects.requireNonNull(ResourceLocation.tryParse(IC2ModernAdapter.MOD_ID + ":iron_furnace")),
                new MenuType<>((id, inventory) -> new IronFurnaceMenu(ironMenuType(), id, inventory),
                        FeatureFlags.DEFAULT_FLAGS));
        electricMenu = Registry.register(
                BuiltInRegistries.MENU,
                Objects.requireNonNull(ResourceLocation.tryParse(IC2ModernAdapter.MOD_ID + ":electric_furnace")),
                new MenuType<>((id, inventory) -> new ElectricFurnaceMenu(electricMenuType(), id, inventory),
                        FeatureFlags.DEFAULT_FLAGS));
        inductionMenu = Registry.register(
                BuiltInRegistries.MENU,
                Objects.requireNonNull(ResourceLocation.tryParse(IC2ModernAdapter.MOD_ID + ":induction_furnace")),
                new MenuType<>((id, inventory) -> new InductionFurnaceMenu(inductionMenuType(), id, inventory),
                        FeatureFlags.DEFAULT_FLAGS));
        FurnaceFuelHooks.install(
                stack -> {
                    Integer burn = FuelRegistry.INSTANCE.get(stack.getItem());
                    return burn == null ? 0 : burn;
                },
                FurnacePlatform::vanillaCraftingRemainder);
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }
            var blockEntity = level.getBlockEntity(hit.getBlockPos());
            if (!(blockEntity instanceof AbstractIronFurnaceBlockEntity)
                    && !(blockEntity instanceof AbstractElectricFurnaceBlockEntity)
                    && !(blockEntity instanceof AbstractInductionFurnaceBlockEntity)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu((MenuProvider) blockEntity);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        });
    }

    public static MenuType<IronFurnaceMenu> ironMenuType() {
        if (ironMenu == null) {
            throw new IllegalStateException("Iron furnace menu requested before Fabric registration");
        }
        return ironMenu;
    }

    public static MenuType<ElectricFurnaceMenu> electricMenuType() {
        if (electricMenu == null) {
            throw new IllegalStateException("Electric furnace menu requested before Fabric registration");
        }
        return electricMenu;
    }

    public static MenuType<InductionFurnaceMenu> inductionMenuType() {
        if (inductionMenu == null) {
            throw new IllegalStateException("Induction furnace menu requested before Fabric registration");
        }
        return inductionMenu;
    }

    private static ItemStack vanillaCraftingRemainder(ItemStack stack) {
        return ((FabricItemStack) (Object) stack).getRecipeRemainder();
    }

    private FurnacePlatform() {
    }
}
