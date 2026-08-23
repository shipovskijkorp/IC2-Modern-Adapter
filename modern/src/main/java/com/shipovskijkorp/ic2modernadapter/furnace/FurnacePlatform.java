package com.shipovskijkorp.ic2modernadapter.furnace;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.menu.ElectricFurnaceMenu;
import com.shipovskijkorp.ic2modernadapter.menu.IronFurnaceMenu;
import com.shipovskijkorp.ic2modernadapter.menu.InductionFurnaceMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NeoForge-only registration and interaction glue for IC2 furnace variants. */
public final class FurnacePlatform {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, IC2ModernAdapter.MOD_ID);
    private static final java.util.function.Supplier<MenuType<IronFurnaceMenu>> IRON_MENU = MENUS.register(
            "iron_furnace",
            () -> new MenuType<>((id, inventory) -> new IronFurnaceMenu(ironMenuType(), id, inventory),
                    FeatureFlags.DEFAULT_FLAGS));
    private static final java.util.function.Supplier<MenuType<ElectricFurnaceMenu>> ELECTRIC_MENU = MENUS.register(
            "electric_furnace",
            () -> new MenuType<>((id, inventory) -> new ElectricFurnaceMenu(electricMenuType(), id, inventory),
                    FeatureFlags.DEFAULT_FLAGS));
    private static final java.util.function.Supplier<MenuType<InductionFurnaceMenu>> INDUCTION_MENU = MENUS.register(
            "induction_furnace",
            () -> new MenuType<>((id, inventory) -> new InductionFurnaceMenu(inductionMenuType(), id, inventory),
                    FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
        FurnaceFuelHooks.install(
                stack -> stack.getBurnTime(RecipeType.SMELTING),
                ItemStack::getCraftingRemainingItem);
        NeoForge.EVENT_BUS.addListener(FurnacePlatform::onRightClickBlock);
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
        player.openMenu((MenuProvider) blockEntity);
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    private FurnacePlatform() {
    }
}
