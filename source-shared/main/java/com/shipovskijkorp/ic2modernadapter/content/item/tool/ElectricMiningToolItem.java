package com.shipovskijkorp.ic2modernadapter.content.item.tool;

import com.shipovskijkorp.ic2modernadapter.content.item.EuElectricItem;
import com.shipovskijkorp.ic2modernadapter.energy.item.EuElectricItemSpec;
import com.shipovskijkorp.ic2modernadapter.energy.item.EuItemManager;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Shearable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Powered drill, chainsaw, hoe and Nano Saber. All EU transfers go through the shared manager. */
public final class ElectricMiningToolItem extends EuElectricItem {
    private final ElectricToolRules rules;
    public ElectricMiningToolItem(String path, EuElectricItemSpec spec, Properties properties, Function<ItemStack, String> variants) {
        super(path, spec, ToolVersionSupport.toolProperties(properties.stacksTo(1), ElectricToolRules.valueOf(spec.name())), variants);
        this.rules = ElectricToolRules.valueOf(spec.name());
    }
    public ElectricToolRules rules() { return rules; }
    public static boolean active(ItemStack stack) { return ToolData.flag(stack, "active"); }
    private static boolean shearDisabled(ItemStack stack) { return ToolData.flag(stack, "disableShear"); }
    private boolean powered(ItemStack stack) { return EuItemManager.canUse(stack, rules.cost); }
    private boolean effective(BlockState state) {
        if (rules.drill()) return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
        return switch (rules) {
            case CHAINSAW -> state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LEAVES) || state.is(BlockTags.WOOL)
                    || state.is(Blocks.COBWEB) || state.is(Blocks.VINE) || state.is(Blocks.GLOW_LICHEN)
                    || state.is(Blocks.REDSTONE_WIRE) || state.is(Blocks.TRIPWIRE) || state.is(Blocks.PUMPKIN)
                    || state.is(Blocks.CARVED_PUMPKIN) || state.is(Blocks.MELON) || shearableBlock(state);
            case ELECTRIC_HOE -> state.is(BlockTags.MINEABLE_WITH_HOE);
            case NANO_SABER -> state.is(Blocks.COBWEB);
            default -> false;
        };
    }
    @Override public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (rules == ElectricToolRules.NANO_SABER) return active(stack) && powered(stack) ? (state.is(Blocks.COBWEB) ? 50F : 4F) : 1F;
        return powered(stack) && effective(state) ? rules.speed : 1F;
    }
    private boolean canHarvest(BlockState state) {
        if (!effective(state)) return false;
        return ToolVersionSupport.harvestAllowed(state, rules.harvest);
    }
    // Both signatures are supplied: 1.20.1 has a state-only method, 1.21.1 uses the stack too.
    public boolean isCorrectToolForDrops(BlockState state) { return canHarvest(state); }
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) { return canHarvest(state); }
    @Override public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0 && !creative(miner)) {
            if (rules == ElectricToolRules.NANO_SABER) {
                if (active(stack)) drainSaber(stack, rules.cost, miner);
            } else EuItemManager.use(stack, rules.cost, miner);
            ToolVersionSupport.updateAttributes(stack, rules);
        }
        return true;
    }
    private static boolean creative(LivingEntity user) { return user instanceof Player p && p.getAbilities().instabuild; }
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide() && !creative(attacker)) {
            if (rules == ElectricToolRules.CHAINSAW) EuItemManager.use(stack, 100, attacker);
            if (rules == ElectricToolRules.NANO_SABER && active(stack)) {
                if (drainSaber(stack, 400, attacker)) {
                    for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                        if (!EuItemManager.canUse(stack, 2000)) break;
                        ItemStack armor = target.getItemBySlot(slot);
                        var electric = EuItemManager.electric(armor);
                        if (!(electric instanceof com.shipovskijkorp.ic2modernadapter.energy.item.SpecElectricItem spec)) continue;
                        String path = spec.spec().itemPath();
                        if (!path.startsWith("nano_") && !path.startsWith("quantum_")) continue;
                        long amount = path.startsWith("nano_") ? 48_000 : 300_000;
                        // The original limits discharge to the saber's tier. Do not delete a higher-tier item.
                        long drained = EuItemManager.discharge(armor, amount, spec().tier(), true, false, false);
                        if (drained > 0) drainSaber(stack, 2000, attacker);
                    }
                }
            }
            ToolVersionSupport.updateAttributes(stack, rules);
        }
        return true;
    }
    private static boolean drainSaber(ItemStack stack, long amount, LivingEntity user) {
        if (EuItemManager.use(stack, amount, user)) return true;
        ToolData.flag(stack, "active", false);
        return false;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (rules != ElectricToolRules.NANO_SABER) return super.use(level, player, hand);
        if (!level.isClientSide()) {
            EuItemManager.chargeFromArmor(stack, player);
            boolean on = active(stack);
            if (!on && !EuItemManager.canUse(stack, 16)) return InteractionResultHolder.fail(stack);
            ToolData.flag(stack, "active", !on);
            ToolVersionSupport.updateAttributes(stack, rules);
            player.getInventory().setChanged();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide() || !(entity instanceof Player player)) return;
        if (selected && !creative(player)) EuItemManager.chargeFromArmor(stack, player);
        if (rules == ElectricToolRules.IRIDIUM_DRILL) ToolVersionSupport.ensureIridiumEnchantments(stack, level, ToolData.flag(stack, "silkTouch"));
        if (rules == ElectricToolRules.NANO_SABER && active(stack) && !creative(player)) {
            int cost = ElectricToolRules.saberIdleCost(level.getGameTime(), slot);
            if (cost != 0) drainSaber(stack, cost, player);
        }
        ToolVersionSupport.updateAttributes(stack, rules);
    }
    @Override public InteractionResult useOn(UseOnContext ctx) {
        if (rules != ElectricToolRules.ELECTRIC_HOE) return InteractionResult.PASS;
        Player player = ctx.getPlayer(); Level level = ctx.getLevel(); BlockPos pos = ctx.getClickedPos();
        if (player == null || !player.mayBuild() || !level.mayInteract(player, pos) || ctx.getClickedFace() == Direction.DOWN
                || !level.getBlockState(pos.above()).isAir() || !player.mayUseItemAt(pos, ctx.getClickedFace(), ctx.getItemInHand())) return InteractionResult.PASS;
        BlockState old = level.getBlockState(pos);
        BlockState tilled = old.is(Blocks.COARSE_DIRT) || old.is(Blocks.ROOTED_DIRT) ? Blocks.DIRT.defaultBlockState()
                : old.is(Blocks.GRASS_BLOCK) || old.is(Blocks.DIRT) || old.is(Blocks.DIRT_PATH) || old.is(Blocks.MYCELIUM)
                || old.is(Blocks.PODZOL) ? Blocks.FARMLAND.defaultBlockState() : null;
        if (tilled == null) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack stack = ctx.getItemInHand();
        EuItemManager.chargeFromArmor(stack, player);
        if (!creative(player) && !powered(stack)) return InteractionResult.FAIL;
        if (!level.setBlock(pos, tilled, Block.UPDATE_ALL)) return InteractionResult.FAIL;
        if (old.is(Blocks.ROOTED_DIRT)) Block.popResource(level, pos.above(), new ItemStack(Items.HANGING_ROOTS));
        if (!creative(player)) EuItemManager.use(stack, rules.cost, player);
        level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1, 1);
        return InteractionResult.CONSUME;
    }
    @Override public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (rules != ElectricToolRules.CHAINSAW || shearDisabled(stack) || !(target instanceof Shearable sheep) || !sheep.readyForShearing()) return InteractionResult.PASS;
        if (player.level().isClientSide()) return InteractionResult.SUCCESS;
        EuItemManager.chargeFromArmor(stack, player);
        if (!creative(player) && !powered(stack)) return InteractionResult.FAIL;
        sheep.shear(SoundSource.PLAYERS);
        if (!creative(player)) EuItemManager.use(stack, rules.cost, player);
        return InteractionResult.CONSUME;
    }
    private static boolean shearableBlock(BlockState state) {
        return state.is(BlockTags.LEAVES) || state.is(Blocks.COBWEB) || state.is(Blocks.VINE)
                || state.is(Blocks.GLOW_LICHEN) || state.is(Blocks.DEAD_BUSH) || state.is(Blocks.FERN)
                || state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS) || state.is(Blocks.TRIPWIRE)
                || state.is(Blocks.HANGING_ROOTS) || state.is(Blocks.NETHER_SPROUTS)
                || state.getBlock() instanceof com.shipovskijkorp.ic2modernadapter.content.block.RubberLeavesBlock
                || ToolVersionSupport.shortGrass(state);
    }
    /** Called only from a loader's server-side block break callback. Vanilla shears loot rules are reused. */
    public static boolean shearBlock(ServerPlayer player, BlockPos pos, BlockState state) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ElectricMiningToolItem tool) || tool.rules != ElectricToolRules.CHAINSAW
                || shearDisabled(stack) || creative(player) || player.isSpectator() || !player.mayBuild()
                || !shearableBlock(state)) return false;
        ServerLevel level = player.serverLevel();
        if (!level.mayInteract(player, pos) || level.getBlockEntity(pos) != null || !level.getBlockState(pos).equals(state)) return false;
        EuItemManager.chargeFromArmor(stack, player);
        if (!tool.powered(stack)) return false;
        var drops = Block.getDrops(state, level, pos, null, player, new ItemStack(Items.SHEARS));
        if (!level.removeBlock(pos, false)) return false;
        for (ItemStack drop : drops) Block.popResource(level, pos, drop);
        player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
        player.causeFoodExhaustion(0.005F);
        EuItemManager.use(stack, tool.rules.cost, player);
        ToolVersionSupport.updateAttributes(stack, tool.rules);
        return true;
    }
    public boolean cycleMode(ItemStack stack, Player player) {
        String key;
        if (rules == ElectricToolRules.CHAINSAW) key = "disableShear";
        else if (rules == ElectricToolRules.IRIDIUM_DRILL) key = "silkTouch";
        else return false;
        boolean value = !ToolData.flag(stack, key);
        ToolData.flag(stack, key, value);
        if (rules == ElectricToolRules.IRIDIUM_DRILL) ToolVersionSupport.ensureIridiumEnchantments(stack, player.level(), value);
        player.displayClientMessage(Component.translatable("ic2.tooltip.mode", Component.translatable(value
                ? rules == ElectricToolRules.CHAINSAW ? "ic2.tooltip.mode.noShear" : "ic2.tooltip.mode.silkTouch"
                : "ic2.tooltip.mode.normal")), true);
        return true;
    }
    public boolean isShearingEnabled(ItemStack stack) { return rules == ElectricToolRules.CHAINSAW && !shearDisabled(stack); }
}
