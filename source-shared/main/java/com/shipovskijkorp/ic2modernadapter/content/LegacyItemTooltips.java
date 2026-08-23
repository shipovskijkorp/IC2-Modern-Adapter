package com.shipovskijkorp.ic2modernadapter.content;

import com.shipovskijkorp.ic2modernadapter.content.item.LegacyCraftingToolItem;
import com.shipovskijkorp.ic2modernadapter.energy.cable.EuCableVariant;
import com.shipovskijkorp.ic2modernadapter.energy.item.IEuElectricItem;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageSpec;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Original IC2-style item tooltip lines that are independent from the red In Dev marker. */
public final class LegacyItemTooltips {
    private static final DecimalFormat CABLE_LOSS_FORMAT = new DecimalFormat(
            "0.###", DecimalFormatSymbols.getInstance(Locale.ROOT));

    public static void append(
            ItemStack stack,
            String namespace,
            String itemPath,
            String variantKey,
            List<Component> tooltip,
            boolean showPowerTier) {
        if (stack == null || stack.isEmpty() || !"ic2".equals(namespace)) {
            return;
        }

        appendCableTooltip(variantKey, tooltip);
        appendStorageTooltip(stack, variantKey, tooltip);
        appendElectricTooltip(stack, tooltip, showPowerTier);
        appendSingleUseBatteryTooltip(itemPath, tooltip);
        appendCraftingToolTooltip(stack, itemPath, tooltip);
        appendUpgradeTooltip(stack, itemPath, variantKey, tooltip);
        appendKnownInformationalTooltip(stack, itemPath, variantKey, tooltip);
    }

    private static void appendCableTooltip(String variantKey, List<Component> tooltip) {
        EuCableVariant cable = EuCableVariant.fromVariantKey(variantKey);
        if (cable == null) {
            return;
        }
        tooltip.add(Component.empty()
                .append(Integer.toString(cable.capacity()))
                .append(" ")
                .append(Component.translatable("ic2.generic.text.EUt")));
        tooltip.add(Component.translatable("ic2.cable.tooltip.loss", CABLE_LOSS_FORMAT.format(cable.loss())));
    }

    private static void appendStorageTooltip(ItemStack stack, String variantKey, List<Component> tooltip) {
        EuStorageSpec storage = EuStorageSpec.fromVariantKey(variantKey);
        if (storage == null) {
            return;
        }
        tooltip.add(Component.empty()
                .append(Component.translatable("ic2.item.tooltip.Output"))
                .append(" ")
                .append(Long.toString(storage.outputEuPerTick()))
                .append(" ")
                .append(Component.translatable("ic2.generic.text.EUt"))
                .append(" ")
                .append(Component.translatable("ic2.item.tooltip.Capacity"))
                .append(" ")
                .append(Long.toString(storage.capacityEu()))
                .append(" ")
                .append(Component.translatable("ic2.generic.text.EU")));
        tooltip.add(Component.empty()
                .append(Component.translatable("ic2.item.tooltip.Store"))
                .append(" ")
                .append(Long.toString(IC2VariantStacks.blockEntityEnergy(stack)))
                .append(" ")
                .append(Component.translatable("ic2.generic.text.EU")));
    }

    private static void appendElectricTooltip(ItemStack stack, List<Component> tooltip, boolean showPowerTier) {
        if (!(stack.getItem() instanceof IEuElectricItem electricItem)) {
            return;
        }
        tooltip.add(Component.literal(toSiString(electricItem.getEuStored(stack))
                + "/" + toSiString(electricItem.getEuCapacity(stack)) + " EU"));
        if (showPowerTier) {
            tooltip.add(Component.translatable("ic2.item.tooltip.PowerTier", electricItem.getEuTier(stack)));
        }
    }

    private static void appendSingleUseBatteryTooltip(String itemPath, List<Component> tooltip) {
        if ("single_use_battery".equals(itemPath)) {
            tooltip.add(Component.literal("1200 EU"));
        }
    }

    private static void appendCraftingToolTooltip(ItemStack stack, String itemPath, List<Component> tooltip) {
        if (!(stack.getItem() instanceof LegacyCraftingToolItem)
                && !"cutter".equals(itemPath)
                && !"forge_hammer".equals(itemPath)) {
            return;
        }
        if (!stack.isDamageableItem()) {
            return;
        }
        int usesLeft = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
        tooltip.add(Component.translatable("ic2.item.ItemTool.tooltip.UsesLeft", usesLeft));
    }

