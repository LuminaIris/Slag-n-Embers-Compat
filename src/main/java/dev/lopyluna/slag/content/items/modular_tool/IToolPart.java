package dev.lopyluna.slag.content.items.modular_tool;

import dev.lopyluna.slag.content.items.MaterialType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Math;

public interface IToolPart {
    MaterialType getMaterialType();
    ToolPartType getToolPartSegment();
    ResourceLocation getPartSegment();

    default float getSpeedMod() { return Math.max(getToolPartSegment().speedMod, 0f); }
    default float getDuraMod() { return Math.max(getToolPartSegment().duraMod, 0f); }
    default float getSharpMod() { return Math.max(getToolPartSegment().sharpMod, 0f); }

    default float getSpeed() { return Math.max(getMaterialType().speed, 0f); }
    default float getDura() { return getMaterialType().dura; }
    default float getTough() { return getMaterialType().tough; }
    default float getSharp() { return getMaterialType().sharp; }
    default float getEnch() { return getMaterialType().ench; }
}
