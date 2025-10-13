package dev.lopyluna.slag.content.jei;

import dev.lopyluna.slag.content.blocks.forge.DoubleSmeltingRecipe;
import dev.lopyluna.slag.content.blocks.melter.MeltingRecipe;
import dev.lopyluna.slag.register.AllRecipes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.function.Supplier;

public class EmbersRecipesJEI {
    public static final Supplier<RecipeType<RecipeHolder<DoubleSmeltingRecipe>>> DOUBLE_SMELTING = RecipeType.createFromDeferredVanilla(AllRecipes.DOUBLE_SMELTING::get);
    public static final Supplier<RecipeType<RecipeHolder<MeltingRecipe>>> MELTING = RecipeType.createFromDeferredVanilla(AllRecipes.MELTING::get);

    public static void register() {}
}
