package dev.lopyluna.slag.content.items.modular;

import dev.lopyluna.slag.content.items.dynamic_part.DynamicPartItem;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
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
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ModularToolsItem extends ModularItem {
    public ModularToolsItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (!hasModularType(stack) || !isTool(stack)) return super.getDefaultAttributeModifiers(stack);
        return super.getDefaultAttributeModifiers(stack)
                .withModifierAdded(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, getSharp(stack), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .withModifierAdded(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -getAttackSpeed(stack), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return hasModularType(stack) && isTool(stack);
    }

    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (hasModularType(stack) && isTool(stack)) stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack print, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (hasModularType(other) && (isTool(other) || isArmor(other))) return false;
        return super.overrideOtherStackedOnMe(print, other, slot, action, player, access);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return hasModularType(stack) && (isTool(stack) || isArmor(stack));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        var modularType = getModularType(stack);
        var has = modularType != null && isTool(stack);
        if (!level.isClientSide && has && state.getDestroySpeed(level, pos) != 0.0F) stack.hurtAndBreak(isMiningTool(modularType) ? 1 : 2, miningEntity, EquipmentSlot.MAINHAND);
        return has;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        if (!hasModularType(stack) || !(isTool(stack) || isArmor(stack))) return super.getMaxDamage(stack);
        return Math.round(getDura(stack));
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        if (!hasModularType(stack) || !(isTool(stack) || isArmor(stack))) return super.getEnchantmentValue(stack);
        return Math.round(getEnch(stack));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (!hasModularType(stack) || !isTool(stack)) return super.getDestroySpeed(stack, state);
        return isCorrectToolForDrops(stack, state) ? getSpeed(stack) * (state.is(Blocks.COBWEB) ? 2f : 1f) : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        if (!hasModularType(stack) || !(isTool(stack) || isArmor(stack))) return super.isValidRepairItem(stack, repairCandidate);
        var parts = getParts(stack);
        if (parts != null) for (var part : parts.getAllDynamicParts()) {
            if (!(part.getItem() instanceof DynamicPartItem item)) continue;
            var material = item.getMaterialType(part);
            if (material.isEmpty()) continue;
            if (material.get().repairMaterials.get().test(repairCandidate)) return true;
        }
        return super.isValidRepairItem(stack, repairCandidate);
    }

    public boolean isMiningTool(ModularType modularType) {
        if (modularType.actions.contains("pickaxe")) return true;
        if (modularType.actions.contains("axe")) return true;
        if (modularType.actions.contains("shovel")) return true;
        return modularType.actions.contains("hoe");
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (!hasModularType(stack) || !isTool(stack)) return super.isCorrectToolForDrops(stack, state);
        var modularType = getModularType(stack);
        if (modularType == null) return super.isCorrectToolForDrops(stack, state);
        boolean flag = false;

        for (var action : modularType.actions) {
            if (flag) break;
            switch (action) {
                case "pickaxe", "pickaxe_mineable" -> flag = state.is(BlockTags.MINEABLE_WITH_PICKAXE);
                case "axe", "axe_mineable"         -> flag = state.is(BlockTags.MINEABLE_WITH_AXE);
                case "shovel", "shovel_mineable"   -> flag = state.is(BlockTags.MINEABLE_WITH_SHOVEL);
                case "hoe", "hoe_mineable"         -> flag = state.is(BlockTags.MINEABLE_WITH_HOE);
                case "sword", "sword_mineable"     -> flag = state.is(Blocks.COBWEB) || state.is(BlockTags.SWORD_EFFICIENT);
            }
            if (!flag && action.contains("_mineable")) flag = state.is(AllTags.blockC(action.replace("_mineable", "")));
        }
        var i = 0f;
        if (state.is(BlockTags.INCORRECT_FOR_WOODEN_TOOL)) i = 1f;
        if (state.is(BlockTags.INCORRECT_FOR_GOLD_TOOL)) i = 2f;
        if (state.is(BlockTags.INCORRECT_FOR_STONE_TOOL)) i = 3f;
        if (state.is(BlockTags.INCORRECT_FOR_IRON_TOOL)) i = 4f;
        if (state.is(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)) i = 5f;
        if (state.is(BlockTags.INCORRECT_FOR_NETHERITE_TOOL)) i = 6f;
        return getTier(stack) > i + 0.5f && flag;
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        var modularType = getModularType(stack);
        if (modularType != null) for (var action : modularType.actions) {
            var onAction = ModularType.doAction(action,"canDisableShield", stack, shield, entity, attacker);
            if (onAction == null) continue;
            if (!(onAction instanceof Boolean b) || !b) continue;
            return true;
        }
        return super.canDisableShield(stack, shield, entity, attacker);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        var modularType = getModularType(stack);
        if (modularType != null) for (var action : modularType.actions) {
            var onAction = ModularType.doAction(action,"use", stack, level, player, usedHand);
            if (onAction == null) continue;
            if (!(onAction instanceof InteractionResultHolder<?> result) || !result.getResult().consumesAction()) continue;
            return (InteractionResultHolder<ItemStack>) result;
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        var stack = context.getItemInHand();
        var modularType = getModularType(stack);
        if (modularType != null) for (var action : modularType.actions) {
            var onAction = ModularType.doAction(action,"useOn", context);
            if (onAction == null) continue;
            if (!(onAction instanceof InteractionResult result) || !result.consumesAction()) continue;
            return result;
        }
        return super.useOn(context);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        var modularType = getModularType(stack);
        if (modularType != null) for (var action : modularType.actions) {
            var onAction = ModularType.doAction(action,"canPerformAction", stack, itemAbility);
            if (onAction == null) continue;
            if (!(onAction instanceof Boolean b) || !b) continue;
            return true;
        }
        return super.canPerformAction(stack, itemAbility);
    }


    @Override
    public boolean isEnchantable(ItemStack stack) {
        if (!hasModularType(stack)) return super.isEnchantable(stack);
        return true;
    }
}
