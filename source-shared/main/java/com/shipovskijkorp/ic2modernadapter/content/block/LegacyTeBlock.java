package com.shipovskijkorp.ic2modernadapter.content.block;

import com.shipovskijkorp.ic2modernadapter.energy.storage.AbstractEuStorageBlockEntity;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageSpec;
import com.shipovskijkorp.ic2modernadapter.generator.GeneratorBlockEntityBase;
import com.shipovskijkorp.ic2modernadapter.generator.GeneratorConstants;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;

/**
 * Entity-capable form of the legacy {@code ic2:te} root block.
 *
 * <p>Implemented subtypes attach real block entities while all unfinished siblings retain their
 * placeholder identity and runtime-compiled original models.</p>
 */
public final class LegacyTeBlock extends LegacyVariantFacingBlock implements EntityBlock {
    @FunctionalInterface
    public interface StorageFactory {
        BlockEntity create(EuStorageSpec spec, BlockPos pos, BlockState state);
    }

    private final BiFunction<BlockPos, BlockState, ? extends BlockEntity> generatorFactory;
    private final StorageFactory storageFactory;
    private final Function<String, ItemStack> variantStackFactory;

    public LegacyTeBlock(
            Properties properties,
            int variantCount,
            ToIntFunction<ItemStack> variantResolver,
            BiFunction<BlockPos, BlockState, ? extends BlockEntity> generatorFactory,
            StorageFactory storageFactory,
            Function<String, ItemStack> variantStackFactory) {
        super(properties, variantCount, variantResolver);
        this.generatorFactory = generatorFactory;
        this.storageFactory = storageFactory;
        this.variantStackFactory = variantStackFactory;
    }

    public static boolean isGenerator(BlockState state) {
        return state.hasProperty(VARIANT) && state.getValue(VARIANT) == GeneratorConstants.VARIANT_INDEX;
    }

    public static boolean isEuStorage(BlockState state) {
        return EuStorageSpec.isStorage(state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        String implementedVariant = null;
        if (isGenerator(state)) {
            implementedVariant = GeneratorConstants.VARIANT_KEY;
        } else {
            EuStorageSpec storage = EuStorageSpec.fromBlockState(state);
            if (storage != null) {
                implementedVariant = storage.variantKey();
            }
        }
        if (implementedVariant == null) {
            return super.getDrops(state, params);
        }
        ItemStack drop = variantStackFactory.apply(implementedVariant);
        return drop == null || drop.isEmpty() ? List.of() : List.of(drop.copy());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (isGenerator(state)) {
            return generatorFactory.apply(pos, state);
        }
        EuStorageSpec storage = EuStorageSpec.fromBlockState(state);
        return storage == null ? null : storageFactory.create(storage, pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        if (isGenerator(state)) {
            return (tickLevel, pos, tickState, blockEntity) -> {
                if (blockEntity instanceof GeneratorBlockEntityBase generator) {
                    generator.serverTick();
                }
            };
        }
        if (isEuStorage(state)) {
            return (tickLevel, pos, tickState, blockEntity) -> {
                if (blockEntity instanceof AbstractEuStorageBlockEntity storage) {
                    storage.serverTick();
                }
            };
        }
        return null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        boolean removeGenerator = isGenerator(state) && (!newState.is(this) || !isGenerator(newState));
        EuStorageSpec oldStorage = EuStorageSpec.fromBlockState(state);
        EuStorageSpec newStorage = newState.is(this) ? EuStorageSpec.fromBlockState(newState) : null;
        boolean removeStorage = oldStorage != null && oldStorage != newStorage;

        if ((removeGenerator || removeStorage) && !level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof GeneratorBlockEntityBase generator) {
                Containers.dropContents(level, pos, generator);
            } else if (blockEntity instanceof AbstractEuStorageBlockEntity storage) {
                Containers.dropContents(level, pos, storage);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return isEuStorage(state);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!isEuStorage(state)) {
            return 0;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof AbstractEuStorageBlockEntity storage
                ? storage.getRedstoneOutputLevel()
                : 0;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return isEuStorage(state);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof AbstractEuStorageBlockEntity storage
                ? storage.getComparatorLevel()
                : 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (!isGenerator(state) || !state.getValue(ACTIVE) || random.nextInt(8) != 0) {
            return;
        }

        Direction facing = state.getValue(FACING);
        // Original TileEntityIronFurnace.showFlames geometry used by TileEntityGenerator.
        double x = pos.getX() + (facing.getStepX() * 1.04D + 1.0D) / 2.0D;
        double y = pos.getY() + random.nextFloat() * 0.375D;
        double z = pos.getZ() + (facing.getStepZ() * 1.04D + 1.0D) / 2.0D;
        if (facing.getAxis() == Direction.Axis.X) {
            z += random.nextFloat() * 0.625D - 0.3125D;
        } else {
            x += random.nextFloat() * 0.625D - 0.3125D;
        }
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
    }
}
