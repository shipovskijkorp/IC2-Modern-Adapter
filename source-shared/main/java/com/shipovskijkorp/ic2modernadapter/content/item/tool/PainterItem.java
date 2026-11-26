package com.shipovskijkorp.ic2modernadapter.content.item.tool;

import com.shipovskijkorp.ic2modernadapter.content.item.LegacyTranslatedItem;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** IC2 paint roller; BCCE brush interaction semantics, original 32 paints per refill. */
public final class PainterItem extends LegacyTranslatedItem {
    public PainterItem(String path, Properties properties, Function<ItemStack, String> variants) {
        super(path, properties.stacksTo(1), variants);
    }
    public static DyeColor color(ItemStack stack) {
        String key = IC2VariantStacks.variantKey(stack);
        if (key == null || !key.startsWith("painter/")) return null;
        String suffix = key.substring("painter/".length());
        for (DyeColor dye : DyeColor.values()) if (dye.getName().equals(suffix)) return dye;
        return null;
    }
    public static int used(ItemStack stack) {
        return Math.max(0, Math.min(ElectricToolRules.paintUses() - 1, IC2VariantStacks.electricData(stack).getInt("paintUsed")));
    }
    @Override public boolean isBarVisible(ItemStack stack) { return color(stack) != null && used(stack) > 0; }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13F * (ElectricToolRules.paintUses() - used(stack)) / ElectricToolRules.paintUses()); }
    @Override public int getBarColor(ItemStack stack) { return 0x5EBA38; }
    @Override public InteractionResult useOn(UseOnContext ctx) {
        DyeColor color = color(ctx.getItemInHand());
        Player player = ctx.getPlayer();
        if (color == null || player == null || !player.mayBuild() || !ctx.getLevel().mayInteract(player, ctx.getClickedPos())
                || !player.mayUseItemAt(ctx.getClickedPos(), ctx.getClickedFace(), ctx.getItemInHand())) return InteractionResult.PASS;
        if (ctx.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        if (!PaintBlockSupport.paint(ctx.getLevel(), ctx.getClickedPos(), color, player)) return InteractionResult.FAIL;
        ctx.getLevel().playSound(null, ctx.getClickedPos(), SoundEvents.DYE_USE, SoundSource.BLOCKS, 1F, 1F);
        spend(player, ctx.getHand(), color);
        return InteractionResult.CONSUME;
    }
    @Override public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        DyeColor color = color(stack);
        if (color == null || !(target instanceof Sheep sheep) || sheep.getColor() == color) return InteractionResult.PASS;
        if (player.level().isClientSide()) return InteractionResult.SUCCESS;
        sheep.setColor(color);
        spend(player, hand, color);
        return InteractionResult.CONSUME;
    }
    private static void spend(Player player, InteractionHand hand, DyeColor color) {
        if (player.getAbilities().instabuild) return;
        ItemStack stack = player.getItemInHand(hand);
        var tag = IC2VariantStacks.electricData(stack);
        int next = used(stack) + 1;
        if (next >= ElectricToolRules.paintUses()) {
            boolean refill = tag.getBoolean("autoRefill") && consumeDye(player, color);
            if (refill) {
                tag.remove("paintUsed");
                IC2VariantStacks.setElectricData(stack, tag);
            } else {
                // Replace the variant identity, but keep custom name and unrelated stack data.
                ToolVersionSupport.clearPainter(stack);
            }
        } else {
            tag.putInt("paintUsed", next);
            IC2VariantStacks.setElectricData(stack, tag);
        }
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
    }
    private static boolean consumeDye(Player player, DyeColor color) {
        for (int i = 0; i < Math.min(36, player.getInventory().getContainerSize()); i++) {
            ItemStack dye = player.getInventory().getItem(i);
            if (dye.getItem() instanceof DyeItem item && item.getDyeColor() == color) {
                dye.shrink(1); return true;
            }
        }
        return false;
    }
    public boolean cycleMode(ItemStack stack, Player player) {
        boolean value = !ToolData.flag(stack, "autoRefill");
        ToolData.flag(stack, "autoRefill", value);
        player.displayClientMessage(Component.translatable(value ? "ic2ma.tool.painter.refill_on" : "ic2ma.tool.painter.refill_off"), true);
        return true;
    }
}
