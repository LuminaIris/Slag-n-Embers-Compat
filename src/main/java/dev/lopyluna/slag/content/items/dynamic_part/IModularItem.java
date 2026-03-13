package dev.lopyluna.slag.content.items.dynamic_part;

import com.mojang.datafixers.util.Pair;
import dev.lopyluna.slag.content.items.modular.DataDynamicParts;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import dev.lopyluna.slag.register.AllLangs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleBiFunction;

@SuppressWarnings("unused")
public interface IModularItem {
    default ModularType getModularTypeFromParts(@Nullable DataDynamicParts parts) { //WORKS NOW
        if (parts == null || parts.isEmpty()) return null;
        var tags = parts.getAllDynamicPartSegments();
        var stacks = parts.getAllNonDynamicParts();
        var sizeTags = tags.size();
        var sizeStacks = stacks.size();

        for (var modular : AllDynamicTypes.getAllModulars()) {
            if (!containsExactlyAllStacks(stacks, modular.finalSegmentStacks)) continue;
            if (!containsExactlyAllTags(tags, modular.segments)) continue;
            return modular;
        }
        return null;
    }

    default boolean containsExactlyAllStacks(List<ItemStack> stacks, List<ItemStack> otherStacks) {
        if (stacks.size() != otherStacks.size()) return false;
        for (var stack : stacks) {
            var found = false;
            for (var otherStack : otherStacks) if (equalStack(stack, otherStack, false)) {
                found = true;
                break;
            }
            if (!found) return false;
        }
        return true;
    }
    default boolean containsExactlyAllTags(List<TagKey<Item>> tags, List<TagKey<Item>> otherTags) {
        if (tags.size() != otherTags.size()) return false;
        for (var tag : tags) {
            var found = false;
            for (var otherTag : otherTags) if (equalTags(tag, otherTag)) {
                found = true;
                break;
            }
            if (!found) return false;
        }
        return true;
    }
    default boolean equalStack(ItemStack aStack, ItemStack bStack, boolean built) {
        var a = aStack.copy();
        var b = bStack.copy();
        if (!built) {
            a.remove(AllDataComponents.BUILT);
            b.remove(AllDataComponents.BUILT);
        }
        return ItemStack.isSameItemSameComponents(a, b) && a.getCount() == b.getCount();
    }
    default boolean equalTags(TagKey<Item> aTag, TagKey<Item> bTag) {
        return aTag.location().equals(bTag.location()) && aTag.registry().location().equals(bTag.registry().location());
    }

    default ModularType getModularTypeFromStack(ItemStack stack) {
        var parts = getParts(stack);
        if (parts == null) return null;
        return getModularTypeFromParts(parts);
    }

    default List<Pair<ItemStack, IDynamicPart>> getDynamicParts(ItemStack pStack) {
        var parts = new ArrayList<Pair<ItemStack, IDynamicPart>>();
        var dataParts = getParts(pStack);
        if (dataParts == null || dataParts.isEmpty()) return parts;
        var copy = dataParts.itemsCopy();
        for (var stack : copy) if (stack.getItem() instanceof IDynamicPart part) parts.add(Pair.of(stack, part));
        return parts;
    }
    default List<MaterialType> getMaterialTypes(DataDynamicParts parts) {
        var types = new ArrayList<MaterialType>();
        for (var stack : parts.itemsCopy()) if (stack.getItem() instanceof IDynamicPart part) {
            var type = part.getMaterialType(stack).orElse(null);
            if (type == null) continue;
            if (types.contains(type)) continue;
            types.add(type);
        }
        return types;
    }
    default List<ItemStack> getNonDynamicParts(ItemStack pStack) {
        var parts = new ArrayList<ItemStack>();
        var dataParts = getParts(pStack);
        if (dataParts == null || dataParts.isEmpty()) return parts;
        for (var stack : dataParts.itemsCopy()) if (!(stack.getItem() instanceof IDynamicPart)) parts.add(stack);
        return parts;
    }
    default DataDynamicParts getParts(ItemStack pStack) {
        return pStack.get(AllDataComponents.DYNAMIC_PARTS);
    }
    default void setParts(ItemStack pStack, List<ItemStack> pStacks) {
        pStack.set(AllDataComponents.DYNAMIC_PARTS, new DataDynamicParts(pStacks));
    }
    default float averageMod(ItemStack stack, ToDoubleBiFunction<ItemStack, IDynamicPart> getter) {
        var parts = getDynamicParts(stack);
        if (parts.isEmpty()) return 0f;
        var newParts = new ArrayList<Double>();
        for (var p : parts) {
            var sumAdd = getter.applyAsDouble(p.getFirst(), p.getSecond());
            if (sumAdd == 987654321f || sumAdd == -987654321f) continue;
            newParts.add(sumAdd);
        }
        if (newParts.isEmpty()) return 0f;
        int size = newParts.size();
        double sum = 0;
        for (var p : newParts) sum += p;
        return ((int) (((float)(sum / size)) * 100f) / 100f);
    }
    default float averageModSize(ItemStack stack, ToDoubleBiFunction<ItemStack, IDynamicPart> getter) {
        var parts = getDynamicParts(stack);
        if (parts.isEmpty()) return 0f;
        var newParts = new ArrayList<Double>();
        for (var p : parts) {
            var sumAdd = getter.applyAsDouble(p.getFirst(), p.getSecond());
            if (sumAdd == 987654321f || sumAdd == -987654321f) continue;
            newParts.add(sumAdd);
        }
        if (newParts.isEmpty()) return 0f;
        int size = newParts.size();
        double sum = 0;
        for (var p : newParts) sum += p * ((float) size * 0.25f + 0.75f);
        return ((int) (((float)(sum / size)) * 100f) / 100f);
    }


