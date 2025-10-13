package dev.lopyluna.slag.content.items.modular_tool;

import com.mojang.datafixers.util.Pair;
import dev.lopyluna.slag.mixin.AxeItemAccessor;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.minecraft.world.item.HoeItem.changeIntoState;

@ParametersAreNonnullByDefault
public class BakedModularToolItem extends ModularToolItem {
    public BakedModularToolItem(Properties properties) {
        super(properties.durability(924));
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        var flag = false;
        for (var part : getToolParts(stack)) if (part.getMaterialType().fireProof) {
            flag = true;
            break;
        }
        if (flag) stack.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);

        return super.getDefaultAttributeModifiers(stack)
                .withModifierAdded(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, averageMod(stack, IToolPart::getSharp) * averageMod(stack, IToolPart::getSharpMod), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .withModifierAdded(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -averageMod(stack, IToolPart::getSpeedMod), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        return false;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return Math.round(averageMod(stack, IToolPart::getEnch));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isCorrectToolForDrops(stack, state) ? averageMod(stack, IToolPart::getSpeed) : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        var pParts = getParts(stack);
        if (pParts == null) return false;
        var parts = pParts.itemsCopy();
        for (var partStack : parts) if (partStack.getItem() instanceof IToolPart part && part.getMaterialType().repairMaterials.get().test(repairCandidate)) return true;
        return false;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        boolean flag = false;
        for (var part : getToolParts(stack)) {
            if (flag) break;
            switch (part.getPartSegment().getPath()) {
                case "pickaxe_head" -> flag = state.is(BlockTags.MINEABLE_WITH_PICKAXE);
                case "axe_head"     -> flag = state.is(BlockTags.MINEABLE_WITH_AXE);
                case "shovel_head"  -> flag = state.is(BlockTags.MINEABLE_WITH_SHOVEL);
                case "hoe_head"     -> flag = state.is(BlockTags.MINEABLE_WITH_HOE);
                case "sword_blade"  -> flag = state.is(BlockTags.SWORD_EFFICIENT);
            }
        }
        var i = 0f;
        if (state.is(BlockTags.INCORRECT_FOR_WOODEN_TOOL)) i = 1f;
        if (state.is(BlockTags.INCORRECT_FOR_GOLD_TOOL)) i = 2f;
        if (state.is(BlockTags.INCORRECT_FOR_STONE_TOOL)) i = 3f;
        if (state.is(BlockTags.INCORRECT_FOR_IRON_TOOL)) i = 4f;
        if (state.is(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)) i = 5f;
        if (state.is(BlockTags.INCORRECT_FOR_NETHERITE_TOOL)) i = 6f;
        return averageMod(stack, IToolPart::getTough) > i + 0.5f && flag;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F)
            stack.hurtAndBreak(2, miningEntity, EquipmentSlot.MAINHAND);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return (int) (averageMod(stack, IToolPart::getDura) * averageMod(stack, IToolPart::getDuraMod));
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        for (var part : getToolParts(stack)) if (part.getPartSegment().getPath().equals("axe_head")) return true;
        return super.canDisableShield(stack, shield, entity, attacker);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        List<Boolean> actions = new ArrayList<>();
        for (var part : getToolParts(stack)) {
            var path = part.getPartSegment().getPath();
            if (path.equals("pickaxe_head")) actions.add(ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(itemAbility));
            if (path.equals("axe_head")) actions.add(ItemAbilities.DEFAULT_AXE_ACTIONS.contains(itemAbility));
            if (path.equals("shovel_head")) actions.add(ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(itemAbility));
            if (path.equals("hoe_head")) actions.add(ItemAbilities.DEFAULT_HOE_ACTIONS.contains(itemAbility));
            if (path.equals("sword_blade")) actions.add(ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(itemAbility));
        }
        return actions.contains(true);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        var stack = context.getItemInHand();
        List<String> tool = new ArrayList<>();
        for (var part : getToolParts(stack)) {
            var path = part.getPartSegment().getPath();
            if (path.equals("pickaxe_head") && !tool.contains("pickaxe")) tool.add("pickaxe");
            if (path.equals("axe_head") && !tool.contains("axe")) tool.add("axe");
            if (path.equals("shovel_head") && !tool.contains("shovel")) tool.add("shovel");
            if (path.equals("hoe_head") && !tool.contains("hoe")) tool.add("hoe");
            if (path.equals("sword_blade") && !tool.contains("sword")) tool.add("sword");
        }
        if (tool.contains("shovel") && tool.contains("hoe")) {
            if (context.getHand().equals(InteractionHand.OFF_HAND)) {
                var shovel = shovelUseOn(context);
                if (shovel.consumesAction()) return shovel;
            } else {
                var hoe = hoeUseOn(context);
                if (hoe.consumesAction()) return hoe;
            }
            if (tool.contains("axe")) {
                var axe = axeUseOn(context);
                if (axe.consumesAction()) return axe;
            }
        } else {
            if (tool.contains("shovel")) {
                var shovel = shovelUseOn(context);
                if (shovel.consumesAction()) return shovel;
            }
            if (tool.contains("hoe")) {
                var hoe = hoeUseOn(context);
                if (hoe.consumesAction()) return hoe;
            }
            if (tool.contains("axe")) {
                var axe = axeUseOn(context);
                if (axe.consumesAction()) return axe;
            }
        }
        return super.useOn(context);
    }

