package dev.lopyluna.slag.register;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.MaterialType;
import dev.lopyluna.slag.content.items.modular_tool.DataToolParts;
import dev.lopyluna.slag.content.items.modular_tool.IToolPart;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static dev.lopyluna.slag.SlagEmbers.REGISTER;
import static dev.lopyluna.slag.register.AllItems.MATERIAL_TYPES;
import static dev.lopyluna.slag.register.AllItems.TOOL_PART_TYPES;

@SuppressWarnings("unused")
public class AllCreativeTabs {
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(BASE_TAB.getKey())) event.remove(AllItems.BAKED_TOOL.asStack(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        if (event.getTabKey().equals(TOOLS_PARTS_TAB.getKey())) {
            for (var material : MATERIAL_TYPES) for (var part : TOOL_PART_TYPES) for (var entry : BuiltInRegistries.ITEM.entrySet()) {
                var item = entry.getValue();
                var key = BuiltInRegistries.ITEM.getKey(item);
                if (!key.getNamespace().equals(SlagEmbers.MOD_ID)) continue;
                if (!(item instanceof IToolPart tool)) continue;
                if (!tool.getToolPartSegment().equals(part) || !tool.getMaterialType().equals(material)) continue;
                event.remove(item.getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                event.accept(item.getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }

            for (var material : MATERIAL_TYPES) getToolMixture(material).forEach((tool, parts) -> {
                if (parts.isEmpty()) return;
                var baseTool = AllItems.BAKED_TOOL.asStack();
                var stick = Items.STICK.getDefaultInstance();
                stick.setCount(testRodCount(tool));
                parts.add(stick);

                baseTool.set(AllDataComponents.TOOL_PARTS, new DataToolParts(parts));
                if (material.fireProof) baseTool.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
                event.accept(baseTool, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            });
        }
    }

    public static ItemStack findPart(MaterialType mat, String segment) {
        for (var e : BuiltInRegistries.ITEM.entrySet()) {
            var item = e.getValue();
            if (item instanceof IToolPart p && p.getMaterialType().equals(mat) && p.getPartSegment().getPath().equals(segment)) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    }

    private static List<ItemStack> findParts(MaterialType mat, String... segments) {
        List<ItemStack> parts = new ArrayList<>();
        for (var segment : segments) {
            var part = findPart(mat, segment);
            if (!part.isEmpty()) parts.add(part);
        }
        return parts;
    }

    public static LinkedHashMap<String, List<ItemStack>> getToolMixture(MaterialType material) {
        LinkedHashMap<String, List<ItemStack>> parts = new LinkedHashMap<>();
        parts.put("sword", findParts(material, "sword_blade", "guard"));
        parts.put("shovel", findParts(material, "shovel_head"));
        parts.put("pickaxe", findParts(material, "pickaxe_head"));
        parts.put("axe", findParts(material, "axe_head"));
        parts.put("hoe", findParts(material, "hoe_head"));

        parts.put("mattock", findParts(material, "axe_head", "hoe_head"));
        parts.put("graip", findParts(material, "shovel_head", "hoe_head"));
        parts.put("prybar", findParts(material, "pickaxe_head", "shovel_head"));
        parts.put("mallet", findParts(material, "pickaxe_head", "axe_head"));

        parts.put("hammer", findParts(material, "pickaxe_head", "axe_head", "shovel_head"));
        parts.put("scythe", findParts(material, "hoe_head", "sword_blade", "guard"));
        parts.put("maul", findParts(material, "pickaxe_head", "axe_head", "sword_blade"));

        parts.put("paxel", findParts(material, "pickaxe_head", "axe_head", "shovel_head", "hoe_head", "sword_blade"));
        return parts;
    }

    public static int testRodCount(String tool) {
        return switch (tool) {
            case "sword" -> 1;
            case "pickaxe", "axe", "shovel", "hoe", "mattock", "prybar", "graip", "mallet" -> 2;
            case "hammer", "scythe", "maul", "paxel" -> 3;
            default -> 0;
        };

    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BASE_TAB = REGISTER.creativeTab().register("base_tab", () -> CreativeModeTab.builder()
            .title(Component.translatableWithFallback("itemGroup." + SlagEmbers.MOD_ID + ".base", SlagEmbers.NAME))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(AllItems.MODULAR_TOOL::asStack)
            .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TOOLS_PARTS_TAB = REGISTER.creativeTab().register("tools_parts_tab", () -> CreativeModeTab.builder()
            .title(Component.translatableWithFallback("itemGroup." + SlagEmbers.MOD_ID + ".tools_parts", "Tools & Parts"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> {
                var baseTool = AllItems.BAKED_TOOL.asStack();
                var material = AllMaterials.GOLD;
                var parts = findParts(material, "pickaxe_head", "axe_head", "sword_blade");
                var stick = Items.STICK.getDefaultInstance();
                stick.setCount(testRodCount("maul"));
                parts.add(stick);
                baseTool.set(AllDataComponents.TOOL_PARTS, new DataToolParts(parts));
                return baseTool;
            })
            .withSearchBar()
            .build());

    public static void register() {}
}
