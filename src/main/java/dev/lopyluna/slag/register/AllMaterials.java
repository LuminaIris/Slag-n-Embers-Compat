package dev.lopyluna.slag.register;

import dev.lopyluna.slag.content.types.MaterialType;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;

@SuppressWarnings("unused")
public class AllMaterials {

    public static final MaterialType WOOD = register(new MaterialType.Builder("wooden", () -> Ingredient.of(ItemTags.PLANKS)).setSortOrder(10)
            .setSharp(3f)
            .setDura(64)
            .setTier(1)
            .setSpeed(2)
            .setEnch(15)
            .setDefense(1f)
            .setTexture("soft")
            .register());
            
    public static final MaterialType GLOWSTONE = register(new MaterialType.Builder("glowstone", () -> Ingredient.of(Items.GLOWSTONE)).setSortOrder(220)
            .setSharp(3.5f)
            .setDura(96)
            .setTier(1)
            .setSpeed(1)
            .setEnch(12)
            .setDefense(2f)
            .setKBRes(0.1F)
            .setTexture("shiny")
            .register());
            
    public static final MaterialType STONE = register(new MaterialType.Builder("stone", () -> Ingredient.of(ItemTags.STONE_TOOL_MATERIALS)).setSortOrder(20)
            .setSharp(4f)
            .setDura(128)
            .setTier(3)
            .setSpeed(4)
            .setEnch(5)
            .setDefense(2f)
            .setTough(1f)
            .setKBRes(0.1F)
            .setTexture("soft")
            .register());
            
    public static final MaterialType REDSTONE = register(new MaterialType.Builder("redstone", () -> Ingredient.of(Tags.Items.DUSTS_REDSTONE)).setSortOrder(160)
            .setSharp(4f)
            .setDura(160)
            .setTier(2)
            .setSpeed(3)
            .setEnch(16)
            .setDefense(3f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_REDSTONE::getSource)
            .register());
            
    public static final MaterialType LAPIS = register(new MaterialType.Builder("lapis", () -> Ingredient.of(Tags.Items.GEMS_LAPIS)).setSortOrder(170)
            .setSharp(4.5f)
            .setDura(384)
            .setTier(3)
            .setSpeed(3)
            .setEnch(32)
            .setDefense(4f)
            .setTough(1f)
            .moltenFluid(AllFluids.MOLTEN_LAPIS::getSource)
            .register());
            
    public static final MaterialType COPPER = register(new MaterialType.Builder("copper", () -> Ingredient.of(Tags.Items.INGOTS_COPPER)).setSortOrder(30)
            .setSharp(4.5f)
            .setDura(192)
            .setTier(3)
            .setSpeed(5)
            .setEnch(8)
            .setDefense(4f)
            .moltenFluid(AllFluids.MOLTEN_COPPER::getSource)
            .register());
            
    public static final MaterialType AMETHYST = register(new MaterialType.Builder("amethyst", () -> Ingredient.of(Tags.Items.GEMS_AMETHYST)).setSortOrder(180)
            .setSharp(5.5f)
            .setDura(224)
            .setTier(3)
            .setSpeed(7)
            .setTexture("metal")
            .setEnch(18)
            .setDefense(5f)
            .moltenFluid(AllFluids.MOLTEN_AMETHYST::getSource)
            .register());
            
    public static final MaterialType GOLD = register(new MaterialType.Builder("golden", () -> Ingredient.of(Tags.Items.INGOTS_GOLD)).setSortOrder(50)
            .setSharp(3f)
            .setDura(32)
            .setTier(2)
            .setSpeed(12)
            .setEnch(22)
            .setDefense(5f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_GOLD::getSource)
            .register());
            
    public static final MaterialType IRON = register(new MaterialType.Builder("iron", () -> Ingredient.of(Tags.Items.INGOTS_IRON)).setSortOrder(40)
            .setSharp(5f)
            .setDura(256)
            .setTier(4)
            .setSpeed(6)
            .setEnch(14)
            .setDefense(6f)
            .moltenFluid(AllFluids.MOLTEN_IRON::getSource)
            .register());
            
    public static final MaterialType ROSE_GOLD = register(new MaterialType.Builder("rose_gold", () -> Ingredient.of(AllTags.itemC("ingots/rose_gold"))).setSortOrder(110)
            .setSharp(6f)
            .setDura(480)
            .setTier(4)
            .setSpeed(10)
            .setEnch(15)
            .setDefense(6f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_ROSE_GOLD::getSource)
            .register());
            
    public static final MaterialType QUARTZ = register(new MaterialType.Builder("quartz", () -> Ingredient.of(Tags.Items.GEMS_QUARTZ)).setSortOrder(150)
            .setSharp(6.5f)
            .setDura(288)
            .setTier(4)
            .setSpeed(7)
            .setEnch(16)
            .setDefense(7f)
            .setTexture("metal")
            .moltenFluid(AllFluids.MOLTEN_QUARTZ::getSource)
            .register());
            
    public static final MaterialType EMERALD = register(new MaterialType.Builder("emerald", () -> Ingredient.of(Tags.Items.GEMS_EMERALD)).setSortOrder(100)
            .setSharp(5.5f)
            .setDura(512)
            .setTier(5)
            .setSpeed(8)
            .setEnch(9)
            .setDefense(7f)
            .setTough(1f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_EMERALD::getSource)
            .register());
            