    private static void appendUpgradeTooltip(
            ItemStack stack, String itemPath, String variantKey, List<Component> tooltip) {
        if (!"upgrade".equals(itemPath) || variantKey == null || !variantKey.startsWith("upgrade/")) {
            return;
        }
        int count = Math.max(1, stack.getCount());
        String variant = variantKey.substring("upgrade/".length());
        switch (variant) {
            case "overclocker" -> {
                tooltip.add(Component.translatable("ic2.tooltip.upgrade.overclocker.time", "70"));
                tooltip.add(Component.translatable("ic2.tooltip.upgrade.overclocker.power", "160"));
            }
            case "transformer" -> tooltip.add(Component.translatable("ic2.tooltip.upgrade.transformer", count));
            case "energy_storage" -> tooltip.add(Component.translatable("ic2.tooltip.upgrade.storage", 10_000 * count));
            case "ejector", "advanced_ejector", "fluid_ejector" -> tooltip.add(Component.translatable(
                    "ic2.tooltip.upgrade.ejector",
                    Component.translatable("ic2.tooltip.upgrade.ejector.anyside")));
            case "pulling", "advanced_pulling", "fluid_pulling" -> tooltip.add(Component.translatable(
                    "ic2.tooltip.upgrade.pulling",
                    Component.translatable("ic2.tooltip.upgrade.ejector.anyside")));
            case "redstone_inverter" -> tooltip.add(Component.translatable("ic2.tooltip.upgrade.redstone"));
            case "remote_interface" -> tooltip.add(Component.translatable("ic2.tooltip.upgrade.remote_interface", count));
            default -> {
            }
        }
    }

    private static void appendKnownInformationalTooltip(
            ItemStack stack, String itemPath, String variantKey, List<Component> tooltip) {
        if ("wind_meter".equals(itemPath)) {
            tooltip.add(Component.translatable("ic2.wind_meter.tooltipA"));
            tooltip.add(Component.translatable("ic2.wind_meter.tooltipB"));
            return;
        }
        if ("block_cutting_blade".equals(itemPath) && variantKey != null) {
            switch (variantKey) {
                case "block_cutting_blade/iron" -> {
                    tooltip.add(Component.translatable("ic2.IronBlockCuttingBlade.info"));
                    tooltip.add(Component.translatable("ic2.CuttingBlade.hardness", 3));
                }
                case "block_cutting_blade/steel" -> {
                    tooltip.add(Component.translatable("ic2.AdvIronBlockCuttingBlade.info"));
                    tooltip.add(Component.translatable("ic2.CuttingBlade.hardness", 6));
                }
                case "block_cutting_blade/diamond" -> {
                    tooltip.add(Component.translatable("ic2.DiamondBlockCuttingBlade.info"));
                    tooltip.add(Component.translatable("ic2.CuttingBlade.hardness", 9));
                }
                default -> {
                }
            }
            return;
        }
        if ("upgrade_kit".equals(itemPath) && "upgrade_kit/mfsu".equals(variantKey)) {
            tooltip.add(Component.translatable("ic2.upgrade_kit.mfsu.info"));
            return;
        }
        appendTransportPipeTooltip(itemPath, variantKey, tooltip);
    }

    private static void appendTransportPipeTooltip(String itemPath, String variantKey, List<Component> tooltip) {
        if (variantKey == null || !"pipe".equals(itemPath)) {
            return;
        }
        if (!variantKey.startsWith("pipe/")) {
            return;
        }
        String[] parts = variantKey.substring("pipe/".length()).split("_");
        if (parts.length != 2) {
            return;
        }
        int baseTransferRate = switch (parts[0]) {
            case "bronze" -> 2_400;
            case "steel" -> 4_800;
            default -> 0;
        };
        float multiplier = switch (parts[1]) {
            case "tiny" -> 0.16666667F;
            case "small" -> 0.33333334F;
            case "medium" -> 1.0F;
            case "large" -> 2.0F;
            default -> 0.0F;
        };
        if (baseTransferRate <= 0 || multiplier <= 0.0F) {
            return;
        }
        int rate = (int) (baseTransferRate * multiplier);
        tooltip.add(Component.literal("Transfer rate: " + rate + " mb/sec").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("Inner capacity: " + rate + " mb").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("Make connections with a wrench").withStyle(ChatFormatting.GOLD));
    }

    private static String toSiString(long value) {
        if (value >= 1_000_000L && value % 1_000_000L == 0L) {
            return (value / 1_000_000L) + "M";
        }
        if (value >= 1_000L && value % 1_000L == 0L) {
            return (value / 1_000L) + "k";
        }
        return Long.toString(value);
    }

    private LegacyItemTooltips() {
    }
}