    public InteractionResult hoeUseOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState toolModifiedState = level.getBlockState(blockpos).getToolModifiedState(context, net.neoforged.neoforge.common.ItemAbilities.HOE_TILL, false);
        Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = toolModifiedState == null ? null : Pair.of(ctx -> true, changeIntoState(toolModifiedState));
        if (pair == null) return InteractionResult.PASS;
        else { Predicate<UseOnContext> predicate = pair.getFirst();
            Consumer<UseOnContext> consumer = pair.getSecond();
            if (predicate.test(context)) {
                Player player = context.getPlayer();
                level.playSound(player, blockpos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!level.isClientSide) {
                    consumer.accept(context);
                    if (player != null) context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else return InteractionResult.PASS;
        }
    }

    public InteractionResult shovelUseOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (context.getClickedFace() == Direction.DOWN) return InteractionResult.PASS;
        else { Player player = context.getPlayer();
            BlockState flatten = blockstate.getToolModifiedState(context, net.neoforged.neoforge.common.ItemAbilities.SHOVEL_FLATTEN, false);
            BlockState state;
            if (flatten != null && level.getBlockState(blockpos.above()).isAir()) {
                level.playSound(player, blockpos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                state = flatten;
            } else if ((state = blockstate.getToolModifiedState(context, net.neoforged.neoforge.common.ItemAbilities.SHOVEL_DOUSE, false)) != null) {
                if (!level.isClientSide()) level.levelEvent(null, 1009, blockpos, 0);
            } if (state != null) {
                if (!level.isClientSide) {
                    level.setBlock(blockpos, state, 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(player, state));
                    if (player != null) context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else return InteractionResult.PASS;
        }
    }

    public InteractionResult axeUseOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Player player = context.getPlayer();
        if (AxeItemAccessor.playerHasShieldUseIntent(context)) return InteractionResult.PASS;
        else {
            Optional<BlockState> optional = evaluateNewBlockState(level, blockpos, player, level.getBlockState(blockpos), context);
            if (optional.isEmpty()) return InteractionResult.PASS;
            else {
                ItemStack itemstack = context.getItemInHand();
                if (player instanceof ServerPlayer) CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockpos, itemstack);

                level.setBlock(blockpos, optional.get(), 11);
                level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(player, optional.get()));
                if (player != null) itemstack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
    }

    private Optional<BlockState> evaluateNewBlockState(Level level, BlockPos pos, @Nullable Player player, BlockState state, UseOnContext p_40529_) {
        Optional<BlockState> optional = Optional.ofNullable(state.getToolModifiedState(p_40529_, net.neoforged.neoforge.common.ItemAbilities.AXE_STRIP, false));
        if (optional.isPresent()) {
            level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            return optional;
        } else {
            Optional<BlockState> optional1 = Optional.ofNullable(state.getToolModifiedState(p_40529_, net.neoforged.neoforge.common.ItemAbilities.AXE_SCRAPE, false));
            if (optional1.isPresent()) {
                level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.levelEvent(player, 3005, pos, 0);
                return optional1;
            } else {
                Optional<BlockState> optional2 = Optional.ofNullable(state.getToolModifiedState(p_40529_, net.neoforged.neoforge.common.ItemAbilities.AXE_WAX_OFF, false));
                if (optional2.isPresent()) {
                    level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.levelEvent(player, 3004, pos, 0);
                    return optional2;
                } else return Optional.empty();
            }
        }
    }
}
