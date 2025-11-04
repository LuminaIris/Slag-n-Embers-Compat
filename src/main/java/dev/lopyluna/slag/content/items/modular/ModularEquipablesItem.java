package dev.lopyluna.slag.content.items.modular;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ModularEquipablesItem extends ModularToolsItem {
    ResourceLocation HELMET_LOC = ResourceLocation.withDefaultNamespace("armor.helmet");
    ResourceLocation CHESTPLATE_LOC = ResourceLocation.withDefaultNamespace("armor.chestplate");
    ResourceLocation LEGGINGS_LOC = ResourceLocation.withDefaultNamespace("armor.leggings");
    ResourceLocation BOOTS_LOC = ResourceLocation.withDefaultNamespace("armor.boots");
    public ModularEquipablesItem(Properties properties) {
        super(properties);
        DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        if (!hasModularType(stack) || !isArmor(stack)) return super.getDefaultAttributeModifiers(stack);
        var modularType = getModularType(stack);
        if (modularType == null) return super.getDefaultAttributeModifiers(stack);
        var act = modularType.actions;
        if (act.isEmpty()) return super.getDefaultAttributeModifiers(stack);
        var loc = act.contains("helmet") ? HELMET_LOC : act.contains("chestplate") ? CHESTPLATE_LOC : act.contains("leggings") ? LEGGINGS_LOC : act.contains("boots") ? BOOTS_LOC : null;
        var slot = act.contains("helmet") ? EquipmentSlotGroup.HEAD : act.contains("chestplate") ? EquipmentSlotGroup.CHEST : act.contains("leggings") ? EquipmentSlotGroup.LEGS : act.contains("boots") ? EquipmentSlotGroup.FEET : null;
        if (loc == null || slot == null) return super.getDefaultAttributeModifiers(stack);

        return super.getDefaultAttributeModifiers(stack)
                .withModifierAdded(Attributes.ARMOR, new AttributeModifier(loc, Math.round(getDefense(stack)), AttributeModifier.Operation.ADD_VALUE), slot)
                .withModifierAdded(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(loc, Math.round(getTough(stack)), AttributeModifier.Operation.ADD_VALUE), slot)
                .withModifierAdded(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(loc, getKbRes(stack), AttributeModifier.Operation.ADD_VALUE), slot);
    }

    public Holder<SoundEvent> getEquipSound(ItemStack stack) {
        var parts = getDynamicParts(stack);
        if (parts.isEmpty()) return SoundEvents.ARMOR_EQUIP_DIAMOND;
        var pair = parts.getFirst();
        var part = pair.getSecond();
        var material = part.getMaterialType(pair.getFirst());
        if (material.isEmpty()) return SoundEvents.ARMOR_EQUIP_DIAMOND;
        var mat = material.get();
        return switch (mat.texture) {
            case "soft" -> SoundEvents.ARMOR_EQUIP_WOLF;
            case "leather" -> SoundEvents.ARMOR_EQUIP_LEATHER;
            case "base" -> SoundEvents.ARMOR_EQUIP_IRON;
            case "shiny" -> SoundEvents.ARMOR_EQUIP_DIAMOND;
            case "metal" -> SoundEvents.ARMOR_EQUIP_NETHERITE;
            default -> SoundEvents.ARMOR_EQUIP_GENERIC;
        };
    }

    public Holder<ArmorMaterial> getPotentialArmorMaterials() {
        return ArmorMaterials.LEATHER;
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(@NotNull ItemStack stack) {
        if (!hasModularType(stack)) return super.getEquipmentSlot(stack);

        var modularType = getModularType(stack);
        if (modularType != null) for (var action : modularType.actions) {
            var onAction = ModularType.doAction(action,"getEquipmentSlot", stack);
            if (onAction == null) continue;
            if (!(onAction instanceof EquipmentSlot result)) continue;
            return result;
        }
        return super.getEquipmentSlot(stack);
    }

    public List<ResourceLocation> getArmorTextures(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        var list = new ArrayList<ResourceLocation>();
        var num = innerModel ? 2 : 1;

        var parts = getDynamicParts(stack);
        if (parts.isEmpty()) return list;
        for (var pair : parts) {
            var partStack = pair.getFirst();
            var partDynamic = pair.getSecond();
            var material = partDynamic.getMaterialType(partStack);
            if (material.isEmpty()) return list;
            var part = partDynamic.getPartType(partStack);
            if (part.isEmpty()) return list;
            var p = part.get();
            var m = material.get();
            var pPath = p.id.getPath();
            var bool = pPath.contains("helmet") || pPath.contains("chestplate") || pPath.contains("leggings") || pPath.contains("boots");
            var prefix = bool ? "" : pPath + "/";
            var path = "armors/" + prefix + m.texture + "_layer_" + num + "_" + m.id.getPath();
            if (bool) list.addFirst(SlagEmbers.loc(m.id.getNamespace(), path));
            else list.add(SlagEmbers.loc(m.id.getNamespace(), path));
        }
        if (list.isEmpty()) list.add(SlagEmbers.loc("textures/armors/metal_layer_" + num + ".png"));
        return list;
    }

    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
        public ModularType getModularType(ItemStack stack) {
            var loc = stack.get(AllDataComponents.MODULAR_TYPE);
            if (loc == null) return null;
            return AllDynamicTypes.getModular(loc).orElse(null);
        }
        public boolean hasModularType(ItemStack stack) {
            return stack.has(AllDataComponents.MODULAR_TYPE);
        }

        @Override
        protected @NotNull ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
            if (!hasModularType(stack)) return super.execute(source, stack);
            var modularType = getModularType(stack);
            if (modularType == null) return super.execute(source, stack);
            var act = modularType.actions;
            if (act.isEmpty()) return super.execute(source, stack);
            if (!(act.contains("dispense") || act.contains("helmet") || act.contains("chestplate") || act.contains("leggings") || act.contains("boots"))) return super.execute(source, stack);
            return ArmorItem.dispenseArmor(source, stack) ? stack : super.execute(source, stack);
        }
    };
}
