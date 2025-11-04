package dev.lopyluna.slag.content.items.dynamic_part;

import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.PartType;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Math;

import java.util.Optional;

public interface IDynamicPart {
    Optional<MaterialType> getMaterialType(ItemStack stack);
    Optional<PartType> getPartType(ItemStack stack);

    default TagKey<Item> getPartSegment(ItemStack stack) {
        var type = getPartType(stack).orElse(null);
        if (type == null) return AllTags.item("empty");
        return type.segmentPart;
    }

    default float getSpeedMod(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 1f; return Math.max(type.speedMod, 0f); }
    default float getDuraMod(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 1f; return Math.max(type.duraMod, 0f); }
    default float getTierMod(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 1f; return Math.max(type.tierMod, 0f); }
    default float getSharpMod(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 1f; return Math.max(type.sharpMod, 0f); }
    default float getKbResMod(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 1f; return Math.max(type.kbResMod, 0f); }
    default float getDefenseMod(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 1f; return Math.max(type.defenseMod, 0f); }
    default float getToughMod(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 1f; return Math.max(type.toughMod, 0f); }
    default float getEnchMod(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 1f; return Math.max(type.enchMod, 0f); }

    default float getSpeedPart(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.speed, 0f); }
    default float getDuraPart(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.dura, 0f); }
    default float getTierPart(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.tier, 0f); }
    default float getSharpPart(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.sharp, 0f); }
    default float getKbResPart(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.kbRes, 0f); }
    default float getDefensePart(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.defense, 0f); }
    default float getToughPart(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.tough, 0f); }
    default float getEnchPart(ItemStack stack) { var type = getPartType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.ench, 0f); }

    default float getSpeed(ItemStack stack) { var type = getMaterialType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.speed, 0f); }
    default float getDura(ItemStack stack) { var type = getMaterialType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.dura, 0f); }
    default float getTier(ItemStack stack) { var type = getMaterialType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.tier, 0f); }
    default float getSharp(ItemStack stack) { var type = getMaterialType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.sharp, 0f); }
    default float getKbRes(ItemStack stack) { var type = getMaterialType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.kbRes, 0f); }
    default float getDefense(ItemStack stack) { var type = getMaterialType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.defense, 0f); }
    default float getTough(ItemStack stack) { var type = getMaterialType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.tough, 0f); }
    default float getEnch(ItemStack stack) { var type = getMaterialType(stack).orElse(null); if (type == null) return 0f; return Math.max(type.ench, 0f); }

    default boolean isFireImmune(ItemStack stack) { var type = getMaterialType(stack).orElse(null); if (type == null) return false; return type.fireProof; }
}
