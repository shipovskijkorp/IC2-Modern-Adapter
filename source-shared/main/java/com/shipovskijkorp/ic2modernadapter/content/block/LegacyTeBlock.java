package com.shipovskijkorp.ic2modernadapter.content.block;

import com.shipovskijkorp.ic2modernadapter.energy.storage.AbstractEuStorageBlockEntity;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageSpec;
import com.shipovskijkorp.ic2modernadapter.generator.GeneratorBlockEntityBase;
import com.shipovskijkorp.ic2modernadapter.generator.GeneratorConstants;
import com.shipovskijkorp.ic2modernadapter.furnace.FurnaceSpec;
import com.shipovskijkorp.ic2modernadapter.furnace.AbstractIronFurnaceBlockEntity;
import com.shipovskijkorp.ic2modernadapter.furnace.AbstractInductionFurnaceBlockEntity;
import com.shipovskijkorp.ic2modernadapter.furnace.AbstractElectricFurnaceBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.AbstractStandardMachineBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.AbstractMetalFormerBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.AbstractOreWashingPlantBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.AbstractThermalCentrifugeBlockEntity;
import com.shipovskijkorp.ic2modernadapter.machine.MachineSpec;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
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

    @FunctionalInterface
    public interface MachineFactory {
        BlockEntity create(MachineSpec spec, BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    public interface FurnaceFactory {
        BlockEntity create(FurnaceSpec spec, BlockPos pos, BlockState state);
    }

    private final BiFunction<BlockPos, BlockState, ? extends BlockEntity> generatorFactory;
    private final StorageFactory storageFactory;
    private final MachineFactory machineFactory;
    private final FurnaceFactory furnaceFactory;
    private final Function<String, ItemStack> variantStackFactory;

    public LegacyTeBlock(
            Properties properties,
            int variantCount,
            ToIntFunction<ItemStack> variantResolver,
            BiFunction<BlockPos, BlockState, ? extends BlockEntity> generatorFactory,
            StorageFactory storageFactory,
            MachineFactory machineFactory,
            FurnaceFactory furnaceFactory,
            Function<String, ItemStack> variantStackFactory) {
        super(properties, variantCount, variantResolver);
        this.generatorFactory = generatorFactory;
        this.storageFactory = storageFactory;
        this.machineFactory = machineFactory;
        this.furnaceFactory = furnaceFactory;
        this.variantStackFactory = variantStackFactory;
    }

    public static boolean isGenerator(BlockState state) {
        return state.hasProperty(VARIANT) && state.getValue(VARIANT) == GeneratorConstants.VARIANT_INDEX;
    }

    public static boolean isEuStorage(BlockState state) {
        return EuStorageSpec.isStorage(state);
    }

    public static boolean isStandardMachine(BlockState state) {
        MachineSpec machine = MachineSpec.fromBlockState(state);
        return machine != null && machine.kind() == MachineSpec.Kind.STANDARD;
    }

    public static boolean isMetalFormer(BlockState state) {
        return MachineSpec.fromBlockState(state) == MachineSpec.METAL_FORMER;
    }

    public static boolean isOreWashingPlant(BlockState state) {
        return MachineSpec.fromBlockState(state) == MachineSpec.ORE_WASHING_PLANT;
    }

    public static boolean isImplementedFurnace(BlockState state) {
        return FurnaceSpec.fromBlockState(state) != null;
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
            } else {
                MachineSpec machine = MachineSpec.fromBlockState(state);
                if (machine != null) {
                    implementedVariant = machine.variantKey();
                } else {
                    FurnaceSpec furnace = FurnaceSpec.fromBlockState(state);
                    if (furnace != null) {
                        implementedVariant = furnace.variantKey();
                    }
                }
            }
        }
        if (implementedVariant == null) {
            return super.getDrops(state, params);
        }
        ItemStack drop = variantStackFactory.apply(implementedVariant);
        return drop == null || drop.isEmpty() ? List.of() : List.of(drop.copy());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) {
            return;
        }
        EuStorageSpec storageSpec = EuStorageSpec.fromBlockState(state);
        if (storageSpec == null) {
            return;
        }
        long stored = Math.min(storageSpec.capacityEu(), IC2VariantStacks.blockEntityEnergy(stack));
        if (stored <= 0L) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AbstractEuStorageBlockEntity storage) {
            storage.setStoredEnergyFromItem(stored);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (isGenerator(state)) {
            return generatorFactory.apply(pos, state);
        }
        EuStorageSpec storage = EuStorageSpec.fromBlockState(state);
        if (storage != null) {
            return storageFactory.create(storage, pos, state);
        }
        MachineSpec machine = MachineSpec.fromBlockState(state);
        if (machine != null) {
            return machineFactory.create(machine, pos, state);
        }
        FurnaceSpec furnace = FurnaceSpec.fromBlockState(state);
        return furnace == null ? null : furnaceFactory.create(furnace, pos, state);
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
        MachineSpec machineSpec = MachineSpec.fromBlockState(state);
        if (machineSpec != null) {
            return (tickLevel, pos, tickState, blockEntity) -> {
                if (blockEntity instanceof AbstractStandardMachineBlockEntity machine) {
                    machine.serverTick();
                } else if (blockEntity instanceof AbstractMetalFormerBlockEntity metalFormer) {
                    metalFormer.serverTick();
                } else if (blockEntity instanceof AbstractOreWashingPlantBlockEntity oreWasher) {
                    oreWasher.serverTick();
                } else if (blockEntity instanceof AbstractThermalCentrifugeBlockEntity centrifuge) {
                    centrifuge.serverTick();
                }
            };
        }
        if (isImplementedFurnace(state)) {
            return (tickLevel, pos, tickState, blockEntity) -> {
                if (blockEntity instanceof AbstractIronFurnaceBlockEntity furnace) {
                    furnace.serverTick();
                } else if (blockEntity instanceof AbstractElectricFurnaceBlockEntity furnace) {
                    furnace.serverTick();
                } else if (blockEntity instanceof AbstractInductionFurnaceBlockEntity furnace) {
                    furnace.serverTick();
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
        MachineSpec oldMachine = MachineSpec.fromBlockState(state);
        MachineSpec newMachine = newState.is(this) ? MachineSpec.fromBlockState(newState) : null;
        boolean removeMachine = oldMachine != null && oldMachine != newMachine;
        FurnaceSpec oldFurnace = FurnaceSpec.fromBlockState(state);
        FurnaceSpec newFurnace = newState.is(this) ? FurnaceSpec.fromBlockState(newState) : null;
        boolean removeFurnace = oldFurnace != null && oldFurnace != newFurnace;

        if ((removeGenerator || removeStorage || removeMachine || removeFurnace) && !level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof GeneratorBlockEntityBase generator) {
                Containers.dropContents(level, pos, generator);
            } else if (blockEntity instanceof AbstractEuStorageBlockEntity storage) {
                Containers.dropContents(level, pos, storage);
            } else if (blockEntity instanceof AbstractStandardMachineBlockEntity machine) {
                Containers.dropContents(level, pos, machine);
            } else if (blockEntity instanceof AbstractMetalFormerBlockEntity metalFormer) {
                Containers.dropContents(level, pos, metalFormer);
            } else if (blockEntity instanceof AbstractOreWashingPlantBlockEntity oreWasher) {
                Containers.dropContents(level, pos, oreWasher);
            } else if (blockEntity instanceof AbstractThermalCentrifugeBlockEntity centrifuge) {
                Containers.dropContents(level, pos, centrifuge);
            } else if (blockEntity instanceof AbstractIronFurnaceBlockEntity furnace) {
                Containers.dropContents(level, pos, furnace);
            } else if (blockEntity instanceof AbstractElectricFurnaceBlockEntity furnace) {
                Containers.dropContents(level, pos, furnace);
            } else if (blockEntity instanceof AbstractInductionFurnaceBlockEntity furnace) {
                Containers.dropContents(level, pos, furnace);
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
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (isEuStorage(state)) {
            return blockEntity instanceof AbstractEuStorageBlockEntity storage
                    ? storage.getRedstoneOutputLevel()
                    : 0;
        }
        return blockEntity instanceof AbstractInductionFurnaceBlockEntity furnace
                ? furnace.getComparatorLevel()
                : 0;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return isEuStorage(state) || FurnaceSpec.fromBlockState(state) == FurnaceSpec.INDUCTION;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AbstractEuStorageBlockEntity storage) {
            return storage.getComparatorLevel();
        }
        return blockEntity instanceof AbstractInductionFurnaceBlockEntity furnace
                ? furnace.getComparatorLevel()
                : 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        boolean showFlames = isGenerator(state) || FurnaceSpec.fromBlockState(state) == FurnaceSpec.IRON;
        if (!showFlames || !state.getValue(ACTIVE) || random.nextInt(8) != 0) {
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
