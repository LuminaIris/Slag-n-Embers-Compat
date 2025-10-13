package dev.lopyluna.slag.register;

import com.tterrag.registrate.util.entry.ItemEntry;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.content.datagen.DualCookingRecipeBuilder;
import dev.lopyluna.slag.content.datagen.MeltingRecipeBuilder;
import dev.lopyluna.slag.content.items.MaterialType;
import dev.lopyluna.slag.content.items.dynamic_mold.DynamicMoldItem;
import dev.lopyluna.slag.content.items.modular_tool.BakedModularToolItem;
import dev.lopyluna.slag.content.items.modular_tool.ModularToolItem;
import dev.lopyluna.slag.content.items.modular_tool.ModularToolPartItem;
import dev.lopyluna.slag.content.items.modular_tool.ToolPartType;
import net.createmod.catnip.data.Iterate;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;

import static com.tterrag.registrate.providers.RegistrateRecipeProvider.has;
import static com.tterrag.registrate.providers.RegistrateRecipeProvider.netheriteSmithing;
import static dev.lopyluna.slag.SlagEmbers.REG;
import static dev.lopyluna.slag.content.AllUtils.compressible9x;
import static dev.lopyluna.slag.register.AllCreativeTabs.findPart;

@SuppressWarnings("unused")
public class AllItems {

    public static final List<ToolPartType> TOOL_PART_TYPES = new ArrayList<>();

    public static final ToolPartType PICKAXE_HEAD = new ToolPartType.Builder("pickaxe_head")
            .setSharpMod(0.6f)
            .setDuraMod(1)
            .setSpeedMod(2.8f)
            .register();
    public static final ToolPartType AXE_HEAD = new ToolPartType.Builder("axe_head")
            .setSharpMod(1.6f)
            .setDuraMod(1)
            .setSpeedMod(3f)
            .register();
    public static final ToolPartType SHOVEL_HEAD = new ToolPartType.Builder("shovel_head")
            .setSharpMod(0.75f)
            .setDuraMod(1)
            .setSpeedMod(3f)
            .register();
    public static final ToolPartType HOE_HEAD = new ToolPartType.Builder("hoe_head")
            .setSharpMod(0f)
            .setDuraMod(1)
            .setSpeedMod(0f)
            .register();
    public static final ToolPartType SWORD_BLADE = new ToolPartType.Builder("sword_blade")
            .setSharpMod(1f)
            .setDuraMod(1)
            .setSpeedMod(2.4f)
            .register();
    public static final ToolPartType GUARD = new ToolPartType.Builder("guard")
            .setSharpMod(1f)
            .setDuraMod(1)
            .setSpeedMod(2.4f)
            .register();

    public static final List<MaterialType> MATERIAL_TYPES = AllMaterials.MATERIAL_TYPES;

    public static final MaterialType WOOD = AllMaterials.WOOD;
    public static final MaterialType GLOWSTONE = AllMaterials.GLOWSTONE;
    public static final MaterialType STONE = AllMaterials.STONE;
    public static final MaterialType REDSTONE = AllMaterials.REDSTONE;
    public static final MaterialType LAPIS = AllMaterials.LAPIS;
    public static final MaterialType COPPER = AllMaterials.COPPER;
    public static final MaterialType AMETHYST = AllMaterials.AMETHYST;
    public static final MaterialType GOLD = AllMaterials.GOLD;
    public static final MaterialType IRON = AllMaterials.IRON;
    public static final MaterialType ROSE_GOLD = AllMaterials.ROSE_GOLD;
    public static final MaterialType QUARTZ = AllMaterials.QUARTZ;
    public static final MaterialType EMERALD = AllMaterials.EMERALD;
    public static final MaterialType DEEP_ALLOY_MATERIAL = AllMaterials.DEEP_ALLOY_MATERIAL;
    public static final MaterialType PRISMARINE = AllMaterials.PRISMARINE;
    public static final MaterialType BLUE_ICE = AllMaterials.BLUE_ICE;
    public static final MaterialType DIAMOND = AllMaterials.DIAMOND;
    public static final MaterialType OBSIDIAN = AllMaterials.OBSIDIAN;
    public static final MaterialType ECHO = AllMaterials.ECHO;
    public static final MaterialType NETHERITE = AllMaterials.NETHERITE;
    public static final MaterialType POPPED_CHORUS = AllMaterials.POPPED_CHORUS;
    public static final MaterialType NAUTILUS = AllMaterials.NAUTILUS;
    public static final MaterialType BONE = AllMaterials.BONE;
    public static final MaterialType FLINT = AllMaterials.FLINT;

