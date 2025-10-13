package dev.lopyluna.slag.content.datagen;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.content.blocks.melter.MeltingRecipe;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static com.tterrag.registrate.providers.RegistrateRecipeProvider.has;

@ParametersAreNonnullByDefault
public class MeltingRecipeBuilder implements RecipeBuilder {
    private final Fluid result;
    private final FluidStack stackResult;
    private final Ingredient input;
    private final Map<String, Criterion<?>> criteria;
    @Nullable
    private String group;
    private final MeltingRecipe.Factory factory;

    private MeltingRecipeBuilder(Fluid result, int count, Ingredient input) {
        this(new FluidStack(result, count), input);
    }
    private MeltingRecipeBuilder(FluidStack result, Ingredient input) {
        this.criteria = new LinkedHashMap<>();
        this.result = result.getFluid();
        this.stackResult = result;
        this.input = input;
        this.factory = MeltingRecipe::new;
    }

    @Override
    public @NotNull RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull RecipeBuilder group(@Nullable String name) {
        this.group = name;
        return this;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation loc) {
        if (this.criteria.isEmpty()) throw new IllegalStateException("No way of obtaining recipe " + loc);
        var builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(loc))
                .rewards(AdvancementRewards.Builder.recipe(loc)).requirements(AdvancementRequirements.Strategy.OR);
        Objects.requireNonNull(builder);
        this.criteria.forEach(builder::addCriterion);

