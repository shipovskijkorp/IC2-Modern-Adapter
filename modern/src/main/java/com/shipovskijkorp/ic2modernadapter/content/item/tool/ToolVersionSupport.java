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
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.registries.Registries;

public final class ToolVersionSupport {
    private ToolVersionSupport() {}
    public static ResourceLocation id(String ns, String path) { return ResourceLocation.fromNamespaceAndPath(ns, path); }
    public static net.minecraft.world.item.Item.Properties toolProperties(net.minecraft.world.item.Item.Properties props, ElectricToolRules rules) {
        return props.component(DataComponents.TOOL, toolComponent(rules, false, false));
    }
    private static net.minecraft.world.item.component.Tool toolComponent(ElectricToolRules rules, boolean powered, boolean active) {
        var list = new java.util.ArrayList<net.minecraft.world.item.component.Tool.Rule>();
        float speed = powered ? rules.speed : 1F;
        if (rules.harvest < 100) list.add(net.minecraft.world.item.component.Tool.Rule.deniesDrops(
                rules.harvest >= 3 ? BlockTags.INCORRECT_FOR_DIAMOND_TOOL : BlockTags.INCORRECT_FOR_IRON_TOOL));
        if (rules.drill()) {
            list.add(net.minecraft.world.item.component.Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, speed));
            list.add(net.minecraft.world.item.component.Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_SHOVEL, speed));
        } else if (rules == ElectricToolRules.CHAINSAW) {
            for (var tag : java.util.List.of(BlockTags.MINEABLE_WITH_AXE, BlockTags.LEAVES, BlockTags.WOOL))
                list.add(net.minecraft.world.item.component.Tool.Rule.minesAndDrops(tag, speed));
        } else if (rules == ElectricToolRules.ELECTRIC_HOE) {
            list.add(net.minecraft.world.item.component.Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_HOE, speed));
        }
        return new net.minecraft.world.item.component.Tool(list, rules == ElectricToolRules.NANO_SABER && powered && active ? 4F : 1F, 0);
    }
    public static boolean shortGrass(BlockState state) { return state.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS); }
    public static boolean harvestAllowed(BlockState state, int level) { return level >= 4 || !state.is(level >= 3 ? BlockTags.INCORRECT_FOR_DIAMOND_TOOL : BlockTags.INCORRECT_FOR_IRON_TOOL); }
    public static CompoundTag saveBlockEntity(BlockEntity entity, Level level) { return entity.saveWithFullMetadata(level.registryAccess()); }
    public static void loadBlockEntity(BlockEntity entity, Level level, CompoundTag tag) { entity.loadWithComponents(tag, level.registryAccess()); }
    public static void clearPainter(ItemStack stack) {
        var tag = IC2VariantStacks.electricData(stack);
        tag.putString(LegacyVariantNbt.VARIANT_KEY, "painter/blank");
        tag.putInt(LegacyVariantNbt.LEGACY_META_KEY, 0); tag.remove("paintUsed");
        IC2VariantStacks.setElectricData(stack, tag);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(OriginalContentManifest.get().customModelData("painter/blank")));
    }
    public static void ensureIridiumEnchantments(ItemStack stack, Level level, boolean silk) {
        var registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var fortune = registry.getHolderOrThrow(Enchantments.FORTUNE);
        var silkTouch = registry.getHolderOrThrow(Enchantments.SILK_TOUCH);
        var current = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (current.getLevel(fortune) == 3 && current.getLevel(silkTouch) == (silk ? 1 : 0)) return;
        var enchantments = new ItemEnchantments.Mutable(current);
        enchantments.set(fortune, 3); enchantments.set(silkTouch, silk ? 1 : 0);
        stack.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
    }
    public static void updateAttributes(ItemStack stack, ElectricToolRules rules) {
        int bonus = ElectricToolRules.attackBonus(rules, EuItemManager.stored(stack), ElectricMiningToolItem.active(stack));
        var tool = toolComponent(rules, EuItemManager.canUse(stack, rules.cost), ElectricMiningToolItem.active(stack));
        if (!tool.equals(stack.get(DataComponents.TOOL))) stack.set(DataComponents.TOOL, tool);
        var builder = ItemAttributeModifiers.builder();
        if (bonus != 0) {
            builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(id("minecraft", "base_attack_damage"), bonus, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(id("minecraft", "base_attack_speed"), -3.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        }
        var result = builder.build();
        if (!result.equals(stack.get(DataComponents.ATTRIBUTE_MODIFIERS))) stack.set(DataComponents.ATTRIBUTE_MODIFIERS, result);
    }
}
