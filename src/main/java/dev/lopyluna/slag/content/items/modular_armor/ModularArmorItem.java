package dev.lopyluna.slag.content.items.modular_armor;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.modular_tool.DataToolParts;
import dev.lopyluna.slag.content.items.modular_tool.IToolPart;
import dev.lopyluna.slag.register.AllDataComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;

public class ModularArmorItem extends Item implements Equipable {
    public ModularArmorItem(Properties properties) {
        super(properties.stacksTo(1).component(AllDataComponents.TOOL_PARTS, DataToolParts.EMPTY));
    }
    public List<IToolPart> getToolParts(ItemStack pStack) {
        List<IToolPart> toolParts = new ArrayList<>();
        var pParts = getParts(pStack);
        if (pParts == null) return toolParts;
        var parts = pParts.itemsCopy();
        for (var stack : parts) if (stack.getItem() instanceof IToolPart part) toolParts.add(part);
        return toolParts;
    }
    public DataToolParts getParts(ItemStack pStack) {
        return pStack.get(AllDataComponents.TOOL_PARTS);
    }
    public void setParts(ItemStack pStack, List<ItemStack> pStacks) {
        pStack.set(AllDataComponents.TOOL_PARTS, new DataToolParts(pStacks));
    }


    public ArmorItem.Type getArmorType(ItemStack stack) {
        return stack.getOrDefault(AllDataComponents.ARMOR_TYPE, ArmorItem.Type.BODY);
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(@NotNull ItemStack stack) {
        return getArmorType(stack).getSlot();
    }

    public int getDefense(ItemStack stack) {
        return averageMod(stack, IToolPart::getTough);
    }
    public int getToughness(ItemStack stack) {
        return stack.getOrDefault(AllDataComponents.ARMOR_TOUGHNESS, 0);
    }
    public int getKnockbackResistance(ItemStack stack) {
        return stack.getOrDefault(AllDataComponents.ARMOR_KNOCKBACK_RESISTANCE, 0);
    }



    public static final IntFunction<ArmorItem.Type> BY_ID = ByIdMap.continuous(ModularArmorItem::typeID, ArmorItem.Type.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, ArmorItem.Type> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ModularArmorItem::typeID);

    public static int typeID(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 0;
            case CHESTPLATE -> 1;
            case LEGGINGS -> 2;
            case BOOTS -> 3;
            case BODY -> 4;
        };
    }

    @Override
    public @NotNull EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.BODY;
    }

    @Override
    public @NotNull String getDescriptionId(ItemStack stack) {
        var mixture = getArmorMixture(stack);
        var material = "";
        var pParts = getParts(stack);
        if (pParts == null) return super.getDescriptionId(stack);
        var parts = pParts.itemsCopy();
        for (var partStack : parts) if (partStack.getItem() instanceof IToolPart part) {
            material = part.getMaterialType().id;
            break;
        }
        return mixture.isEmpty() || material.isEmpty() ? super.getDescriptionId(stack) : Util.makeDescriptionId("item", SlagEmbers.loc(BuiltInRegistries.ITEM.getKey(this).getNamespace(), material + "_" + mixture));
    }
    public String getArmorMixture(ItemStack pStack) {
        if (pStack.isEmpty()) return "";
        var parts = getParts(pStack);
        if (parts == null) return "";
        if (parts.hasAllPartSegments("helmet_part", "armor_trim")) return "helmet";
        if (parts.hasAllPartSegments("chestplate_part", "armor_trim")) return "chestplate";
        if (parts.hasAllPartSegments("leggings_part", "armor_trim")) return "leggings";
        if (parts.hasAllPartSegments("boots_part", "armor_trim")) return "boots";
        return "";
    }
    public boolean testArmorParts(ItemStack pStack) {
        return !getArmorMixture(pStack).isEmpty();
    }

    public float averageMod(ItemStack stack, ToDoubleFunction<IToolPart> getter) {
        var parts = getToolParts(stack);
        if (parts.isEmpty()) return 0f;
        double sum = 0;
        for (var p : parts) sum += getter.applyAsDouble(p);
        return round2((float)(sum / parts.size()));
    }

    static float round2(float v) { return ((int) (v * 100f) / 100f); }
}