    static {

        TOOL_PART_TYPES.add(AXE_HEAD);
        TOOL_PART_TYPES.add(PICKAXE_HEAD);
        TOOL_PART_TYPES.add(SHOVEL_HEAD);
        TOOL_PART_TYPES.add(HOE_HEAD);
        TOOL_PART_TYPES.add(SWORD_BLADE);
        TOOL_PART_TYPES.add(GUARD);
    }

    public static final ItemEntry<BakedModularToolItem> BAKED_TOOL = REG.item("baked_tool", BakedModularToolItem::new)
            .model((c, p) -> p.withExistingParent(c.getName(), "item/handheld"))
            .register();

    public static final ItemEntry<ModularToolItem> MODULAR_TOOL = REG.item("modular_tool", ModularToolItem::new)
            .model((c, p) ->
                    p.withExistingParent(c.getName(), "item/generated").texture("layer0", SlagEmbers.loc("item/blueprint")))
            .recipe((c, p) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, c.get())
                    .requires(Items.PAPER)
                    .requires(Items.PAPER)
                    .requires(Items.PAPER)
                    .requires(Items.CLAY_BALL)
                    .unlockedBy("has_paper", has(Items.PAPER))
                    .save(p, SlagEmbers.loc("crafting/" + c.getName()))
            ).lang("Modular Tool Blueprint")
            .register();

    public static final ItemEntry<DynamicMoldItem> SANDSTONE_MOLD = REG.item("sandstone_mold", DynamicMoldItem::new)
            .recipe((c, p) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, c.get(), 4)
                    .requires(Tags.Items.SANDSTONE_UNCOLORED_BLOCKS).unlockedBy("has_sandstone", has(Tags.Items.SANDSTONE_UNCOLORED_BLOCKS)).save(p, SlagEmbers.loc("crafting/" + c.getName())))
            .model((c, p) -> {
                var castTypes = new ArrayList<>(List.of("axe_heads", "balls", "dusts", "gems", "guards", "hoe_heads", "ingots", "nuggets", "pickaxe_heads", "rods", "shovel_heads", "sword_blades"));
                for (var cast : castTypes) for (var cutout : Iterate.trueAndFalse) {
                    var loc = SlagEmbers.loc("item/" + (cutout ? "cutout/" : "") + c.getName() + "/" + cast);
                    p.withExistingParent(loc.getPath(), "item/generated").texture("layer0", loc);
                }
                p.withExistingParent("item/cutout/" + c.getName(), "item/generated").texture("layer0", SlagEmbers.loc("item/cutout/" + c.getName()));
                p.withExistingParent(c.getName(), "item/generated").texture("layer0", SlagEmbers.loc("item/" + c.getName()));
            }).tag(AllTags.MOLDS_SINGLE)
            .register();
    public static final ItemEntry<DynamicMoldItem> TERRACOTTA_MOLD = REG.item("terracotta_mold", DynamicMoldItem::new)
            .recipe((c, p) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, c.get(), 2)
                    .requires(ItemTags.TERRACOTTA).unlockedBy("has_terracotta", has(ItemTags.TERRACOTTA)).save(p, SlagEmbers.loc("crafting/" + c.getName())))
            .model((c, p) -> {
                var castTypes = new ArrayList<>(List.of("axe_heads", "balls", "dusts", "gems", "guards", "hoe_heads", "ingots", "nuggets", "pickaxe_heads", "rods", "shovel_heads", "sword_blades"));
                for (var cast : castTypes) for (var cutout : Iterate.trueAndFalse) {
                    var loc = SlagEmbers.loc("item/" + (cutout ? "cutout/" : "") + c.getName() + "/" + cast);
                    p.withExistingParent(loc.getPath(), "item/generated").texture("layer0", loc);
                }
                p.withExistingParent("item/cutout/" + c.getName(), "item/generated").texture("layer0", SlagEmbers.loc("item/cutout/" + c.getName()));
                p.withExistingParent(c.getName(), "item/generated").texture("layer0", SlagEmbers.loc("item/" + c.getName()));
            }).tag(AllTags.MOLDS_REUSABLE)
            .register();

    public static final ItemEntry<Item> ROSE_GOLD_INGOT = REG.item("rose_gold_ingot", Item::new)
            .recipe((c, p) -> {
                compressible9x(c, p, Ingredient.of(AllTags.itemC("ingots/rose_gold")), Ingredient.of(AllTags.itemC("storage_blocks/rose_gold")), c.get(), AllBlocks.ROSE_GOLD_BLOCK);
                DualCookingRecipeBuilder.create(RecipeCategory.MISC, c.get(), 2, AllTags.itemC("ingots/copper"), AllTags.itemC("ingots/gold"), 1.4f)
                        .unlockedBy("has_copper", has(AllTags.itemC("ingots/copper")))
                        .save(p, SlagEmbers.loc("double_smelting/" + c.getName()));
            })
            .tag(AllTags.itemC("ingots/rose_gold"), AllTags.itemC("ingots"), ItemTags.BEACON_PAYMENT_ITEMS)
            .register();
    public static final ItemEntry<Item> ROSE_GOLD_NUGGET = REG.item("rose_gold_nugget", Item::new)
            .recipe((c, p) -> compressible9x(c, p,
                    Ingredient.of(AllTags.itemC("nuggets/rose_gold")), Ingredient.of(AllTags.itemC("ingots/rose_gold")),
                    c.get(), ROSE_GOLD_INGOT))
            .tag(AllTags.itemC("nuggets/rose_gold"), AllTags.itemC("nuggets"))
            .register();


    public static final ItemEntry<Item> DEEP_ALLOY = REG.item("deep_alloy", Item::new)
            .recipe((c, p) -> {
                compressible9x(c, p, Ingredient.of(AllTags.itemC("ingots/deep_alloy")), Ingredient.of(AllTags.itemC("storage_blocks/deep_alloy")), c.get(), AllBlocks.DEEP_ALLOY_BLOCK);
                DualCookingRecipeBuilder.create(RecipeCategory.MISC, c.get(), AllTags.itemC("ingots/iron"), Items.POLISHED_DEEPSLATE, 1.4f)
                        .unlockedBy("has_iron", has(AllTags.itemC("ingots/iron")))
                        .save(p, SlagEmbers.loc("double_smelting/" + c.getName()));
            }).tag(AllTags.itemC("ingots/deep_alloy"), AllTags.itemC("ingots"), ItemTags.BEACON_PAYMENT_ITEMS)
            .register();

    public static final List<ItemEntry<ModularToolPartItem>> TOOL_PARTS = new ArrayList<>();

    static {
        for (var material : MATERIAL_TYPES) for (var part : TOOL_PART_TYPES) {
            registerPart(material, part);
            registerPart(material, part);
            registerPart(material, part);
            registerPart(material, part);
            registerPart(material, part);
            registerPart(material, part);
        }
    }

    public static void registerPart(MaterialType material, ToolPartType part) {
        var reg = REG.item(material.id + "_" + part.id, p -> new ModularToolPartItem(material, part, p))
                .model((c, p) -> {
                    var textMat = material.texture.equals("base") ? "" : material.texture + "/";
                    var partBase = part.id.replace("_head", "").replace("_blade", "");
                    for (var mixture : testMixture(part.id)) {
                        var name = material.id + "_" + part.id + "_" + mixture;
                        var texture = "item/built/" + textMat + partBase + "_" + mixture + "_" + material.id;
                        p.existingFileHelper.trackGenerated(SlagEmbers.loc(texture), ModelProvider.TEXTURE);
                        p.withExistingParent(name, "item/handheld").texture("layer0", SlagEmbers.loc(texture));
                    }
                    p.existingFileHelper.trackGenerated(SlagEmbers.loc("item/" + textMat + part.id + "_" + material.id), ModelProvider.TEXTURE);
                    p.existingFileHelper.trackGenerated(SlagEmbers.loc("item/built/" + textMat + part.id + "_" + material.id), ModelProvider.TEXTURE);
                    p.withExistingParent(c.getName() + "_built", "item/handheld").texture("layer0", SlagEmbers.loc("item/built/" + textMat + part.id + "_" + material.id));
                    p.withExistingParent(c.getName(), "item/generated").texture("layer0", SlagEmbers.loc("item/" + textMat + part.id + "_" + material.id));
                }).recipe((c, p) -> {
                    if (material.id.equals("netherite")) {
                        var stack = findPart(DIAMOND, part.id);
                        if (!stack.isEmpty()) netheriteSmithing(p, stack.getItem(), RecipeCategory.TOOLS, c.get());
                    } else buildPattern(ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, c.get()), part)
                            .define('M', material.repairMaterials.get())
                            .define('R', Items.PAPER)
                            .unlockedBy("has_blueprint", has(MODULAR_TOOL))
                            .save(p, SlagEmbers.loc("crafting/parts/" + c.getName()));

                    var fluid = material.moltenFluid.get();
                    if (fluid != null) {
                        var cast = getCast(part);

                        var size = MelterBE.INGOT_SIZE;
                        if (fluid == AllFluids.MOLTEN_OBSIDIAN.getSource()) size = MelterBE.BLOCK_SIZE;
                        size *= getSize(part);
                        if (size > 0 && fluid == AllFluids.MOLTEN_NETHERITE.getSource()) size = MelterBE.INGOT_SIZE;

                        if (size > 0) {
                            if (cast != null) AllBlocks.create(p, part.id, material.id, c.get(), fluid, size, cast);
                            MeltingRecipeBuilder.create(fluid, size, c.get())
                                    .unlockedBy("has_blueprint", has(MODULAR_TOOL))
                                    .save(p, SlagEmbers.loc("melting/" + c.getName()));
                        }
                    }
                }).tag(AllTags.item("cast/" + part.id + "s"))
                ;
        if (material.fireProof) reg = reg.properties(Item.Properties::fireResistant);
        TOOL_PARTS.add(reg.register());
    }

    public static TagKey<Item> getCast(ToolPartType part) {
        return switch (part.id) {
            case "axe_head" -> AllTags.CAST_AXE_HEADS;
            case "pickaxe_head" -> AllTags.CAST_PICKAXE_HEADS;
            case "shovel_head" -> AllTags.CAST_SHOVEL_HEADS;
            case "hoe_head" -> AllTags.CAST_HOE_HEADS;
            case "sword_blade" -> AllTags.CAST_SWORD_BLADES;
            case "guard" -> AllTags.CAST_GUARDS;
            default -> null;
        };
    }

    public static int getSize(ToolPartType part) {
        return switch (part.id) {
            case "axe_head", "pickaxe_head" -> 3;
            case "sword_blade", "hoe_head" -> 2;
            case "guard", "shovel_head" -> 1;
            default -> 0;
        };
    }

    public static ShapedRecipeBuilder buildPattern(ShapedRecipeBuilder value, ToolPartType part) {
        return switch (part.id) {
            case "axe_head" -> value
                    .pattern("MM")
                    .pattern("MR")
                    .pattern(" R");
            case "pickaxe_head" -> value
                    .pattern("MMM")
                    .pattern(" R ")
                    .pattern(" R ");
            case "shovel_head" -> value
                    .pattern("M")
                    .pattern("R")
                    .pattern("R");
            case "hoe_head" -> value
                    .pattern("MM")
                    .pattern(" R")
                    .pattern(" R");
            case "sword_blade" -> value
                    .pattern("M")
                    .pattern("M")
                    .pattern("R");
            case "guard" -> value
                    .pattern("RMR");
            default -> value;
        };
    }

    public static List<String> testMixture(String part) {
        List<String> parts = new ArrayList<>();
        if (List.of("axe_head", "hoe_head").contains(part)) parts.add("mattock");
        if (List.of("pickaxe_head", "shovel_head").contains(part)) parts.add("prybar");
        if (List.of("shovel_head", "hoe_head").contains(part)) parts.add("graip");
        if (List.of("pickaxe_head", "axe_head").contains(part)) parts.add("mallet");

        if (List.of("pickaxe_head", "axe_head", "shovel_head").contains(part)) parts.add("hammer");
        if (List.of("hoe_head", "sword_blade", "guard").contains(part)) parts.add("scythe");
        if (List.of("pickaxe_head", "axe_head", "sword_blade").contains(part)) parts.add("maul");

        if (List.of("pickaxe_head", "axe_head", "shovel_head", "hoe_head", "sword_blade").contains(part)) parts.add("paxel");
        return parts;
    }


    public static void register() {}
}
