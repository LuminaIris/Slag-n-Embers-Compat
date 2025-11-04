package dev.lopyluna.slag.register;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.FluidEntry;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.basin.BasinBlock;
import dev.lopyluna.slag.content.blocks.crucible.CrucibleBlock;
import dev.lopyluna.slag.content.blocks.crucible.CrucibleItem;
import dev.lopyluna.slag.content.blocks.crucible_interface.InterfaceBlock;
import dev.lopyluna.slag.content.blocks.drain.DrainBlock;
import dev.lopyluna.slag.content.blocks.forge.ForgeBlock;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.content.blocks.melter.MelterBlock;
import dev.lopyluna.slag.content.blocks.table.TableBlock;
import dev.lopyluna.slag.content.datagen.*;
import dev.lopyluna.slag.content.utils.BlockHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.Tags;

import java.util.function.Function;

import static com.tterrag.registrate.providers.RegistrateRecipeProvider.has;
import static dev.lopyluna.slag.SlagEmbers.REG;
import static dev.lopyluna.slag.content.blocks.crucible.CrucibleBE.*;
import static dev.lopyluna.slag.content.datagen.AlloyingRecipeBuilder.fluid;
import static dev.lopyluna.slag.content.datagen.MeltingRecipeBuilder.*;
import static dev.lopyluna.slag.content.utils.BlockHelper.getExistingModel;

@SuppressWarnings({"removal", "unused"})
public class AllBlocks {
    public static final BlockEntry<Block> ROSE_GOLD_BLOCK = REG.block("rose_gold_block", Block::new)
            .lang("Block of Rose Gold")
            .initialProperties(() -> Blocks.GOLD_BLOCK)
            .properties(p -> p.mapColor(MapColor.COLOR_PINK).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL, BlockTags.BEACON_BASE_BLOCKS, AllTags.blockC("storage_blocks"), AllTags.blockC("storage_blocks/rose_gold"))
            .item()
            .tag(AllTags.itemC("storage_blocks"), AllTags.itemC("storage_blocks/rose_gold"))
            .build()
            .register();

    public static final BlockEntry<Block> DEEP_ALLOY_BLOCK = REG.block("deep_alloy_block", Block::new)
            .lang("Block of Deep Alloy")
            .initialProperties(() -> Blocks.GOLD_BLOCK)
            .properties(p -> p.sound(AllSoundTypes.CRUCIBLE).noOcclusion().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL, BlockTags.BEACON_BASE_BLOCKS, AllTags.blockC("storage_blocks"), AllTags.blockC("storage_blocks/deep_alloy"))
            .item()
            .tag(AllTags.itemC("storage_blocks"), AllTags.itemC("storage_blocks/deep_alloy"))
            .build()
            .register();

