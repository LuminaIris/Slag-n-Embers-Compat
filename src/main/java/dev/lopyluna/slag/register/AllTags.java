package dev.lopyluna.slag.register;

import com.tterrag.registrate.providers.RegistrateTagsProvider;
import dev.lopyluna.slag.SlagEmbers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

import java.util.function.Function;
import java.util.stream.Stream;

import static dev.lopyluna.slag.SlagEmbers.REG;

@SuppressWarnings({"deprecation", "unused"})
public class AllTags {

    public static void addGenerators() {
        REG.generateTags();
    }

    public static TagKey<Fluid> HOT_FLUIDS = fluid("hot_fluids");

    public static TagKey<Fluid> MOLTEN_METALS = fluid("molten_metals");
    public static TagKey<Fluid> MOLTEN_GEMS = fluid("molten_gems");
    public static TagKey<Fluid> MOLTEN_CRYSTALS = fluid("molten_crystals");
    public static TagKey<Fluid> MOLTEN_DUSTS = fluid("molten_dusts");
    public static TagKey<Fluid> MOLTEN_DUSTS_SMALL = fluid("molten_dusts_small");
    public static TagKey<Fluid> MOLTEN_BALLS = fluid("molten_balls");
    public static TagKey<Fluid> MOLTEN_BALLS_SMALL = fluid("molten_balls_small");

    public static void genFluidTags(RegistrateTagsProvider<Fluid> provIn) {
        TagsProvider<Fluid> prov = new TagsProvider<>(provIn, Fluid::builtInRegistryHolder);

        prov.tag(MOLTEN_METALS)
                .add(AllFluids.MOLTEN_COPPER.getSource())
                .add(AllFluids.MOLTEN_GOLD.getSource())
                .add(AllFluids.MOLTEN_IRON.getSource())
                .add(AllFluids.MOLTEN_NETHERITE.getSource())
                .add(AllFluids.MOLTEN_ROSE_GOLD.getSource())
        ;
        prov.tag(MOLTEN_GEMS)
                .add(AllFluids.MOLTEN_DIAMOND.getSource())
                .add(AllFluids.MOLTEN_EMERALD.getSource())
                .add(AllFluids.MOLTEN_LAPIS.getSource())
        ;
        prov.tag(MOLTEN_CRYSTALS)
                .add(AllFluids.MOLTEN_AMETHYST.getSource())
                .add(AllFluids.MOLTEN_PRISMARINE.getSource())
                .add(AllFluids.MOLTEN_QUARTZ.getSource())
        ;
        prov.tag(MOLTEN_DUSTS)
                .add(AllFluids.MOLTEN_REDSTONE.getSource())
                .add(AllFluids.MOLTEN_OBSIDIAN.getSource())
        ;
    }

    public static TagKey<Block> MELTER_HEATER = block("melter_heater");

    public static void genBlockTags(RegistrateTagsProvider<Block> provIn) {
        TagsProvider<Block> prov = new TagsProvider<>(provIn, Block::builtInRegistryHolder);
        prov.tag(MELTER_HEATER)
                .add(Blocks.LAVA)
                .add(Blocks.LAVA_CAULDRON)
                .addTag(BlockTags.FIRE)
                .addTag(BlockTags.CAMPFIRES)
        ;
    }
    public static TagKey<Item> COPPER_BLOCKS = item("copper_blocks");
    public static TagKey<Item> QUARTZ_BLOCKS = item("quartz_blocks");
    public static TagKey<Item> AMETHYST_BLOCKS = item("amethyst_blocks");
    public static TagKey<Item> COPPER_RAW_MATERIALS = item("copper_raw_materials");
    public static TagKey<Item> IRON_RAW_MATERIALS = item("iron_raw_materials");
    public static TagKey<Item> GOLD_RAW_MATERIALS = item("gold_raw_materials");

    public static TagKey<Item> CAST_AXE_HEADS = item("cast/axe_heads");
    public static TagKey<Item> CAST_PICKAXE_HEADS = item("cast/pickaxe_heads");
    public static TagKey<Item> CAST_SHOVEL_HEADS = item("cast/shovel_heads");
    public static TagKey<Item> CAST_HOE_HEADS = item("cast/hoe_heads");
    public static TagKey<Item> CAST_SWORD_BLADES = item("cast/sword_blades");
    public static TagKey<Item> CAST_GUARDS = item("cast/guards");

    public static TagKey<Item> CAST_INGOTS = item("cast/ingots");
    public static TagKey<Item> CAST_GEMS = item("cast/gems");
    public static TagKey<Item> CAST_BALLS = item("cast/balls");
    public static TagKey<Item> CAST_NUGGETS = item("cast/nuggets");
    public static TagKey<Item> CAST_DUSTS = item("cast/dusts");
    public static TagKey<Item> CAST_RODS = item("cast/rods");

    public static TagKey<Item> MOLDS_REUSABLE = item("molds/reusable");
    public static TagKey<Item> MOLDS_SINGLE = item("molds/single");

    public static TagKey<Item> BLACKLISTED_HOTBAR_ITEMS = item("blacklisted_hotbar_items");

