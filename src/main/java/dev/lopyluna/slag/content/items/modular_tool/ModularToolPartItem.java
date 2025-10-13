package dev.lopyluna.slag.content.items.modular_tool;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.MaterialType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModularToolPartItem extends Item implements IToolPart {
    MaterialType type;
    ToolPartType toolSegment;
    public ModularToolPartItem(MaterialType type, ToolPartType toolSegment, Properties properties) {
        super(properties);
        this.type = type;
        this.toolSegment = toolSegment;
    }

    @Override
    public MaterialType getMaterialType() {
        return type;
    }

    @Override
    public ToolPartType getToolPartSegment() {
        return toolSegment;
    }

    @Override
    public ResourceLocation getPartSegment() {
        if (toolSegment.id.contains("c:")) return SlagEmbers.loc(toolSegment.id);
        if (toolSegment.id.contains(":")) return ResourceLocation.parse(toolSegment.id);
        return ResourceLocation.fromNamespaceAndPath(BuiltInRegistries.ITEM.getKey(this).getNamespace(), toolSegment.id);
    }
}
