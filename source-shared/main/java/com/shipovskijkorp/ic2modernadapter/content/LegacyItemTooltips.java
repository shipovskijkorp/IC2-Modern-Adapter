package com.shipovskijkorp.ic2modernadapter.content;

import com.shipovskijkorp.ic2modernadapter.energy.cable.EuCableVariant;
import com.shipovskijkorp.ic2modernadapter.energy.item.IEuElectricItem;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageSpec;
import com.shipovskijkorp.ic2modernadapter.energy.util.OriginalEnergyFormat;
import com.shipovskijkorp.ic2modernadapter.energy.util.EuUtil;
import com.shipovskijkorp.ic2modernadapter.content.item.EuElectricItem;
import com.shipovskijkorp.ic2modernadapter.content.item.FoamCharge;
import com.shipovskijkorp.ic2modernadapter.machine.Ic2FluidKind;
import com.shipovskijkorp.ic2modernadapter.machine.MachineSpec;
import com.shipovskijkorp.ic2modernadapter.furnace.FurnaceSpec;
import net.minecraft.nbt.CompoundTag;
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
    private static String decimal(String pattern, double value) {
        return new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.ROOT)).format(value);
    }

    public static void append(
            ItemStack stack,
            String namespace,
            String itemPath,
            String variantKey,
            List<Component> tooltip,
            boolean showPowerTier, boolean inToolbox, Component attackKey, Component useKey) {
        if (stack == null || stack.isEmpty()) return;
        if (!"ic2".equals(namespace)) {
            if (com.shipovskijkorp.ic2modernadapter.energy.item.AttachedJetpackEnergy.attached(stack))
                appendElectricTooltip(stack, tooltip, showPowerTier);
            return;
        }

        appendCableTooltip(variantKey, tooltip);
        appendMachineTooltip(stack, variantKey, tooltip);
        if (stack.getItem() instanceof EuElectricItem electric && electric.spec().chargesHotbar()) {
            tooltip.add(Component.translatable(EuElectricItem.modeKey(stack)));
            if (inToolbox) tooltip.add(Component.translatable("ic2.tooltip.mode.boxable")
                    .withStyle(IC2VariantStacks.chargeMode(stack) == 1 ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
        appendCraftingToolTooltip(stack, itemPath, tooltip);
        appendUpgradeTooltip(stack, itemPath, variantKey, tooltip);
        appendKnownInformationalTooltip(stack, itemPath, variantKey, tooltip);
        appendOriginalDetails(stack, itemPath, variantKey, tooltip, attackKey, useKey);
        // Original item-specific lines precede the charge/tier tooltip event.
        appendElectricTooltip(stack, tooltip, showPowerTier);
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
        tooltip.add(Component.translatable("ic2.cable.tooltip.loss", decimal("0.00#", cable.loss())));
    }

    private static Component tier(int tier) {
        return Component.translatable("ic2.item.tooltip.PowerTier", tier);
    }

    private static void appendMachineTooltip(ItemStack stack, String variantKey, List<Component> tooltip) {
        if (variantKey == null || !variantKey.startsWith("te/")) return;
        EuStorageSpec storage = EuStorageSpec.fromVariantKey(variantKey);
        int machineTier = machineTier(variantKey);
        if (machineTier > 0) tooltip.add(tier(machineTier));
        int transformerTier = switch (variantKey) {
            case "te/lv_transformer" -> 1;
            case "te/mv_transformer" -> 2;
            case "te/hv_transformer" -> 3;
            case "te/ev_transformer" -> 4;
            default -> 0;
        };
        if (transformerTier != 0) {
            tooltip.add(Component.empty().append(Component.translatable("ic2.item.tooltip.Low"))
                    .append(" " + EuUtil.powerFromTier(transformerTier) + " ")
                    .append(Component.translatable("ic2.generic.text.EUt")).append(" ")
                    .append(Component.translatable("ic2.item.tooltip.High"))
                    .append(" " + EuUtil.powerFromTier(transformerTier + 1) + " ")
                    .append(Component.translatable("ic2.generic.text.EUt")));
        }
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
        IEuElectricItem electricItem = com.shipovskijkorp.ic2modernadapter.energy.item.EuItemManager.electric(stack);
        if (electricItem == null) {
            return;
        }
        if (com.shipovskijkorp.ic2modernadapter.energy.item.AttachedJetpackEnergy.attached(stack)) {
            tooltip.add(Component.translatable("ic2.jetpackAttached").withStyle(ChatFormatting.YELLOW));
        }
        if (stack.getItem() instanceof com.shipovskijkorp.ic2modernadapter.content.item.EuDebugItem) {
            tooltip.add(Component.literal("infinite EU"));
        } else {
            tooltip.add(Component.literal(OriginalEnergyFormat.item(electricItem.getEuStored(stack), electricItem.getEuCapacity(stack))));
        }
        if (showPowerTier) {
            tooltip.add(Component.translatable("ic2.item.tooltip.PowerTier", electricItem.getEuTier(stack)));
        }
    }

    private static void appendCraftingToolTooltip(ItemStack stack, String itemPath, List<Component> tooltip) {
        if (!"cutter".equals(itemPath) && !"forge_hammer".equals(itemPath)) {
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
                tooltip.add(Component.translatable("ic2.tooltip.upgrade.overclocker.time", decimal("0.##", 100 * Math.pow(0.7, count))));
                tooltip.add(Component.translatable("ic2.tooltip.upgrade.overclocker.power", decimal("0.##", 100 * Math.pow(1.6, count))));
            }
            case "transformer" -> tooltip.add(Component.translatable("ic2.tooltip.upgrade.transformer", count));
            case "energy_storage" -> tooltip.add(Component.translatable("ic2.tooltip.upgrade.storage", 10_000 * count));
            case "ejector", "advanced_ejector", "fluid_ejector" -> tooltip.add(Component.translatable(
                    "ic2.tooltip.upgrade.ejector",
                    upgradeDirection(stack)));
            case "pulling", "advanced_pulling", "fluid_pulling" -> tooltip.add(Component.translatable(
                    "ic2.tooltip.upgrade.pulling",
                    upgradeDirection(stack)));
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

    private static Component upgradeDirection(ItemStack stack) {
        return Component.translatable(switch (IC2VariantStacks.electricData(stack).getByte("dir")) {
            case 1 -> "ic2.dir.bottom";
            case 2 -> "ic2.dir.top";
            case 3 -> "ic2.dir.north";
            case 4 -> "ic2.dir.south";
            case 5 -> "ic2.dir.west";
            case 6 -> "ic2.dir.east";
            default -> "ic2.tooltip.upgrade.ejector.anyside";
        });
    }

    private static int machineTier(String key) {
        EuStorageSpec storage = EuStorageSpec.fromVariantKey(key);
        if (storage != null) return storage.tier();
        MachineSpec machine = MachineSpec.fromVariantKey(key);
        if (machine != null) return machine.tier();
        for (FurnaceSpec furnace : FurnaceSpec.values()) {
            if (furnace.variantKey().equals(key)) return furnace.tier();
        }
        // Original constructors of not-yet-implemented tiles, with default IC2 config.
        // No claimed tier for tiles which have no Energy component.
        return switch (key) {
            case "te/generator", "te/geo_generator", "te/rt_generator", "te/semifluid_generator",
                 "te/solar_generator", "te/water_generator", "te/wind_generator", "te/fluid_bottler",
                 "te/pump", "te/luminator_flat", "te/magnetizer", "te/recycler", "te/block_cutter",
                 "te/crop_harvester", "te/cropmatron", "te/miner", "te/chunk_loader", "te/batch_crafter" -> 1;
            case "te/sorting_machine", "te/tesla_coil", "te/electrolyzer" -> 2;
            case "te/advanced_miner", "te/matter_generator" -> 3;
            case "te/electric_heat_generator", "te/electric_kinetic_generator",
                 "te/fluid_regulator", "te/terraformer", "te/replicator", "te/scanner" -> 4;
            case "te/condenser" -> 3;
            default -> 0;
        };
    }

    private static void appendOriginalDetails(ItemStack stack, String path, String key,
            List<Component> tooltip, Component attackKey, Component useKey) {
        CompoundTag data = IC2VariantStacks.electricData(stack);
        switch (path) {
            case "scanner" -> tooltip.add(Component.translatable("ic2.scanner.range", 6));
            case "advanced_scanner" -> tooltip.add(Component.translatable("ic2.scanner.range", 12));
            case "rotor_wood" -> rotor(tooltip, 10, 60);
            case "rotor_bronze", "rotor_iron" -> rotor(tooltip, 14, 75);
            case "rotor_steel" -> rotor(tooltip, 17, 90);
            case "rotor_carbon" -> rotor(tooltip, 20, 110);
            case "frequency_transmitter" -> tooltip.add(data.getBoolean("targetSet")
                    ? Component.translatable("ic2.frequency_transmitter.tooltip.target", data.getInt("targetX"), data.getInt("targetY"), data.getInt("targetZ"))
                    : Component.translatable("ic2.frequency_transmitter.tooltip.blank"));
            case "crystal_memory" -> {
                // No recorded pattern can be created until the original scanner is implemented.
                tooltip.add(Component.translatable("ic2.item.CrystalMemory.tooltip.Empty"));
            }
            case "mining_laser" -> {
                String mode = switch (data.getInt("laserSetting")) {
                    case 0 -> "mining"; case 1 -> "lowFocus"; case 2 -> "longRange"; case 3 -> "horizontal";
                    case 4 -> "superHeat"; case 5 -> "scatter"; case 6 -> "explosive"; case 7 -> "3x3";
                    default -> null;
                };
                if (mode != null) tooltip.add(Component.translatable("ic2.tooltip.mode", Component.translatable("ic2.tooltip.mode." + mode)));
            }
            case "fluid_cell" -> {
                Ic2FluidKind fluid = key != null && key.startsWith("fluid_cell/")
                        ? Ic2FluidKind.byKey(key.substring("fluid_cell/".length())) : Ic2FluidKind.EMPTY;
                fluidTooltip(tooltip, fluid, fluid.isEmpty() ? 0 : 1000);
            }
            case "foam_sprayer" -> fluidTooltip(tooltip, Ic2FluidKind.CONSTRUCTION_FOAM, FoamCharge.get(stack));
            case "cf_pack" -> fluidTooltip(tooltip, Ic2FluidKind.EMPTY, 0);
            case "cover" -> {
                if ("cover/pump_lv".equals(key)) tooltip.add(Component.literal("Transfer rate: 640 mb/sec (as attachment)"));
                if ("cover/pump_mv".equals(key)) tooltip.add(Component.literal("Transfer rate: 2560 mb/sec (as attachment)"));
            }
            case "painter" -> {
                if (key != null && key.startsWith("painter/") && !key.equals("painter/blank")) {
                    tooltip.add(Component.translatable("item.minecraft." + key.substring("painter/".length()) + "_dye"));
                }
            }
            case "remote" -> {
                if (stack.getDamageValue() > 0) tooltip.add(Component.literal("Linked to " + stack.getDamageValue() + " dynamite"));
            }
            case "crowbar" -> {
                tooltip.add(Component.empty().append(useKey).append(":"));
                tooltip.add(Component.literal(" Remove attachments from blocks"));
            }
            case "wrench_new" -> {
                tooltip.add(Component.empty().append(attackKey).append(":"));
                tooltip.add(Component.literal(" Safely mine IC2 machines (Yes you will get the machine and not the machine block)"));
                tooltip.add(Component.empty());
                tooltip.add(Component.empty().append(useKey).append(":"));
                tooltip.add(Component.literal(" Set the machine facing (rotate)"));
                tooltip.add(Component.literal(" Connect pipes together and to covers"));
            }
            default -> { }
        }
        int maximum = switch (path) {
            case "heat_storage", "advanced_heat_exchanger", "lithium_fuel_rod", "depleted_isotope_fuel_rod",
                 "mox_fuel_rod", "dual_mox_fuel_rod", "quad_mox_fuel_rod" -> 10000;
            case "tri_heat_storage", "neutron_reflector" -> 30000;
            case "hex_heat_storage" -> 60000;
            case "heat_exchanger" -> 2500;
            case "reactor_heat_exchanger", "component_heat_exchanger" -> 5000;
            case "heat_vent", "reactor_heat_vent", "overclocked_heat_vent", "advanced_heat_vent" -> 1000;
            case "rsh_condensator", "uranium_fuel_rod", "dual_uranium_fuel_rod", "quad_uranium_fuel_rod" -> 20000;
            case "lzh_condensator" -> 100000;
            case "thick_neutron_reflector" -> 120000;
            default -> 0;
        };
        if (maximum > 0) {
            int damage = Math.max(0, Math.min(maximum,
                    stack.isDamageableItem() ? stack.getDamageValue() : data.getInt("advDmg")));
            tooltip.add(Component.empty().append(Component.translatable("ic2.reactoritem.durability"))
                    .append(" " + (maximum - damage) + "/" + maximum));
            if (damage > 0 && (path.endsWith("heat_storage") || path.endsWith("heat_exchanger") || path.endsWith("heat_vent"))) {
                tooltip.add(Component.translatable("ic2.reactoritem.heatwarning.line1"));
                tooltip.add(Component.translatable("ic2.reactoritem.heatwarning.line2"));
            }
        }
        if (key == null) return;
        int box = switch (key) {
            case "te/wooden_storage_box" -> 27;
            case "te/bronze_storage_box", "te/iron_storage_box" -> 45;
            case "te/steel_storage_box" -> 63;
            case "te/iridium_storage_box" -> 126;
            default -> 0;
        };
        if (box > 0) {
            tooltip.add(Component.literal("Stores items even when broken"));
            tooltip.add(Component.literal("Inventory size: " + box));
        }
        int tank = switch (key) {
            case "te/bronze_tank", "te/iron_tank" -> 32000;
            case "te/steel_tank" -> 128000;
            case "te/iridium_tank" -> 1024000;
            default -> 0;
        };
        if (tank > 0) {
            // Tank mechanics and item serialization are not implemented yet.
            tooltip.add(Component.literal("Capacity: " + tank + " mB"));
            tooltip.add(Component.literal("Empty"));
        }
        if (key.equals("te/uu_assembly_bench") || key.equals("te/mass_fabricator")) {
            tooltip.add(Component.literal("You probably want the ").append(Component.translatable(
                    key.equals("te/uu_assembly_bench") ? "ic2.te.replicator" : "ic2.te.matter_generator")));
        }
        if (key.equals("te/coke_kiln")) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("MultiBlock Structure:"));
            tooltip.add(Component.empty());
            tooltip.add(Component.literal(" Bottom Layer - 3x3 of Refractory Blocks with a Coke Kiln Grate in the centre"));
            tooltip.add(Component.empty());
            tooltip.add(Component.literal(" Middle Layer - 3x3 of Refractory Blocks with a hollow centre and this block in the middle of one of the sides"));
            tooltip.add(Component.empty());
            tooltip.add(Component.literal(" Top Layer - 3x3 of Refractory Blocks with a Coke Kiln Hatch in the centre"));
            tooltip.add(Component.empty());
        }
    }

    private static void rotor(List<Component> tooltip, int min, int max) {
        tooltip.add(Component.translatable("ic2.itemrotor.wind.info", min, max));
    }

    private static void fluidTooltip(List<Component> tooltip, Ic2FluidKind fluid, int amount) {
        if (fluid.isEmpty() || amount <= 0) {
            tooltip.add(Component.translatable("ic2.item.FluidContainer.Empty"));
            return;
        }
        Component name = switch (fluid) {
            case WATER -> Component.translatable("block.minecraft.water");
            case LAVA -> Component.translatable("block.minecraft.lava");
            default -> Component.translatable("ic2." + fluid.key());
        };
        tooltip.add(Component.literal("< ").append(name).append(", " + amount + " mB >"));
    }

    private LegacyItemTooltips() {
    }
}