    public static final BlockEntry<CrucibleBlock> CRUCIBLE = REG.block("crucible", CrucibleBlock::new)
            .initialProperties(() -> Blocks.CAULDRON)
            .blockstate((c, p) -> p.getVariantBuilder(c.get()).forAllStates(state -> {
                Function<BlockState, ModelFile> modelFunc = s -> {
                    var model = getExistingModel(c, p,
                            switch (s.getValue(SHAPE)) { case PLAIN -> ""; case INNER -> "inner"; case NW -> "nw"; case SW -> "sw"; case NE -> "ne"; case SE -> "se"; case NORTH -> "north"; case SOUTH -> "south"; case WEST -> "west"; case EAST -> "east"; },
                            s.getValue(TOP) ? "top" : "",
                            s.getValue(BOTTOM) ? "bottom" : ""
                    );
                    var loc = model.getLocation();
                    var wModel = p.models().withExistingParent(loc.getPath() + "_window", loc).texture("1", "block/crucible_side_window");
                    return s.getValue(WINDOW) ? wModel : model;
                };
                return ConfiguredModel.builder().modelFile(modelFunc.apply(state)).build();
            })).properties(p -> p.sound(AllSoundTypes.CRUCIBLE).noOcclusion().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops())
            .recipe((c, p) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 4)
                        .pattern("D D")
                        .pattern("D D")
                        .pattern("DBD")
                        .define('D', AllTags.itemC("ingots/deep_alloy")).define('B', AllTags.itemC("storage_blocks/deep_alloy"))
                        .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName()));

                AlloyingRecipeBuilder.create(AllFluids.MOLTEN_OBSIDIAN.getSource(), MelterBE.NUGGET_SIZE, fluid(Fluids.WATER, MelterBE.NUGGET_SIZE), fluid(Fluids.LAVA, MelterBE.NUGGET_SIZE))
                        .unlockedBy("has_lava", has(Items.LAVA_BUCKET)).save(p, SlagEmbers.loc("alloying/molten_obsidian"));
                AlloyingRecipeBuilder.create(AllFluids.MOLTEN_ROSE_GOLD.getSource(), MelterBE.NUGGET_SIZE * 2, fluid(AllFluids.MOLTEN_GOLD.getSource(), MelterBE.NUGGET_SIZE), fluid(AllFluids.MOLTEN_COPPER.getSource(), MelterBE.NUGGET_SIZE))
                        .unlockedBy("has_lava", has(Items.LAVA_BUCKET)).save(p, SlagEmbers.loc("alloying/molten_rose_gold"));
            })
            .addLayer(() -> RenderType::cutoutMipped)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .item(CrucibleItem::new)
            .model((c, p) -> p.withExistingParent("item/" + c.getName(), p.modLoc("block/"+c.getName()+"/block_top_bottom")))
            .build()
            .register();

    public static final BlockEntry<TableBlock> TABLE = REG.block("table", TableBlock::new)
            .initialProperties(() -> Blocks.CAULDRON)
            .lang("Casting Table")
            .blockstate((c, p) -> p.simpleBlock(c.get(), p.models().getExistingFile(p.modLoc("block/table"))))
            .properties(p -> p.sound(AllSoundTypes.CRUCIBLE).noOcclusion().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .recipe((c, p) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 1)
                        .pattern("AAA")
                        .pattern("A A")
                        .define('A', AllTags.itemC("ingots/deep_alloy"))
                        .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName()));

                create(p, "ingot", "copper", Items.COPPER_INGOT, AllFluids.MOLTEN_COPPER, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                create(p, "ingot", "gold", Items.GOLD_INGOT, AllFluids.MOLTEN_GOLD, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                create(p, "ingot", "iron", Items.IRON_INGOT, AllFluids.MOLTEN_IRON, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                create(p, "ingot", "rose_gold", AllItems.ROSE_GOLD_INGOT.get(), AllFluids.MOLTEN_ROSE_GOLD, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);
                create(p, "ingot", "netherite", Items.NETHERITE_INGOT, AllFluids.MOLTEN_NETHERITE, MelterBE.INGOT_SIZE, AllTags.CAST_INGOTS);

                create(p, "gem", "diamond", Items.DIAMOND, AllFluids.MOLTEN_DIAMOND, MelterBE.INGOT_SIZE, AllTags.CAST_GEMS);
                create(p, "gem", "emerald", Items.EMERALD, AllFluids.MOLTEN_EMERALD, MelterBE.INGOT_SIZE, AllTags.CAST_GEMS);
                create(p, "gem", "lapis", Items.LAPIS_LAZULI, AllFluids.MOLTEN_LAPIS, MelterBE.INGOT_SIZE, AllTags.CAST_GEMS);

                create(p, "gem", "quartz", Items.QUARTZ, AllFluids.MOLTEN_QUARTZ, MelterBE.INGOT_SIZE, AllTags.CAST_GEMS);
                create(p, "gem", "amethyst", Items.AMETHYST_SHARD, AllFluids.MOLTEN_AMETHYST, MelterBE.INGOT_SIZE, AllTags.CAST_GEMS);
                create(p, "gem", "prismarine", Items.PRISMARINE, AllFluids.MOLTEN_PRISMARINE, MelterBE.INGOT_SIZE, AllTags.CAST_GEMS);

                create(p, "dust", "redstone", Items.REDSTONE, AllFluids.MOLTEN_REDSTONE, MelterBE.INGOT_SIZE, AllTags.CAST_DUSTS);

                create(p, "nugget", "gold", Items.GOLD_NUGGET, AllFluids.MOLTEN_GOLD, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                create(p, "nugget", "iron", Items.IRON_NUGGET, AllFluids.MOLTEN_IRON, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);
                create(p, "nugget", "rose_gold", AllItems.ROSE_GOLD_NUGGET.get(), AllFluids.MOLTEN_ROSE_GOLD, MelterBE.NUGGET_SIZE, AllTags.CAST_NUGGETS);

            })
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
            .model((c, p) -> p.withExistingParent("item/" + c.getName(), p.modLoc("block/table")))
            .build()
            .register();

    public static void create(RegistrateRecipeProvider p, String key, String type, Item result, FluidEntry<?> fluid, int mb, TagKey<Item> castType) {
        create(p, key, type, result, fluid.getSource(), mb, castType);
    }
    public static void create(RegistrateRecipeProvider p, String key, String type, ItemStack result, FluidEntry<?> fluid, int mb, TagKey<Item> castType) {
        create(p, key, type, result, fluid.getSource(), mb, castType);
    }
    public static void create(RegistrateRecipeProvider p, String key, String type, Item result, Fluid fluid, int mb, TagKey<Item> castType) {
        create(p, key, type, new ItemStack(result), fluid, mb, castType);
    }
    public static void create(RegistrateRecipeProvider p, String key, String type, ItemStack result, Fluid fluid, int mb, TagKey<Item> castType) {
        TableCastingRecipeBuilder.create(result, fluid, mb, castType)
                .unlockedBy("has_" + key, has(AllTags.itemC(key + "s/" + type))).save(p, SlagEmbers.loc("casting/table/" + type + "_" + key));
    }

    public static final BlockEntry<BasinBlock> BASIN = REG.block("basin", BasinBlock::new)
            .initialProperties(() -> Blocks.CAULDRON)
            .lang("Casting Basin")
            .blockstate((c, p) -> p.simpleBlock(c.get(), p.models().getExistingFile(p.modLoc("block/basin"))))
            .properties(p -> p.sound(AllSoundTypes.CRUCIBLE).noOcclusion().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .recipe((c, p) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 1)
                        .pattern("A A")
                        .pattern("A A")
                        .pattern("AAA")
                        .define('A', AllTags.itemC("ingots/deep_alloy"))
                        .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName()));
                BasinCastingRecipeBuilder.create(Items.AMETHYST_BLOCK, 1, AllFluids.MOLTEN_AMETHYST.getSource(), MelterBE.SMALL_BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/amethyst"))).save(p, SlagEmbers.loc("casting/basin/amethyst_block"));
                BasinCastingRecipeBuilder.create(Items.COPPER_BLOCK, 1, AllFluids.MOLTEN_COPPER.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.COPPER_BLOCKS)).save(p, SlagEmbers.loc("casting/basin/copper_block"));
                BasinCastingRecipeBuilder.create(Items.DIAMOND_BLOCK, 1, AllFluids.MOLTEN_DIAMOND.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/diamond"))).save(p, SlagEmbers.loc("casting/basin/diamond_block"));
                BasinCastingRecipeBuilder.create(Items.EMERALD_BLOCK, 1, AllFluids.MOLTEN_EMERALD.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/emerald"))).save(p, SlagEmbers.loc("casting/basin/emerald_block"));
                BasinCastingRecipeBuilder.create(Items.GOLD_BLOCK, 1, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/gold"))).save(p, SlagEmbers.loc("casting/basin/gold_block"));
                BasinCastingRecipeBuilder.create(Items.IRON_BLOCK, 1, AllFluids.MOLTEN_IRON.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/iron"))).save(p, SlagEmbers.loc("casting/basin/iron_block"));
                BasinCastingRecipeBuilder.create(Items.LAPIS_BLOCK, 1, AllFluids.MOLTEN_LAPIS.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/lapis"))).save(p, SlagEmbers.loc("casting/basin/lapis_block"));
                BasinCastingRecipeBuilder.create(Items.NETHERITE_BLOCK, 1, AllFluids.MOLTEN_NETHERITE.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/netherite"))).save(p, SlagEmbers.loc("casting/basin/netherite_block"));
                BasinCastingRecipeBuilder.create(Items.QUARTZ_BLOCK, 1, AllFluids.MOLTEN_QUARTZ.getSource(), MelterBE.SMALL_BLOCK_SIZE).unlockedBy("has_block", has(AllTags.QUARTZ_BLOCKS)).save(p, SlagEmbers.loc("casting/basin/quartz_block"));
                BasinCastingRecipeBuilder.create(Items.REDSTONE_BLOCK, 1, AllFluids.MOLTEN_REDSTONE.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/redstone"))).save(p, SlagEmbers.loc("casting/basin/redstone_block"));
                BasinCastingRecipeBuilder.create(ROSE_GOLD_BLOCK.asItem(), 1, AllFluids.MOLTEN_ROSE_GOLD.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(AllTags.itemC("storage_blocks/rose_gold"))).save(p, SlagEmbers.loc("casting/basin/rose_gold_block"));

                BasinCastingRecipeBuilder.create(Items.OBSIDIAN, 1, AllFluids.MOLTEN_OBSIDIAN.getSource(), MelterBE.BLOCK_SIZE).unlockedBy("has_block", has(Tags.Items.OBSIDIANS)).save(p, SlagEmbers.loc("casting/basin/obsidian"));
            })
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
            .model((c, p) -> p.withExistingParent("item/" + c.getName(), p.modLoc("block/basin")))
            .build()
            .register();

    public static final BlockEntry<DrainBlock> DRAIN = REG.block("drain", DrainBlock::new)
            .initialProperties(() -> Blocks.CAULDRON)
            .blockstate(BlockHelper::genHorizontalDirectional)
            .properties(p -> p.sound(AllSoundTypes.CRUCIBLE).noOcclusion().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .recipe((c, p) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, c.get(), 1)
                    .requires(AllTags.itemC("ingots/deep_alloy")).unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName())))
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
            .model((c, p) -> p.withExistingParent("item/" + c.getName(), p.modLoc("block/drain")))
            .build()
            .register();

    public static final BlockEntry<MelterBlock> MELTER = REG.block("melter", MelterBlock::new)
            .initialProperties(() -> Blocks.CAULDRON)
            .blockstate(BlockHelper::genHorizontalDirectional)
            .properties(p -> p.sound(AllSoundTypes.CRUCIBLE).noOcclusion().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .recipe((c, p) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 1)
                        .pattern("AAA")
                        .pattern("RDR")
                        .pattern("AAA")
                        .define('A', AllTags.itemC("ingots/deep_alloy")).define('R', AllTags.itemC("ingots/rose_gold")).define('D', DRAIN.get())
                        .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName()));

                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_NETHERITE.getSource(), MelterBE.INGOT_SIZE, Items.NETHERITE_HELMET);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_NETHERITE.getSource(), MelterBE.INGOT_SIZE, Items.NETHERITE_CHESTPLATE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_NETHERITE.getSource(), MelterBE.INGOT_SIZE, Items.NETHERITE_LEGGINGS);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_NETHERITE.getSource(), MelterBE.INGOT_SIZE, Items.NETHERITE_BOOTS);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_NETHERITE.getSource(), MelterBE.INGOT_SIZE, Items.NETHERITE_SWORD);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_NETHERITE.getSource(), MelterBE.INGOT_SIZE, Items.NETHERITE_PICKAXE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_NETHERITE.getSource(), MelterBE.INGOT_SIZE, Items.NETHERITE_AXE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_NETHERITE.getSource(), MelterBE.INGOT_SIZE, Items.NETHERITE_SHOVEL);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_NETHERITE.getSource(), MelterBE.INGOT_SIZE, Items.NETHERITE_HOE);

                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_DIAMOND.getSource(), MelterBE.INGOT_SIZE * 5, Items.DIAMOND_HELMET);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_DIAMOND.getSource(), MelterBE.INGOT_SIZE * 8, Items.DIAMOND_CHESTPLATE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_DIAMOND.getSource(), MelterBE.INGOT_SIZE * 7, Items.DIAMOND_LEGGINGS);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_DIAMOND.getSource(), MelterBE.INGOT_SIZE * 4, Items.DIAMOND_BOOTS);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_DIAMOND.getSource(), MelterBE.INGOT_SIZE * 2, Items.DIAMOND_SWORD);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_DIAMOND.getSource(), MelterBE.INGOT_SIZE * 3, Items.DIAMOND_PICKAXE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_DIAMOND.getSource(), MelterBE.INGOT_SIZE * 3, Items.DIAMOND_AXE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_DIAMOND.getSource(), MelterBE.INGOT_SIZE, Items.DIAMOND_SHOVEL);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_DIAMOND.getSource(), MelterBE.INGOT_SIZE * 2, Items.DIAMOND_HOE);

                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE * 5, Items.GOLDEN_HELMET);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE * 8, Items.GOLDEN_CHESTPLATE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE * 7, Items.GOLDEN_LEGGINGS);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE * 4, Items.GOLDEN_BOOTS);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE * 2, Items.GOLDEN_SWORD);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE * 3, Items.GOLDEN_PICKAXE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE * 3, Items.GOLDEN_AXE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE, Items.GOLDEN_SHOVEL);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE * 2, Items.GOLDEN_HOE);

                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 5, Items.IRON_HELMET);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 8, Items.IRON_CHESTPLATE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 7, Items.IRON_LEGGINGS);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 4, Items.IRON_BOOTS);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 2, Items.IRON_SWORD);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 3, Items.IRON_PICKAXE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 3, Items.IRON_AXE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE, Items.IRON_SHOVEL);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 2, Items.IRON_HOE);

                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), 27, Items.IRON_BARS);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), 108, Items.CROSSBOW);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), 36, Items.TRIPWIRE_HOOK);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), 36, Items.TRAPPED_CHEST);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), 27, Items.RAIL);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 3, Items.IRON_DOOR);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 4, Items.IRON_TRAPDOOR);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 3, Items.BUCKET);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 2, Items.SHEARS);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 2, Items.HEAVY_WEIGHTED_PRESSURE_PLATE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE, Items.FLINT_AND_STEEL);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE, Items.SHIELD);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE, Items.ACTIVATOR_RAIL);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE, Items.DETECTOR_RAIL);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE, Items.LODESTONE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 4, Items.COMPASS);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 4, Items.RECOVERY_COMPASS);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 5, Items.HOPPER);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 7, Items.CAULDRON);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 5, Items.MINECART);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 5, Items.CHEST_MINECART);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 12, Items.HOPPER_MINECART);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 24, Items.HEAVY_CORE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE + MelterBE.NUGGET_SIZE * 2, Items.CHAIN);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.INGOT_SIZE * 2, Items.SMITHING_TABLE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.NUGGET_SIZE * 8, Items.SOUL_LANTERN);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_IRON.getSource(), MelterBE.NUGGET_SIZE * 8, Items.LANTERN);

                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE * 8, Items.GOLDEN_APPLE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE * 8 * 4, Items.ENCHANTED_GOLDEN_APPLE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.NUGGET_SIZE * 8, Items.GOLDEN_CARROT);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.BLOCK_SIZE, Items.BELL);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE * 2, Items.LIGHT_WEIGHTED_PRESSURE_PLATE);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE * 4, Items.CLOCK);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_GOLD.getSource(), MelterBE.INGOT_SIZE, Items.POWERED_RAIL);

                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_COPPER.getSource(), MelterBE.INGOT_SIZE, Items.BRUSH);
                MeltingRecipeBuilder.create(p, fluids(fluid(AllFluids.MOLTEN_COPPER.getSource(), MelterBE.INGOT_SIZE * 2), fluid(AllFluids.MOLTEN_AMETHYST.getSource(), MelterBE.INGOT_SIZE)), Items.SPYGLASS);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_COPPER.getSource(), MelterBE.INGOT_SIZE * 3, Items.LIGHTNING_ROD);
                MeltingRecipeBuilder.create(p, "copper_doors", AllFluids.MOLTEN_COPPER.getSource(), MelterBE.INGOT_SIZE * 3, AllTags.itemC("doors/copper"));
                MeltingRecipeBuilder.create(p, "copper_trapdoors", AllFluids.MOLTEN_COPPER.getSource(), MelterBE.INGOT_SIZE * 3, AllTags.itemC("trapdoors/copper"));
                MeltingRecipeBuilder.create(p, "copper_chiseled_blocks", AllFluids.MOLTEN_COPPER.getSource(), 162, AllTags.itemC("chiseled_blocks/copper"));
                MeltingRecipeBuilder.create(p, "copper_grate_blocks", AllFluids.MOLTEN_COPPER.getSource(), 162, AllTags.itemC("grate_blocks/copper"));
                MeltingRecipeBuilder.create(p, "copper_cut_blocks", AllFluids.MOLTEN_COPPER.getSource(), 162, AllTags.itemC("cut_blocks/copper"));

                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_DIAMOND.getSource(), MelterBE.INGOT_SIZE, Items.JUKEBOX);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_REDSTONE.getSource(), MelterBE.INGOT_SIZE, Items.NOTE_BLOCK);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_REDSTONE.getSource(), MelterBE.INGOT_SIZE * 4, Items.TARGET);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_AMETHYST.getSource(), MelterBE.INGOT_SIZE * 3, Items.CALIBRATED_SCULK_SENSOR);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_PRISMARINE.getSource(), MelterBE.INGOT_SIZE * 5, Items.SEA_LANTERN);
                MeltingRecipeBuilder.create(p, AllFluids.MOLTEN_OBSIDIAN.getSource(), MelterBE.BLOCK_SIZE * 6, Items.RESPAWN_ANCHOR);

                gemMeltable(p, "diamond", AllFluids.MOLTEN_DIAMOND.getSource(), Tags.Items.STORAGE_BLOCKS_DIAMOND, Tags.Items.GEMS_DIAMOND, AllTags.itemC("nuggets/diamond"));
                gemMeltable(p, "emerald", AllFluids.MOLTEN_EMERALD.getSource(), Tags.Items.STORAGE_BLOCKS_EMERALD, Tags.Items.GEMS_EMERALD, AllTags.itemC("nuggets/emerald"));
                gemMeltable(p, "lapis", AllFluids.MOLTEN_LAPIS.getSource(), Tags.Items.STORAGE_BLOCKS_LAPIS, Tags.Items.GEMS_LAPIS, AllTags.itemC("nuggets/lapis"));
                dustMeltable(p, "redstone", AllFluids.MOLTEN_REDSTONE.getSource(), Tags.Items.STORAGE_BLOCKS_REDSTONE, Tags.Items.DUSTS_REDSTONE);
                crystalMeltable(p, "quartz", AllFluids.MOLTEN_QUARTZ.getSource(), AllTags.QUARTZ_BLOCKS, Tags.Items.GEMS_QUARTZ);
                crystalMeltable(p, "amethyst", AllFluids.MOLTEN_AMETHYST.getSource(), AllTags.AMETHYST_BLOCKS, Tags.Items.GEMS_AMETHYST);
                crystalMeltable(p, "prismarine", AllFluids.MOLTEN_PRISMARINE.getSource(), null, Tags.Items.GEMS_PRISMARINE);
                ingotMeltable(p, "copper", AllFluids.MOLTEN_COPPER.getSource(), AllTags.COPPER_BLOCKS, Tags.Items.INGOTS_COPPER, AllTags.itemC("nuggets/copper"));
                ingotMeltable(p, "gold", AllFluids.MOLTEN_GOLD.getSource(), Tags.Items.STORAGE_BLOCKS_GOLD, Tags.Items.INGOTS_GOLD, Tags.Items.NUGGETS_GOLD);
                ingotMeltable(p, "iron", AllFluids.MOLTEN_IRON.getSource(), Tags.Items.STORAGE_BLOCKS_IRON, Tags.Items.INGOTS_IRON, Tags.Items.NUGGETS_IRON);
                ingotMeltable(p, "netherite", AllFluids.MOLTEN_NETHERITE.getSource(), Tags.Items.STORAGE_BLOCKS_NETHERITE, Tags.Items.INGOTS_NETHERITE, AllTags.itemC("nuggets/netherite"));
                ingotMeltable(p, "rose_gold", AllFluids.MOLTEN_ROSE_GOLD.getSource(), AllTags.itemC("storage_blocks/rose_gold"), AllTags.itemC("ingots/rose_gold"), AllTags.itemC("nuggets/rose_gold"));

                oreMeltable(p, "raw_copper", AllFluids.MOLTEN_COPPER.getSource(), Tags.Items.STORAGE_BLOCKS_RAW_COPPER, AllTags.COPPER_RAW_MATERIALS, null);
                oreMeltable(p, "raw_iron", AllFluids.MOLTEN_IRON.getSource(), Tags.Items.STORAGE_BLOCKS_RAW_IRON, AllTags.IRON_RAW_MATERIALS, null);
                oreMeltable(p, "raw_gold", AllFluids.MOLTEN_GOLD.getSource(), Tags.Items.STORAGE_BLOCKS_RAW_GOLD, AllTags.GOLD_RAW_MATERIALS, null);

                oreMeltableGem(p, "raw_diamond", AllFluids.MOLTEN_DIAMOND.getSource(), null, Tags.Items.ORES_DIAMOND, null);
                oreMeltableGem(p, "raw_emerald", AllFluids.MOLTEN_EMERALD.getSource(), null, Tags.Items.ORES_EMERALD, null);
                oreMeltableGem(p, "raw_quartz", AllFluids.MOLTEN_QUARTZ.getSource(), null, Tags.Items.ORES_QUARTZ, null);

                MeltingRecipeBuilder.create(AllFluids.MOLTEN_OBSIDIAN.getSource(), MelterBE.BLOCK_SIZE, Tags.Items.OBSIDIANS)
                        .unlockedBy("has_meltable_obsidian", has(Tags.Items.OBSIDIANS))
                        .save(p, SlagEmbers.loc("melting/obsidian_blocks"));

                MeltingRecipeBuilder.create(AllFluids.MOLTEN_AMETHYST.getSource(), MelterBE.INGOT_SIZE * 3, Items.LARGE_AMETHYST_BUD)
                        .unlockedBy("has_meltable_buds", has(Tags.Items.BUDS))
                        .save(p, SlagEmbers.loc("melting/large_amethyst_bud"));
                MeltingRecipeBuilder.create(AllFluids.MOLTEN_AMETHYST.getSource(), MelterBE.INGOT_SIZE * 2, Items.MEDIUM_AMETHYST_BUD)
                        .unlockedBy("has_meltable_buds", has(Tags.Items.BUDS))
                        .save(p, SlagEmbers.loc("melting/medium_amethyst_bud"));
                MeltingRecipeBuilder.create(AllFluids.MOLTEN_AMETHYST.getSource(), MelterBE.INGOT_SIZE, Items.SMALL_AMETHYST_BUD)
                        .unlockedBy("has_meltable_buds", has(Tags.Items.BUDS))
                        .save(p, SlagEmbers.loc("melting/small_amethyst_bud"));
            })
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
            .model((c, p) -> p.withExistingParent("item/" + c.getName(), p.modLoc("block/melter")))
            .build()
            .register();

    public static final BlockEntry<InterfaceBlock> INTERFACE = REG.block("crucible_interface", InterfaceBlock::new)
            .initialProperties(() -> Blocks.CAULDRON)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(AllSoundTypes.CRUCIBLE).requiresCorrectToolForDrops())
            .blockstate(BlockHelper::genHorizontalDirectional)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 1)
                    .pattern("AA")  .pattern("GG")
                    .define('A', AllTags.itemC("ingots/deep_alloy")).define('G', AllTags.itemC("ingots/gold"))
                    .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName())))
            .lang("Fluid Interface")
            .simpleItem()
            .register();

    public static final BlockEntry<ForgeBlock> FORGE = REG.block("brick_forge", ForgeBlock::new)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
                    .sound(SoundType.MUD_BRICKS)
                    .strength(2.5f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(BlockHelper.litBlockEmission(13))
            ).blockstate(BlockHelper.empty())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .recipe((c, p) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 1).pattern("BLB").pattern("LFL").pattern("BLB")
                        .define('F', Items.FURNACE).define('B', Items.MUD_BRICKS).define('L', ItemTags.LOGS)
                        .unlockedBy("has_furnace", has(Items.FURNACE)).save(p, SlagEmbers.loc("crafting/" + c.getName()));

                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.GLASS, Ingredient.of(ItemTags.SMELTS_TO_GLASS), 0.1F)
                        .unlockedBy("has_smelts_to_glass", has(ItemTags.SMELTS_TO_GLASS))
                        .save(p, SlagEmbers.loc("double_smelting/smelts_to_glass"));


                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.MISC, Items.BRICK, Ingredient.of(Items.CLAY_BALL), 0.3F)
                        .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                        .save(p, SlagEmbers.loc("double_smelting/brick"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.MISC, Items.CHARCOAL, Ingredient.of(ItemTags.LOGS_THAT_BURN), 0.15F)
                        .unlockedBy("has_log", has(ItemTags.LOGS_THAT_BURN))
                        .save(p, SlagEmbers.loc("double_smelting/charcoal"));

                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.MISC, Items.POPPED_CHORUS_FRUIT, Ingredient.of(Items.CHORUS_FRUIT), 0.1F)
                        .unlockedBy("has_chorus_fruit", has(Items.CHORUS_FRUIT))
                        .save(p, SlagEmbers.loc("double_smelting/popped_chorus_fruit"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.MISC, Items.LIME_DYE, Ingredient.of(Blocks.SEA_PICKLE), 0.1F)
                        .unlockedBy("has_sea_pickle", has(Blocks.SEA_PICKLE))
                        .save(p, SlagEmbers.loc("double_smelting/lime_dye"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.MISC, Items.GREEN_DYE, Ingredient.of(Blocks.CACTUS), 1.0F)
                        .unlockedBy("has_cactus", has(Blocks.CACTUS))
                        .save(p, SlagEmbers.loc("double_smelting/green_dye"));


                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.TERRACOTTA.asItem(), Ingredient.of(Blocks.CLAY), 0.35F)
                        .unlockedBy("has_clay_block", has(Blocks.CLAY))
                        .save(p, SlagEmbers.loc("double_smelting/terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.MISC, Items.NETHER_BRICK, Ingredient.of(Blocks.NETHERRACK), 0.1F)
                        .unlockedBy("has_netherrack", has(Blocks.NETHERRACK))
                        .save(p, SlagEmbers.loc("double_smelting/nether_brick"));

                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_STONE.asItem(), Ingredient.of(Blocks.STONE), 0.1F)
                        .unlockedBy("has_stone", has(Blocks.STONE))
                        .save(p, SlagEmbers.loc("double_smelting/smooth_stone"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_SANDSTONE.asItem(), Ingredient.of(Blocks.SANDSTONE), 0.1F)
                        .unlockedBy("has_sandstone", has(Blocks.SANDSTONE))
                        .save(p, SlagEmbers.loc("double_smelting/smooth_sandstone"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_RED_SANDSTONE.asItem(), Ingredient.of(Blocks.RED_SANDSTONE), 0.1F)
                        .unlockedBy("has_red_sandstone", has(Blocks.RED_SANDSTONE))
                        .save(p, SlagEmbers.loc("double_smelting/smooth_red_sandstone"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_QUARTZ.asItem(), Ingredient.of(Blocks.QUARTZ_BLOCK), 0.1F)
                        .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
                        .save(p, SlagEmbers.loc("double_smelting/smooth_quartz"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.SMOOTH_BASALT, Ingredient.of(Blocks.BASALT), 0.1F)
                        .unlockedBy("has_basalt", has(Blocks.BASALT))
                        .save(p, SlagEmbers.loc("double_smelting/smooth_basalt"));

                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.STONE.asItem(), Ingredient.of(Blocks.COBBLESTONE), 0.1F)
                        .unlockedBy("has_cobblestone", has(Blocks.COBBLESTONE))
                        .save(p, SlagEmbers.loc("double_smelting/stone"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.DEEPSLATE, Ingredient.of(Blocks.COBBLED_DEEPSLATE), 0.1F)
                        .unlockedBy("has_cobbled_deepslate", has(Blocks.COBBLED_DEEPSLATE))
                        .save(p, SlagEmbers.loc("double_smelting/deepslate"));

                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.CRACKED_STONE_BRICKS.asItem(), Ingredient.of(Blocks.STONE_BRICKS), 0.1F)
                        .unlockedBy("has_stone_bricks", has(Blocks.STONE_BRICKS))
                        .save(p, SlagEmbers.loc("double_smelting/cracked_stone_bricks"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.asItem(), Ingredient.of(Blocks.POLISHED_BLACKSTONE_BRICKS), 0.1F)
                        .unlockedBy("has_polished_blackstone_bricks", has(Blocks.POLISHED_BLACKSTONE_BRICKS))
                        .save(p, SlagEmbers.loc("double_smelting/cracked_polished_blackstone_bricks"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.CRACKED_NETHER_BRICKS.asItem(), Ingredient.of(Blocks.NETHER_BRICKS), 0.1F)
                        .unlockedBy("has_nether_bricks", has(Blocks.NETHER_BRICKS))
                        .save(p, SlagEmbers.loc("double_smelting/cracked_nether_bricks"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.CRACKED_DEEPSLATE_BRICKS.asItem(), Ingredient.of(Blocks.DEEPSLATE_BRICKS), 0.1F)
                        .unlockedBy("has_deepslate_bricks", has(Blocks.DEEPSLATE_BRICKS))
                        .save(p, SlagEmbers.loc("double_smelting/cracked_deepslate_bricks"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.BUILDING_BLOCKS, Blocks.CRACKED_DEEPSLATE_TILES.asItem(), Ingredient.of(Blocks.DEEPSLATE_TILES), 0.1F)
                        .unlockedBy("has_deepslate_tiles", has(Blocks.DEEPSLATE_TILES))
                        .save(p, SlagEmbers.loc("double_smelting/cracked_deepslate_tiles"));


                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.BLACK_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.BLACK_TERRACOTTA), 0.1F)
                        .unlockedBy("has_black_terracotta", has(Blocks.BLACK_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/black_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.BLUE_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.BLUE_TERRACOTTA), 0.1F)
                        .unlockedBy("has_blue_terracotta", has(Blocks.BLUE_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/blue_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.BROWN_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.BROWN_TERRACOTTA), 0.1F)
                        .unlockedBy("has_brown_terracotta", has(Blocks.BROWN_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/brown_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.CYAN_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.CYAN_TERRACOTTA), 0.1F)
                        .unlockedBy("has_cyan_terracotta", has(Blocks.CYAN_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/cyan_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.GRAY_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.GRAY_TERRACOTTA), 0.1F)
                        .unlockedBy("has_gray_terracotta", has(Blocks.GRAY_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/gray_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.GREEN_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.GREEN_TERRACOTTA), 0.1F)
                        .unlockedBy("has_green_terracotta", has(Blocks.GREEN_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/green_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.LIGHT_BLUE_TERRACOTTA), 0.1F)
                        .unlockedBy("has_light_blue_terracotta", has(Blocks.LIGHT_BLUE_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/light_blue_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.LIGHT_GRAY_TERRACOTTA), 0.1F)
                        .unlockedBy("has_light_gray_terracotta", has(Blocks.LIGHT_GRAY_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/light_gray_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.LIME_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.LIME_TERRACOTTA), 0.1F)
                        .unlockedBy("has_lime_terracotta", has(Blocks.LIME_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/lime_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.MAGENTA_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.MAGENTA_TERRACOTTA), 0.1F)
                        .unlockedBy("has_magenta_terracotta", has(Blocks.MAGENTA_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/magenta_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.ORANGE_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.ORANGE_TERRACOTTA), 0.1F)
                        .unlockedBy("has_orange_terracotta", has(Blocks.ORANGE_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/orange_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.PINK_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.PINK_TERRACOTTA), 0.1F)
                        .unlockedBy("has_pink_terracotta", has(Blocks.PINK_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/pink_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.PURPLE_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.PURPLE_TERRACOTTA), 0.1F)
                        .unlockedBy("has_purple_terracotta", has(Blocks.PURPLE_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/purple_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.RED_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.RED_TERRACOTTA), 0.1F)
                        .unlockedBy("has_red_terracotta", has(Blocks.RED_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/red_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.WHITE_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.WHITE_TERRACOTTA), 0.1F)
                        .unlockedBy("has_white_terracotta", has(Blocks.WHITE_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/white_glazed_terracotta"));
                DualCookingRecipeBuilder.doubleSingle(RecipeCategory.DECORATIONS, Blocks.YELLOW_GLAZED_TERRACOTTA.asItem(), Ingredient.of(Blocks.YELLOW_TERRACOTTA), 0.1F)
                        .unlockedBy("has_yellow_terracotta", has(Blocks.YELLOW_TERRACOTTA))
                        .save(p, SlagEmbers.loc("double_smelting/yellow_glazed_terracotta"));

            }).simpleItem()
            .register();


    public static final BlockEntry<Block> DEEP_ALLOY_BRICKS = REG.block("deep_alloy_bricks", Block::new)
            .lang("Deep Alloy Bricks")
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(AllSoundTypes.CRUCIBLE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL)
            .recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 4)
                    .pattern("DD").pattern("DD").define('D', AllTags.itemC("ingots/deep_alloy"))
                    .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName())))
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> DEEP_ALLOY_BRICK_STAIRS = REG.block("deep_alloy_brick_stairs", p -> new StairBlock(DEEP_ALLOY_BRICKS.getDefaultState(), p))
            .lang("Deep Alloy Brick Stairs")
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(AllSoundTypes.CRUCIBLE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL)
            .recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 4)
                    .pattern("D  ").pattern("DD ").pattern("DDD").define('D', DEEP_ALLOY_BRICKS.get())
                    .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName())))
            .blockstate((c, p) -> p.stairsBlock(c.get(), SlagEmbers.loc("block/deep_alloy_bricks")))
            .simpleItem()
            .register();

    public static final BlockEntry<SlabBlock> DEEP_ALLOY_BRICK_SLAB = REG.block("deep_alloy_brick_slab", SlabBlock::new)
            .initialProperties(() -> Blocks.DEEPSLATE_BRICK_SLAB)
            .lang("Deep Alloy Brick Slab")
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(AllSoundTypes.CRUCIBLE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL)
            .recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get(), 6)
                    .pattern("DDD").define('D', DEEP_ALLOY_BRICKS.get())
                    .unlockedBy("has_deep_alloy", has(AllTags.itemC("ingots/deep_alloy"))).save(p, SlagEmbers.loc("crafting/" + c.getName())))
            .blockstate((c, p) -> p.slabBlock(c.get(), SlagEmbers.loc("block/deep_alloy_bricks"), SlagEmbers.loc("block/deep_alloy_bricks")))
            .loot((p, c) -> p.add(c, p.createSlabItemTable(c)))
            .simpleItem()
            .register();

    public static void register() {}
}