    public static final MaterialType DEEP_ALLOY_MATERIAL = register(new MaterialType.Builder("deep_alloy", () -> Ingredient.of(AllTags.itemC("ingots/deep_alloy"))).setSortOrder(140)
            .setSharp(5f)
            .setDura(704)
            .setTier(4)
            .setSpeed(5)
            .setEnch(11)
            .setDefense(6f)
            .setTough(1f)
            .setKBRes(0.1F)
            .setTexture("shiny")
            .fireProof()
            .register());
            
    public static final MaterialType PRISMARINE = register(new MaterialType.Builder("prismarine", () -> Ingredient.of(Tags.Items.GEMS_PRISMARINE)).setSortOrder(130)
            .setSharp(6f)
            .setDura(1280)
            .setTier(4)
            .setSpeed(9)
            .setEnch(11)
            .setDefense(6f)
            .moltenFluid(AllFluids.MOLTEN_PRISMARINE::getSource)
            .register());
            
    public static final MaterialType BLUE_ICE = register(new MaterialType.Builder("blue_icy", () -> Ingredient.of(Items.BLUE_ICE)).setSortOrder(120)
            .setSharp(7f)
            .setDura(768)
            .setTier(4)
            .setSpeed(6)
            .setEnch(6)
            .setDefense(5f)
            .setTexture("shiny")
            .register());
            
    public static final MaterialType DIAMOND = register(new MaterialType.Builder("diamond", () -> Ingredient.of(Tags.Items.GEMS_DIAMOND)).setSortOrder(60)
            .setSharp(6f)
            .setDura(1024)
            .setTier(5)
            .setSpeed(8)
            .setEnch(10)
            .setDefense(8f)
            .setTough(2f)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_DIAMOND::getSource)
            .register());
            
    public static final MaterialType OBSIDIAN = register(new MaterialType.Builder("obsidian", () -> Ingredient.of(Tags.Items.OBSIDIANS)).setSortOrder(90)
            .setSharp(6.5f)
            .setDura(2560)
            .setTier(5)
            .setSpeed(5)
            .setEnch(21)
            .setDefense(7f)
            .setTough(5f)
            .setKBRes(0.1F)
            .setTexture("shiny")
            .moltenFluid(AllFluids.MOLTEN_OBSIDIAN::getSource)
            .register());
            
    public static final MaterialType ECHO = register(new MaterialType.Builder("echo", () -> Ingredient.of(Items.ECHO_SHARD)).setSortOrder(80)
            .setSharp(7f)
            .setDura(1536)
            .setTier(6)
            .setSpeed(10)
            .setEnch(24)
            .setDefense(7f)
            .setTough(3f)
            .setKBRes(0.1F)
            .setTexture("metal")
            .register());
            
    public static final MaterialType NETHERITE = register(new MaterialType.Builder("netherite", () -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)).setSortOrder(70)
            .setSharp(7f)
            .setDura(2048)
            .setTier(6)
            .setSpeed(9)
            .setEnch(15)
            .setDefense(8f)
            .setTough(3f)
            .setKBRes(0.1F)
            .setTexture("metal")
            .fireProof()
            .moltenFluid(AllFluids.MOLTEN_NETHERITE::getSource)
            .register());
            
    public static final MaterialType POPPED_CHORUS = register(new MaterialType.Builder("purpur", () -> Ingredient.of(Items.POPPED_CHORUS_FRUIT)).setSortOrder(190)
            .setSharp(6.5f)
            .setDura(832)
            .setTier(4)
            .setSpeed(5)
            .setEnch(16)
            .setDefense(6f)
            .setTough(1f)
            .setKBRes(0.1F)
            .register());
            
    public static final MaterialType NAUTILUS = register(new MaterialType.Builder("nautilus", () -> Ingredient.of(Items.NAUTILUS_SHELL)).setSortOrder(200)
            .setSharp(6f)
            .setDura(1120)
            .setTier(3)
            .setSpeed(11)
            .setEnch(19)
            .setDefense(5f)
            .setTough(1f)
            .setKBRes(0.1F)
            .setTexture("soft")
            .register());
            
    public static final MaterialType BONE = register(new MaterialType.Builder("bone", () -> Ingredient.of(Tags.Items.BONES)).setSortOrder(210)
            .setSharp(5.5f)
            .setDura(144)
            .setTier(3)
            .setSpeed(4)
            .setEnch(8)
            .setDefense(3f)
            .setTough(3f)
            .setTexture("soft")
            .register());
            
    public static final MaterialType FLINT = register(new MaterialType.Builder("flint", () -> Ingredient.of(Items.FLINT)).setSortOrder(230)
            .setSharp(5f)
            .setDura(112)
            .setTier(2)
            .setSpeed(3)
            .setEnch(5)
            .setDefense(2f)
            .setTough(1f)
            .setTexture("metal")
            .register());

    private static MaterialType register(MaterialType material) {
        return AllDynamicTypes.registerMaterial(material);
    }

    public static void register() {}
}

