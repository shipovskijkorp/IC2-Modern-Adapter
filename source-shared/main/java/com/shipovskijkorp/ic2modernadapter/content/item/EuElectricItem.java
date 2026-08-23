package com.shipovskijkorp.ic2modernadapter.content.item;

import com.shipovskijkorp.ic2modernadapter.energy.item.EuElectricItemSpec;
import com.shipovskijkorp.ic2modernadapter.energy.item.IEuElectricItem;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/** Native IC2MA implementation of IC2 chargeable batteries and crystals. */
public final class EuElectricItem extends LegacyTranslatedItem implements IEuElectricItem {
    private final EuElectricItemSpec spec;

    public EuElectricItem(
            String itemPath,
            EuElectricItemSpec spec,
            Properties properties,
            Function<ItemStack, String> variantResolver) {
        super(itemPath, properties, variantResolver);
        this.spec = Objects.requireNonNull(spec, "spec");
    }

    public EuElectricItemSpec spec() {
        return spec;
    }

    @Override
    public int getEuTier(ItemStack stack) {
        return spec.tier();
    }

    @Override
    public long getEuStored(ItemStack stack) {
        return clamp(IC2VariantStacks.euStored(stack));
    }

    @Override
    public long getEuCapacity(ItemStack stack) {
        return spec.capacityEu();
    }

    @Override
    public long getEuTransferLimit(ItemStack stack) {
        return spec.transferLimitEu();
    }

    @Override
    public long insertEu(ItemStack stack, long amount, boolean simulate) {
        if (stack == null || stack.isEmpty() || stack.getCount() != 1 || amount <= 0L) {
            return 0L;
        }
        long stored = getEuStored(stack);
        long accepted = Math.min(amount, spec.capacityEu() - stored);
        if (accepted <= 0L) {
            return 0L;
        }
        if (!simulate) {
            IC2VariantStacks.setEuStored(stack, stored + accepted);
        }
        return accepted;
    }

    @Override
    public long extractEu(ItemStack stack, long amount, boolean simulate) {
        if (stack == null || stack.isEmpty() || stack.getCount() != 1 || amount <= 0L) {
            return 0L;
        }
        long stored = getEuStored(stack);
        long extracted = Math.min(amount, stored);
        if (extracted <= 0L) {
            return 0L;
        }
        if (!simulate) {
            IC2VariantStacks.setEuStored(stack, stored - extracted);
        }
        return extracted;
    }

    @Override
    public boolean canProvideEu(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canChargeFromTier(ItemStack stack, int chargerTier) {
        return stack != null
                && !stack.isEmpty()
                && stack.getCount() == 1
                && IEuElectricItem.super.canChargeFromTier(stack, chargerTier);
    }

    @Override
    public boolean canDischargeToTier(ItemStack stack, int receiverTier) {
        return stack != null
                && !stack.isEmpty()
                && stack.getCount() == 1
                && IEuElectricItem.super.canDischargeToTier(stack, receiverTier);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getEuStored(stack) > 0L;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (spec.capacityEu() <= 0L) {
            return 0;
        }
        return Math.round(13.0F * (float) getEuStored(stack) / (float) spec.capacityEu());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float ratio = spec.capacityEu() <= 0L ? 0.0F : (float) getEuStored(stack) / (float) spec.capacityEu();
        return Mth.hsvToRgb(Math.max(0.0F, ratio) / 3.0F, 1.0F, 1.0F);
    }

    private long clamp(long value) {
        return Math.max(0L, Math.min(spec.capacityEu(), value));
    }
}
