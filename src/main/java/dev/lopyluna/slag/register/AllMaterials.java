package dev.lopyluna.slag.register;

import dev.lopyluna.slag.content.items.MaterialType;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;

public class AllMaterials {
    
    public static final List<MaterialType> MATERIAL_TYPES = new ArrayList<>();

    public static final MaterialType WOOD = register(new MaterialType.Builder("wooden", () -> Ingredient.of(ItemTags.PLANKS))
            .setSharp(3f)
            .setDura(64)
            .setTough(1)
            .setSpeed(2)
            .setEnch(15)
            .setTexture("soft")
            .registerTool());
            
    public static final MaterialType GLOWSTONE = register(new MaterialType.Builder("glowstone", () -> Ingredient.of(Items.GLOWSTONE))
            .setSharp(3.5f)
            .setDura(96)
            .setTough(1)
            .setSpeed(1)
            .setEnch(12)
            .setTexture("shiny")
            .registerTool());
            
    public static final MaterialType STONE = register(new MaterialType.Builder("stone", () -> Ingredient.of(ItemTags.STONE_TOOL_MATERIALS))
            .setSharp(4f)
            .setDura(128)
            .setTough(3)
            .setSpeed(4)
            .setEnch(5)
            .setTexture("soft")
            .registerTool());
            
