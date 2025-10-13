package dev.lopyluna.slag.content.items.modular_armor;

import dev.lopyluna.slag.content.items.MaterialType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Math;

public interface IArmorPart {
    MaterialType getMaterialType();
    ArmorPartType getArmorPartSegment();
    ResourceLocation getPartSegment();

    default float getDuraMod() { return org.joml.Math.max(getArmorPartSegment().duraMod, 0f); }

    default float getSpeed() { return Math.max(getMaterialType().speed, 0f); }
    default float getDura() { return getMaterialType().dura; }
    default float getTough() { return getMaterialType().tough; }
    default float getSharp() { return getMaterialType().sharp; }
    default float getEnch() { return getMaterialType().ench; }
}