        MeltingRecipe recipe = this.factory.create(Objects.requireNonNullElse(this.group, ""), this.input, this.stackResult);
        recipeOutput.accept(loc, recipe, builder.build(loc.withPrefix("recipes/misc/")));

    }

    @Override
    public @NotNull Item getResult() {
        return Items.AIR;
    }
    public Fluid getResultFluid() {
        return result;
    }

    @Override
    public void save(RecipeOutput recipeOutput) {
        this.save(recipeOutput, getDefaultRecipeId(this.getResultFluid()));
    }
    @Override
    public void save(RecipeOutput recipeOutput, String id) {
        var loc = getDefaultRecipeId(this.getResultFluid());
        var parse = ResourceLocation.parse(id);
        if (parse.equals(loc)) throw new IllegalStateException("Recipe " + id + " should remove its 'save' argument as it is equal to default one");
        else this.save(recipeOutput, parse);
    }

    static ResourceLocation getDefaultRecipeId(Fluid fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid);
    }

    public static MeltingRecipeBuilder create(Fluid result, int mb, Ingredient ingredient) {
        return new MeltingRecipeBuilder(result, mb, ingredient);
    }
    public static MeltingRecipeBuilder create(FluidStack result, Ingredient ingredient) {
        return new MeltingRecipeBuilder(result, ingredient);
    }

    public static MeltingRecipeBuilder create(Fluid result, int mb, TagKey<Item> tag) {
        return new MeltingRecipeBuilder(result, mb, Ingredient.of(tag));
    }
    public static MeltingRecipeBuilder create(FluidStack result, TagKey<Item> tag) {
        return new MeltingRecipeBuilder(result, Ingredient.of(tag));
    }

    public static MeltingRecipeBuilder create(Fluid result, int mb, ItemLike item) {
        return new MeltingRecipeBuilder(result, mb, Ingredient.of(item));
    }
    public static MeltingRecipeBuilder create(FluidStack result, ItemLike item) {
        return new MeltingRecipeBuilder(result, Ingredient.of(item));
    }

    public static void oreMeltableGem(RegistrateRecipeProvider p, String name, Fluid fluid, @Nullable TagKey<Item> blocks, @Nullable TagKey<Item> material, @Nullable TagKey<Item> nuggets) {
        if (blocks != null) MeltingRecipeBuilder.create(fluid, MelterBE.BLOCK_SIZE + MelterBE.INGOT_SIZE + MelterBE.INGOT_SIZE, blocks)
                .unlockedBy("has_meltable_" + name, has(blocks))
                .save(p, SlagEmbers.loc("melting/" + name + "_blocks"));
        if (material != null) MeltingRecipeBuilder.create(fluid, MelterBE.INGOT_SIZE + MelterBE.SHARD_SIZE + MelterBE.SHARD_SIZE, material)
                .unlockedBy("has_meltable_" + name, has(material))
                .save(p, SlagEmbers.loc("melting/" + name + "_materials"));
        if (nuggets != null) MeltingRecipeBuilder.create(fluid, MelterBE.SHARD_SIZE, nuggets)
                .unlockedBy("has_meltable_" + name, has(nuggets))
                .save(p, SlagEmbers.loc("melting/" + name + "_nuggets"));
    }

    public static void oreMeltable(RegistrateRecipeProvider p, String name, Fluid fluid, @Nullable TagKey<Item> blocks, @Nullable TagKey<Item> material, @Nullable TagKey<Item> nuggets) {
        if (blocks != null) MeltingRecipeBuilder.create(fluid, MelterBE.BLOCK_SIZE + MelterBE.INGOT_SIZE + MelterBE.INGOT_SIZE + MelterBE.INGOT_SIZE, blocks)
                .unlockedBy("has_meltable_" + name, has(blocks))
                .save(p, SlagEmbers.loc("melting/" + name + "_blocks"));
        if (material != null) MeltingRecipeBuilder.create(fluid, MelterBE.INGOT_SIZE + MelterBE.NUGGET_SIZE + MelterBE.NUGGET_SIZE + MelterBE.NUGGET_SIZE, material)
                .unlockedBy("has_meltable_" + name, has(material))
                .save(p, SlagEmbers.loc("melting/" + name + "_materials"));
        if (nuggets != null) MeltingRecipeBuilder.create(fluid, MelterBE.NUGGET_SIZE, nuggets)
                .unlockedBy("has_meltable_" + name, has(nuggets))
                .save(p, SlagEmbers.loc("melting/" + name + "_nuggets"));
    }

    public static void ingotMeltable(RegistrateRecipeProvider p, String name, Fluid fluid, @Nullable TagKey<Item> blocks, @Nullable TagKey<Item> ingots, @Nullable TagKey<Item> nuggets) {
        if (blocks != null) MeltingRecipeBuilder.create(fluid, MelterBE.BLOCK_SIZE, blocks)
                .unlockedBy("has_meltable_" + name, has(blocks))
                .save(p, SlagEmbers.loc("melting/" + name + "_blocks"));
        if (ingots != null) MeltingRecipeBuilder.create(fluid, MelterBE.INGOT_SIZE, ingots)
                .unlockedBy("has_meltable_" + name, has(ingots))
                .save(p, SlagEmbers.loc("melting/" + name + "_ingots"));
        if (nuggets != null) MeltingRecipeBuilder.create(fluid, MelterBE.NUGGET_SIZE, nuggets)
                .unlockedBy("has_meltable_" + name, has(nuggets))
                .save(p, SlagEmbers.loc("melting/" + name + "_nuggets"));
    }
    public static void crystalMeltable(RegistrateRecipeProvider p, String name, Fluid fluid, @Nullable TagKey<Item> blocks, @Nullable TagKey<Item> crystals) {
        if (blocks != null) MeltingRecipeBuilder.create(fluid, MelterBE.SMALL_BLOCK_SIZE, blocks)
                .unlockedBy("has_meltable_" + name, has(blocks))
                .save(p, SlagEmbers.loc("melting/" + name + "_blocks"));
        if (crystals != null) MeltingRecipeBuilder.create(fluid, MelterBE.INGOT_SIZE, crystals)
                .unlockedBy("has_meltable_" + name, has(crystals))
                .save(p, SlagEmbers.loc("melting/" + name + "_crystals"));
    }
    public static void gemMeltable(RegistrateRecipeProvider p, String name, Fluid fluid, @Nullable TagKey<Item> blocks, @Nullable TagKey<Item> gems, @Nullable TagKey<Item> shards) {
        if (blocks != null) MeltingRecipeBuilder.create(fluid, MelterBE.BLOCK_SIZE, blocks)
                .unlockedBy("has_meltable_" + name, has(blocks))
                .save(p, SlagEmbers.loc("melting/" + name + "_blocks"));
        if (gems != null) MeltingRecipeBuilder.create(fluid, MelterBE.INGOT_SIZE, gems)
                .unlockedBy("has_meltable_" + name, has(gems))
                .save(p, SlagEmbers.loc("melting/" + name + "_gems"));
        if (shards != null) MeltingRecipeBuilder.create(fluid, MelterBE.SHARD_SIZE, shards)
                .unlockedBy("has_meltable_" + name, has(shards))
                .save(p, SlagEmbers.loc("melting/" + name + "_shards"));
    }
    public static void dustMeltable(RegistrateRecipeProvider p, String name, Fluid fluid, @Nullable TagKey<Item> blocks, @Nullable TagKey<Item> dusts) {
        if (blocks != null) MeltingRecipeBuilder.create(fluid, MelterBE.BLOCK_SIZE, blocks)
                .unlockedBy("has_meltable_" + name, has(blocks))
                .save(p, SlagEmbers.loc("melting/" + name + "_blocks"));
        if (dusts != null) MeltingRecipeBuilder.create(fluid, MelterBE.INGOT_SIZE, dusts)
                .unlockedBy("has_meltable_" + name, has(dusts))
                .save(p, SlagEmbers.loc("melting/" + name + "_dusts"));
    }

    public static FluidStack fluid(Fluid fluid, int mb) {
        return new FluidStack(fluid, mb);
    }
    public static List<FluidStack> fluids(FluidStack... fluids) {
        return new ArrayList<>(List.of(fluids));
    }
}