    public static void genItemTags(RegistrateTagsProvider<Item> provIn) {
        TagsProvider<Item> prov = new TagsProvider<>(provIn, Item::builtInRegistryHolder);

        prov.tag(BLACKLISTED_HOTBAR_ITEMS).addOptional(SlagEmbers.loc("create", "wand_of_symmetry"));

        prov.tag(CAST_INGOTS).addTag(Tags.Items.INGOTS).addTag(Tags.Items.BRICKS);
        prov.tag(CAST_GEMS).add(Items.ECHO_SHARD).addTag(ItemTags.COALS).addTag(Tags.Items.GEMS).addTag(Tags.Items.NETHER_STARS);
        prov.tag(CAST_BALLS).add(Items.WIND_CHARGE).add(Items.FIRE_CHARGE).add(Items.FIREWORK_STAR).add(Items.ENDER_EYE).add(Items.CLAY_BALL).add(Items.SNOWBALL).add(Items.MAGMA_CREAM).add(Items.HEART_OF_THE_SEA).addTag(Tags.Items.SLIME_BALLS).addTag(Tags.Items.ENDER_PEARLS);
        prov.tag(CAST_NUGGETS).addTag(Tags.Items.NUGGETS);
        prov.tag(CAST_DUSTS).add(Items.BLAZE_POWDER).add(Items.SUGAR).add(Items.GUNPOWDER).addTag(Tags.Items.DUSTS);
        prov.tag(CAST_RODS).add(Items.END_ROD).add(Items.LIGHTNING_ROD).add(Items.BAMBOO).addTag(Tags.Items.RODS);

        prov.tag(COPPER_RAW_MATERIALS)
                .addTag(Tags.Items.ORES_COPPER)
                .addTag(Tags.Items.RAW_MATERIALS_COPPER);
        prov.tag(IRON_RAW_MATERIALS)
                .addTag(Tags.Items.ORES_IRON)
                .addTag(Tags.Items.RAW_MATERIALS_IRON);
        prov.tag(GOLD_RAW_MATERIALS)
                .addTag(Tags.Items.ORES_GOLD)
                .addTag(Tags.Items.RAW_MATERIALS_GOLD);
        prov.tag(AMETHYST_BLOCKS)
                .add(Items.AMETHYST_CLUSTER)
                .add(Items.BUDDING_AMETHYST)
                .add(Items.AMETHYST_BLOCK);
        prov.tag(QUARTZ_BLOCKS)
                .add(Items.CHISELED_QUARTZ_BLOCK)
                .add(Items.QUARTZ_PILLAR)
                .add(Items.QUARTZ_BRICKS)
                .add(Items.SMOOTH_QUARTZ)
                .add(Items.QUARTZ_BLOCK);
        prov.tag(COPPER_BLOCKS)
                .add(Items.EXPOSED_COPPER)
                .add(Items.WEATHERED_COPPER)
                .add(Items.OXIDIZED_COPPER)
                .add(Items.WAXED_COPPER_BLOCK)
                .add(Items.WAXED_EXPOSED_COPPER)
                .add(Items.WAXED_WEATHERED_COPPER)
                .add(Items.WAXED_OXIDIZED_COPPER)
                .addTag(Tags.Items.STORAGE_BLOCKS_COPPER);
    }

    public static TagKey<Block> block(String name) { return TagKey.create(Registries.BLOCK, SlagEmbers.loc(name)); }
    public static TagKey<Block> blockC(String name) { return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", name)); }
    public static TagKey<Block> blockMC(String name) { return TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace(name)); }
    public static TagKey<Item> item(String name) { return TagKey.create(Registries.ITEM, SlagEmbers.loc(name)); }
    public static TagKey<Item> itemC(String name) { return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name)); }
    public static TagKey<Item> itemMC(String name) { return TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace(name)); }
    public static TagKey<Fluid> fluid(String name) { return TagKey.create(Registries.FLUID, SlagEmbers.loc(name)); }
    public static TagKey<Fluid> fluidC(String name) { return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", name)); }
    public static TagKey<Fluid> fluidMC(String name) { return TagKey.create(Registries.FLUID, ResourceLocation.withDefaultNamespace(name)); }

    public static class TagsProvider<T> {

        private final RegistrateTagsProvider<T> provider;
        private final Function<T, ResourceKey<T>> keyExtractor;

        public TagsProvider(RegistrateTagsProvider<T> provider, Function<T, Holder.Reference<T>> refExtractor) {
            this.provider = provider;
            this.keyExtractor = refExtractor.andThen(Holder.Reference::key);
        }

        public TagAppender<T> tag(TagKey<T> tag) {
            TagBuilder tagbuilder = getOrCreateRawBuilder(tag);
            return new TagAppender<>(tagbuilder, keyExtractor);
        }

        public TagBuilder getOrCreateRawBuilder(TagKey<T> tag) {
            return provider.addTag(tag).getInternalBuilder();
        }

    }

    public static class TagAppender<T> extends net.minecraft.data.tags.TagsProvider.TagAppender<T> {

        private final Function<T, ResourceKey<T>> keyExtractor;

        public TagAppender(TagBuilder pBuilder, Function<T, ResourceKey<T>> pKeyExtractor) {
            super(pBuilder);
            this.keyExtractor = pKeyExtractor;
        }

        public TagAppender<T> add(T entry) {
            this.add(this.keyExtractor.apply(entry));
            return this;
        }

        @SafeVarargs
        public final TagAppender<T> add(T... entries) {
            Stream.of(entries)
                    .map(this.keyExtractor)
                    .forEach(this::add);
            return this;
        }

    }
}
