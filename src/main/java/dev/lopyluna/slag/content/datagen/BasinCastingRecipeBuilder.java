package dev.lopyluna.slag.content.datagen;

import dev.lopyluna.slag.content.blocks.basin.BasinCastingRecipe;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class BasinCastingRecipeBuilder implements RecipeBuilder {
    private final Item result;
    private final ItemStack stackResult;
    private final FluidStack input;
    private final Map<String, Criterion<?>> criteria;
    @Nullable
    private String group;
    private final BasinCastingRecipe.Factory factory;

    private BasinCastingRecipeBuilder(ItemStack result, FluidStack input) {
        this.criteria = new LinkedHashMap<>();
        this.result = result.getItem();
        this.stackResult = result;
        this.input = input;
        this.factory = BasinCastingRecipe::new;
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

        BasinCastingRecipe recipe = this.factory.create(Objects.requireNonNullElse(this.group, ""), this.input, this.stackResult);
        recipeOutput.accept(loc, recipe, builder.build(loc.withPrefix("recipes/misc/")));

    }

    @Override
    public @NotNull Item getResult() {
        return result;
    }

    @Override
    public void save(RecipeOutput recipeOutput) {
        this.save(recipeOutput, getDefaultRecipeId(this.getResult()));
    }
    @Override
    public void save(RecipeOutput recipeOutput, String id) {
        var loc = getDefaultRecipeId(this.getResult());
        var parse = ResourceLocation.parse(id);
        if (parse.equals(loc)) throw new IllegalStateException("Recipe " + id + " should remove its 'save' argument as it is equal to default one");
        else this.save(recipeOutput, parse);
    }

    static ResourceLocation getDefaultRecipeId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static BasinCastingRecipeBuilder create(Item result, int count, Fluid fluid, int mb) {
        return new BasinCastingRecipeBuilder(item(result, count), fluid(fluid, mb));
    }
    public static BasinCastingRecipeBuilder create(Item result, int count, FluidStack fluid) {
        return new BasinCastingRecipeBuilder(item(result, count), fluid);
    }
    public static BasinCastingRecipeBuilder create(ItemStack result, Fluid fluid, int mb) {
        return new BasinCastingRecipeBuilder(result, fluid(fluid, mb));
    }
    public static BasinCastingRecipeBuilder create(ItemStack result, FluidStack fluid) {
        return new BasinCastingRecipeBuilder(result, fluid);
    }

    public static FluidStack fluid(Fluid fluid, int mb) {
        return new FluidStack(fluid, mb);
    }
    public static ItemStack item(Item item, int count) {
        return new ItemStack(item, count);
    }
}
