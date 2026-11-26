package com.shipovskijkorp.ic2modernadapter.content.item.tool;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.EventPriority;
public final class ToolInteractionPlatform {
    private ToolInteractionPlatform() {}
    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, ToolInteractionPlatform::useBlock);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ToolInteractionPlatform::shearBlock);
    }
    private static void shearBlock(BlockEvent.BreakEvent event) {
        if (!event.isCanceled() && event.getPlayer() instanceof ServerPlayer player
                && ElectricMiningToolItem.shearBlock(player, event.getPos(), event.getState())) event.setCanceled(true);
    }
    private static void useBlock(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity(); var level = event.getLevel(); var stack = event.getItemStack();
        if (player.isSpectator()) return;
        var state = level.getBlockState(event.getPos());
        boolean painter = stack.getItem() instanceof PainterItem && PainterItem.color(stack) != null
                && PaintBlockSupport.supported(state, level.getBlockEntity(event.getPos()), PainterItem.color(stack));
        boolean wrench = (stack.getItem() instanceof ClassicWrenchItem || stack.getItem() instanceof ElectricWrenchItem) && WrenchAction.target(state);
        if (!painter && !wrench) return;
        InteractionResult result = stack.getItem().useOn(new UseOnContext(player, event.getHand(), event.getHitVec()));
        if (result != InteractionResult.PASS) { event.setCancellationResult(result); event.setCanceled(true); }
    }
    public static boolean mayDismantle(ServerPlayer player, BlockPos pos, BlockState state) {
        var event = new BlockEvent.BreakEvent(player.level(), pos, state, player);
        NeoForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }
}
