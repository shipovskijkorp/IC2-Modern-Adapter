package com.shipovskijkorp.ic2modernadapter.content.item.tool;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
public final class ToolInteractionPlatform {
    private ToolInteractionPlatform() {}
    public static void register() {
        var phase = ToolVersionSupport.id("ic2", "chainsaw_shearing");
        PlayerBlockBreakEvents.BEFORE.addPhaseOrdering(net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE, phase);
        PlayerBlockBreakEvents.BEFORE.register(phase, (world, player, pos, state, entity) ->
                !(player instanceof ServerPlayer server) || !ElectricMiningToolItem.shearBlock(server, pos, state));
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (player.isSpectator()) return InteractionResult.PASS;
            var stack = player.getItemInHand(hand);
            var state = level.getBlockState(hit.getBlockPos());
            if (stack.getItem() instanceof PainterItem painter && PainterItem.color(stack) != null
                    && PaintBlockSupport.supported(state, level.getBlockEntity(hit.getBlockPos()), PainterItem.color(stack))) {
                return painter.useOn(new UseOnContext(player, hand, hit));
            }
            if ((stack.getItem() instanceof ClassicWrenchItem || stack.getItem() instanceof ElectricWrenchItem) && WrenchAction.target(state))
                return stack.getItem().useOn(new UseOnContext(player, hand, hit));
            return InteractionResult.PASS;
        });
    }
    public static boolean mayDismantle(ServerPlayer player, BlockPos pos, BlockState state) {
        var level = player.serverLevel();
        return PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, state, level.getBlockEntity(pos));
    }
}
