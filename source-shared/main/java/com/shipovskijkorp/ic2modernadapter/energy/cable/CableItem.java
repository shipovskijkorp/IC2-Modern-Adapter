package com.shipovskijkorp.ic2modernadapter.energy.cable;

import com.shipovskijkorp.ic2modernadapter.content.item.LegacyTranslatedBlockItem;
import java.util.function.Function;
import net.minecraft.world.item.ItemStack;

/**
 * Original standalone {@code ic2:cable} item backed by IC2MA's internal cable carrier block.
 *
 * <p>Keeping this as a real BlockItem is important: placement goes through vanilla/loader block
 * placement hooks, pick-block resolves back to {@code ic2:cable}, and the variant-bearing stack is
 * still available to {@link CableBlock#getStateForPlacement}.</p>
 */
public final class CableItem extends LegacyTranslatedBlockItem {
    public CableItem(
            Properties properties,
            CableBlock block,
            Function<ItemStack, String> variantResolver) {
        super("cable", block, properties, variantResolver);
    }
}
