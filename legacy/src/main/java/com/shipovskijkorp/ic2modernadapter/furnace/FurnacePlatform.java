package com.shipovskijkorp.ic2modernadapter.furnace;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.menu.ElectricFurnaceMenu;
import com.shipovskijkorp.ic2modernadapter.menu.IronFurnaceMenu;
import com.shipovskijkorp.ic2modernadapter.menu.InductionFurnaceMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Forge-only registration and interaction glue for IC2 furnace variants. */
public final class FurnacePlatform {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, IC2ModernAdapter.MOD_ID);
    private static final RegistryObject<MenuType<IronFurnaceMenu>> IRON_MENU = MENUS.register(
            "iron_furnace",
            () -> new MenuType<>((id, inventory) -> new IronFurnaceMenu(ironMenuType(), id, inventory),
                    FeatureFlags.DEFAULT_FLAGS));
    private static final RegistryObject<MenuType<ElectricFurnaceMenu>> ELECTRIC_MENU = MENUS.register(
            "electric_furnace",
            () -> new MenuType<>((id, inventory) -> new ElectricFurnaceMenu(electricMenuType(), id, inventory),
                    FeatureFlags.DEFAULT_FLAGS));
    private static final RegistryObject<MenuType<InductionFurnaceMenu>> INDUCTION_MENU = MENUS.register(
            "induction_furnace",
            () -> new MenuType<>((id, inventory) -> new InductionFurnaceMenu(inductionMenuType(), id, inventory),
                    FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
        FurnaceFuelHooks.install(
                stack -> ForgeHooks.getBurnTime(stack, RecipeType.SMELTING),
                ForgeHooks::getCraftingRemainingItem);
        MinecraftForge.EVENT_BUS.addListener(FurnacePlatform::onRightClickBlock);
    }

    public static MenuType<IronFurnaceMenu> ironMenuType() {
        return IRON_MENU.get();
    }

    public static MenuType<ElectricFurnaceMenu> electricMenuType() {
        return ELECTRIC_MENU.get();
    }

    public static MenuType<InductionFurnaceMenu> inductionMenuType() {
        return INDUCTION_MENU.get();
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getLevel().isClientSide()) {
            return;
        }
        if (player.isShiftKeyDown()) {
            return;
        }
        var blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (!(blockEntity instanceof AbstractIronFurnaceBlockEntity)
                && !(blockEntity instanceof AbstractElectricFurnaceBlockEntity)
                && !(blockEntity instanceof AbstractInductionFurnaceBlockEntity)) {
            return;
        }
        player.openMenu((net.minecraft.world.MenuProvider) blockEntity);
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    private FurnacePlatform() {
    }
}
