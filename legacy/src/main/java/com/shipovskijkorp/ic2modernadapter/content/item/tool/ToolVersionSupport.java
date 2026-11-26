package com.shipovskijkorp.ic2modernadapter.content.item.tool;
import com.shipovskijkorp.ic2modernadapter.content.LegacyVariantNbt;
import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import com.shipovskijkorp.ic2modernadapter.energy.item.EuItemManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.EquipmentSlot;
import java.util.HashMap;
import java.util.UUID;

public final class ToolVersionSupport {
    private ToolVersionSupport() {}
    private static final UUID ATTACK_DAMAGE = UUID.fromString("cb3f55d3-645c-4f38-a497-9c13a33db5cf");
    private static final UUID ATTACK_SPEED = UUID.fromString("fa233e1c-4180-4865-b01b-bcce9785aca3");
    public static ResourceLocation id(String ns, String path) { return new ResourceLocation(ns, path); }
    public static net.minecraft.world.item.Item.Properties toolProperties(net.minecraft.world.item.Item.Properties props, ElectricToolRules rules) { return props; }
    public static boolean shortGrass(BlockState state) { return state.is(net.minecraft.world.level.block.Blocks.GRASS); }
    public static boolean harvestAllowed(BlockState state, int level) { return level >= 3 || !state.is(BlockTags.NEEDS_DIAMOND_TOOL); }
    public static CompoundTag saveBlockEntity(BlockEntity entity, Level level) { return entity.saveWithFullMetadata(); }
    public static void loadBlockEntity(BlockEntity entity, Level level, CompoundTag tag) { entity.load(tag); }
    public static void clearPainter(ItemStack stack) {
        var tag = IC2VariantStacks.electricData(stack);
        tag.putString(LegacyVariantNbt.VARIANT_KEY, "painter/blank");
        tag.putInt(LegacyVariantNbt.LEGACY_META_KEY, 0); tag.remove("paintUsed");
        tag.putInt("CustomModelData", OriginalContentManifest.get().customModelData("painter/blank"));
        IC2VariantStacks.setElectricData(stack, tag);
    }
    public static void ensureIridiumEnchantments(ItemStack stack, Level level, boolean silk) {
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack) == 3
                && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == (silk ? 1 : 0)) return;
        var enchantments = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        enchantments.put(Enchantments.BLOCK_FORTUNE, 3);
        if (silk) enchantments.put(Enchantments.SILK_TOUCH, 1); else enchantments.remove(Enchantments.SILK_TOUCH);
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }
    public static void updateAttributes(ItemStack stack, ElectricToolRules rules) {
        int bonus = ElectricToolRules.attackBonus(rules, EuItemManager.stored(stack), ElectricMiningToolItem.active(stack));
        CompoundTag data = stack.getOrCreateTag();
        if (data.contains("ic2maToolAttack") && data.getInt("ic2maToolAttack") == bonus) return;
        data.remove("AttributeModifiers");
        if (bonus != 0) {
            stack.addAttributeModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE, "Tool modifier", bonus, AttributeModifier.Operation.ADDITION), EquipmentSlot.MAINHAND);
            stack.addAttributeModifier(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED, "Tool modifier", -3.0, AttributeModifier.Operation.ADDITION), EquipmentSlot.MAINHAND);
        }
        data.putInt("ic2maToolAttack", bonus);
    }
}
