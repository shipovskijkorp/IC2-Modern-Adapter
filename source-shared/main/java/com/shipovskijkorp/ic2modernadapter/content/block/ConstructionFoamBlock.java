package com.shipovskijkorp.ic2modernadapter.content.block;

import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Construction foam cures into a wall or reinforced stone, retaining IC2's light-dependent timing. */
public class ConstructionFoamBlock extends LegacyVariantBlock {
    public ConstructionFoamBlock(Properties properties, ToIntFunction<ItemStack> resolver) {
        super(properties, 2, resolver);
    }

    public static BlockState stateFor(String key) {
        String path = key.substring(0, key.indexOf('/'));
        return IC2ContentRegistries.block(path).get().defaultBlockState().setValue(VARIANT,
                OriginalContentManifest.get().stackVariantIndex(key));
    }

    private static boolean reinforced(BlockState state) {
        return state.getValue(VARIANT) == OriginalContentManifest.get().stackVariantIndex("foam/reinforced");
    }

    private static boolean harden(Level level, BlockPos pos, BlockState state) {
        return level.setBlock(pos, stateFor(reinforced(state) ? "resource/reinforced_stone" : "wall/light_gray"), Block.UPDATE_ALL);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int speed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        if (speed <= 0) return;
        int light = level.getMaxLocalRawBrightness(pos);
        for (Direction side : Direction.values()) {
            light = Math.max(light, level.getMaxLocalRawBrightness(pos.relative(side)));
        }
        int seconds = reinforced(state) ? 600 : 300;
        double chance = 4096.0 / (speed * seconds * 20.0 * (16 - Math.min(15, light)));
        if (random.nextDouble() < chance) harden(level, pos, state);
    }

    protected boolean trySand(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
        if (!stack.is(Items.SAND) && !stack.is(Items.RED_SAND)) return false;
        if (!level.mayInteract(player, pos)) return false;
        if (level.isClientSide()) return true;
        if (!harden(level, pos, state)) return false;
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return true;
    }
}
