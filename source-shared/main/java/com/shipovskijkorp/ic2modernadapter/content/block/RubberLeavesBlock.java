package com.shipovskijkorp.ic2modernadapter.content.block;

import net.minecraft.world.level.block.LeavesBlock;

/** Vanilla leaf distance/decay with persistent defaults for old adapter decorative leaves. */
public final class RubberLeavesBlock extends LeavesBlock {
    public RubberLeavesBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(PERSISTENT, true));
    }
}