    default float getSpeed(ItemStack stack) {
        return (averageMod(stack, (s, p) -> p.getSpeed(s)) + averageMod(stack, (s, p) -> p.getSpeedPart(s))) * averageMod(stack, (s, p) -> 1f);
    }
    default float getAttackSpeed(ItemStack stack) {
        return averageMod(stack, (s, p) -> p.getSpeedMod(s));
    }
    default float getDura(ItemStack stack) {
        return (averageModSize(stack, (s, p) -> p.getDura(s)) + averageModSize(stack, (s, p) -> p.getDuraPart(s))) * averageModSize(stack, (s, p) -> p.getDuraMod(s));
    }
    default float getTier(ItemStack stack) {
        return (averageMod(stack, (s, p) -> p.getTier(s)) + averageMod(stack, (s, p) -> p.getTierPart(s))) * averageMod(stack, (s, p) -> p.getTierMod(s));
    }
    default float getHammerTier(DataDynamicParts parts, ItemStack stack) {
        return getTier(stack);
    }
    default float getSharp(ItemStack stack) {
        return (averageMod(stack, (s, p) -> p.getSharp(s)) + averageMod(stack, (s, p) -> p.getSharpPart(s))) * averageMod(stack, (s, p) -> p.getSharpMod(s));
    }
    default float getKbRes(ItemStack stack) {
        return (averageMod(stack, (s, p) -> p.getKbRes(s)) + averageMod(stack, (s, p) -> p.getKbResPart(s))) * averageMod(stack, (s, p) -> p.getKbResMod(s));
    }
    default float getDefense(ItemStack stack) {
        return (averageMod(stack, (s, p) -> p.getDefense(s)) + averageMod(stack, (s, p) -> p.getDefensePart(s))) * averageMod(stack, (s, p) -> p.getDefenseMod(s));
    }
    default float getTough(ItemStack stack) {
        return (averageMod(stack, (s, p) -> p.getTough(s)) + averageMod(stack, (s, p) -> p.getToughPart(s))) * averageMod(stack, (s, p) -> p.getToughMod(s));
    }
    default float getEnch(ItemStack stack) {
        return (averageMod(stack, (s, p) -> p.getEnch(s)) + averageMod(stack, (s, p) -> p.getEnchPart(s))) * averageMod(stack, (s, p) -> p.getEnchMod(s));
    }

    default void appendHoverModularArmor(IModularItem item, ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (context.level() instanceof net.minecraft.client.multiplayer.ClientLevel level) {
            var parts = item.getParts(stack);
            if (parts != null && !parts.isEmpty()) {
                AllLangs.modularArmorStats(tooltip, parts, stack, item);
                AllLangs.modularParts(tooltip, parts.itemsCopy());
            } else tooltip.add(AllLangs.tr("modular_tool_waiting").withStyle(ChatFormatting.GRAY));
        }
    }

    default void appendHoverTextModularTool(IModularItem item, ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (context.level() instanceof net.minecraft.client.multiplayer.ClientLevel level) {
            var parts = item.getParts(stack);
            if (parts != null && !parts.isEmpty()) {
                var toolParts = parts.itemsCopy();
                if (toolParts.isEmpty()) {
                    tooltip.add(AllLangs.tr("modular_tool_waiting").withStyle(ChatFormatting.GRAY));
                    return;
                }

                AllLangs.modularToolStats(tooltip, parts, stack, item);
                AllLangs.modularParts(tooltip, toolParts);
            } else tooltip.add(AllLangs.tr("modular_tool_waiting").withStyle(ChatFormatting.GRAY));
        }
    }

    default void appendHoverTextModularBlueprint(IModularItem item, ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (context.level() instanceof net.minecraft.client.multiplayer.ClientLevel level) {
            var parts = item.getParts(stack);
            if (parts == null || parts.isEmpty()) return;

            var player = net.minecraft.client.Minecraft.getInstance().player;
            var tier = item.getTier(stack);

            if (3.5f >= tier) tooltip.add(AllLangs.tr("modular_tool_crafting_hammer_weak").withStyle(ChatFormatting.RED));
            else tooltip.add(AllLangs.trArgs("modular_tool_crafting_hammer", String.valueOf(tier)).withStyle(ChatFormatting.RED));

            //var f = true;
            //if (player != null) {
            //    var lookState = level.getBlockState(Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE).getBlockPos());
            //    var size = parts.size();
            //    if (size > 4) {
            //        if (!lookState.is(BlockTags.ANVIL)) {
            //            tooltip.add(AllLangs.tr("modular_tool_anvil").withStyle(ChatFormatting.RED));
            //            f = false;
            //        }
            //    }
            //}

            //if (f) {
            //    if (3.5f >= tier) tooltip.add(AllLangs.tr("modular_tool_crafting_hammer_weak").withStyle(ChatFormatting.RED));
            //    else tooltip.add(AllLangs.trArgs("modular_tool_crafting_hammer", String.valueOf(tier)).withStyle(ChatFormatting.RED));
            //}
        }
    }

}