    public static final MaterialType REDSTONE = register(new MaterialType.Builder("redstone", () -> Ingredient.of(Tags.Items.DUSTS_REDSTONE))
            .setSharp(4f)
            .setDura(160)
            .setTough(2)
            .setSpeed(3)
            .setEnch(16)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_REDSTONE::getSource)
            .registerTool());
            
    public static final MaterialType LAPIS = register(new MaterialType.Builder("lapis", () -> Ingredient.of(Tags.Items.GEMS_LAPIS))
            .setSharp(4.5f)
            .setDura(384)
            .setTough(3)
            .setSpeed(3)
            .setEnch(32)
            .moltenFluid(AllFluids.MOLTEN_LAPIS::getSource)
            .registerTool());
            
    public static final MaterialType COPPER = register(new MaterialType.Builder("copper", () -> Ingredient.of(Tags.Items.INGOTS_COPPER))
            .setSharp(4.5f)
            .setDura(192)
            .setTough(3)
            .setSpeed(5)
            .setEnch(8)
            .moltenFluid(AllFluids.MOLTEN_COPPER::getSource)
            .registerTool());
            
    public static final MaterialType AMETHYST = register(new MaterialType.Builder("amethyst", () -> Ingredient.of(Tags.Items.GEMS_AMETHYST))
            .setSharp(5.5f)
            .setDura(224)
            .setTough(3)
            .setSpeed(7)
            .setTexture("metal")
            .setEnch(18)
            .moltenFluid(AllFluids.MOLTEN_AMETHYST::getSource)
            .registerTool());
            
    public static final MaterialType GOLD = register(new MaterialType.Builder("golden", () -> Ingredient.of(Tags.Items.INGOTS_GOLD))
            .setSharp(3f)
            .setDura(32)
            .setTough(2)
            .setSpeed(12)
            .setEnch(22)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_GOLD::getSource)
            .registerTool());
            
    public static final MaterialType IRON = register(new MaterialType.Builder("iron", () -> Ingredient.of(Tags.Items.INGOTS_IRON))
            .setSharp(5f)
            .setDura(256)
            .setTough(4)
            .setSpeed(6)
            .setEnch(14)
            .moltenFluid(AllFluids.MOLTEN_IRON::getSource)
            .registerTool());
            
    public static final MaterialType ROSE_GOLD = register(new MaterialType.Builder("rose_gold", () -> Ingredient.of(AllTags.itemC("ingots/rose_gold")))
            .setSharp(6f)
            .setDura(480)
            .setTough(4)
            .setSpeed(10)
            .setEnch(15)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_ROSE_GOLD::getSource)
            .registerTool());
            
    public static final MaterialType QUARTZ = register(new MaterialType.Builder("quartz", () -> Ingredient.of(Tags.Items.GEMS_QUARTZ))
            .setSharp(6.5f)
            .setDura(288)
            .setTough(4)
            .setSpeed(7)
            .setEnch(16)
            .setTexture("metal")
            .moltenFluid(AllFluids.MOLTEN_QUARTZ::getSource)
            .registerTool());
            
    public static final MaterialType EMERALD = register(new MaterialType.Builder("emerald", () -> Ingredient.of(Tags.Items.GEMS_EMERALD))
            .setSharp(5.5f)
            .setDura(512)
            .setTough(5)
            .setSpeed(8)
            .setEnch(9)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_EMERALD::getSource)
            .registerTool());
            
    public static final MaterialType DEEP_ALLOY_MATERIAL = register(new MaterialType.Builder("deep_alloy", () -> Ingredient.of(AllTags.itemC("ingots/deep_alloy")))
            .setSharp(5f)
            .setDura(704)
            .setTough(4)
            .setSpeed(5)
            .setEnch(11)
            .setTexture("shiny")
            .fireProof()
            .registerTool());
            
    public static final MaterialType PRISMARINE = register(new MaterialType.Builder("prismarine", () -> Ingredient.of(Tags.Items.GEMS_PRISMARINE))
            .setSharp(6f)
            .setDura(1280)
            .setTough(4)
            .setSpeed(9)
            .setEnch(11)
            .moltenFluid(AllFluids.MOLTEN_PRISMARINE::getSource)
            .registerTool());
            
    public static final MaterialType BLUE_ICE = register(new MaterialType.Builder("blue_icy", () -> Ingredient.of(Items.BLUE_ICE))
            .setSharp(7f)
            .setDura(768)
            .setTough(4)
            .setSpeed(6)
            .setEnch(6)
            .setTexture("shiny")
            .registerTool());
            
    public static final MaterialType DIAMOND = register(new MaterialType.Builder("diamond", () -> Ingredient.of(Tags.Items.GEMS_DIAMOND))
            .setSharp(6f)
            .setDura(1024)
            .setTough(5)
            .setSpeed(8)
            .setEnch(10)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_DIAMOND::getSource)
            .registerTool());
            
    public static final MaterialType OBSIDIAN = register(new MaterialType.Builder("obsidian", () -> Ingredient.of(Tags.Items.OBSIDIANS))
            .setSharp(6.5f)
            .setDura(2560)
            .setTough(5)
            .setSpeed(5)
            .setEnch(21)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_OBSIDIAN::getSource)
            .registerTool());
            
    public static final MaterialType ECHO = register(new MaterialType.Builder("echo", () -> Ingredient.of(Items.ECHO_SHARD))
            .setSharp(7f)
            .setDura(1536)
            .setTough(6)
            .setSpeed(10)
            .setEnch(24)
            .setTexture("metal")
            .registerTool());
            
    public static final MaterialType NETHERITE = register(new MaterialType.Builder("netherite", () -> Ingredient.of(Tags.Items.INGOTS_NETHERITE))
            .setSharp(7f)
            .setDura(2048)
            .setTough(6)
            .setSpeed(9)
            .setEnch(15)
            .setTexture("metal")
            .fireProof()
            .moltenFluid(AllFluids.MOLTEN_NETHERITE::getSource)
            .registerTool());
            
    public static final MaterialType POPPED_CHORUS = register(new MaterialType.Builder("purpur", () -> Ingredient.of(Items.POPPED_CHORUS_FRUIT))
            .setSharp(6.5f)
            .setDura(832)
            .setTough(4)
            .setSpeed(5)
            .setEnch(16)
            .registerTool());
            
    public static final MaterialType NAUTILUS = register(new MaterialType.Builder("nautilus", () -> Ingredient.of(Items.NAUTILUS_SHELL))
            .setSharp(6f)
            .setDura(1120)
            .setTough(3)
            .setSpeed(11)
            .setEnch(19)
            .setTexture("soft")
            .registerTool());
            
    public static final MaterialType BONE = register(new MaterialType.Builder("bone", () -> Ingredient.of(Tags.Items.BONES))
            .setSharp(5.5f)
            .setDura(144)
            .setTough(3)
            .setSpeed(4)
            .setEnch(8)
            .setTexture("soft")
            .registerTool());
            
    public static final MaterialType FLINT = register(new MaterialType.Builder("flint", () -> Ingredient.of(Items.FLINT))
            .setSharp(5f)
            .setDura(112)
            .setTough(2)
            .setSpeed(3)
            .setEnch(5)
            .setTexture("metal")
            .registerTool());

    private static MaterialType register(MaterialType material) {
        MATERIAL_TYPES.add(material);
        return material;
    }
}

