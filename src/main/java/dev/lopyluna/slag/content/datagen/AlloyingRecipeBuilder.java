package dev.lopyluna.slag.content.datagen;

import dev.lopyluna.slag.content.blocks.crucible.AlloyingRecipe;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public class AlloyingRecipeBuilder implements RecipeBuilder {
    private final Fluid result;
    private final FluidStack stackResult;
    private final List<FluidStack> inputs;
    private final Map<String, Criterion<?>> criteria;
    @Nullable
    private String group;
    private final AlloyingRecipe.Factory factory;

    private AlloyingRecipeBuilder(Fluid result, int count, List<FluidStack> inputs) {
        this(new FluidStack(result, count), inputs);
    }
    private AlloyingRecipeBuilder(FluidStack result, List<FluidStack> inputs) {
        this.criteria = new LinkedHashMap<>();
        this.result = result.getFluid();
        this.stackResult = result;
        this.inputs = inputs;
        this.factory = AlloyingRecipe::new;
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

        AlloyingRecipe recipe = this.factory.create(Objects.requireNonNullElse(this.group, ""), this.inputs, this.stackResult);
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

    public static AlloyingRecipeBuilder create(Fluid result, int mb, List<FluidStack> fluids) {
        return new AlloyingRecipeBuilder(result, mb, fluids);
    }
    public static AlloyingRecipeBuilder create(FluidStack result, List<FluidStack> fluids) {
        return new AlloyingRecipeBuilder(result, fluids);
    }

    public static AlloyingRecipeBuilder create(Fluid result, int mb, FluidStack... fluids) {
        return new AlloyingRecipeBuilder(result, mb, fluids(fluids));
    }
    public static AlloyingRecipeBuilder create(FluidStack result, FluidStack... fluids) {
        return new AlloyingRecipeBuilder(result, fluids(fluids));
    }

    public static FluidStack fluid(Fluid fluid, int mb) {
        return new FluidStack(fluid, mb);
    }
    public static List<FluidStack> fluids(FluidStack... fluids) {
        return new ArrayList<>(List.of(fluids));
    }
}
