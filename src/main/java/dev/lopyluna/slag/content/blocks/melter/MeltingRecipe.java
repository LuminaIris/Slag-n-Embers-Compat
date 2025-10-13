package dev.lopyluna.slag.content.blocks.melter;

import dev.lopyluna.slag.content.blocks.crucible.AlloyingRecipe;
import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MeltingRecipe implements Recipe<SingleRecipeInput> {
    protected final RecipeType<?> type;
    protected final String group;

    private final Ingredient input;
    private final FluidStack output;

    public MeltingRecipe(String group, Ingredient input, FluidStack output) {
        this(AllRecipes.MELTING.get(), group, input, output);
    }
    public MeltingRecipe(RecipeType<?> type, String group, Ingredient input, FluidStack output) {
        this.type = type;
        this.group = group;
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    public FluidStack assembleWithFluid(AlloyingRecipe.AlloyRecipeInput alloyRecipeInput, HolderLookup.Provider provider) {
        return output.copy();
    }
    public FluidStack getResultFluid(HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override public @NotNull String getGroup() {
        return group;
    }
    public Ingredient getInput() {
        return input;
    }
    public FluidStack getOutput() {
        return output;
    }
    @Override public boolean canCraftInDimensions(int i, int i1) {
        return false;
    }
    @Override public @NotNull ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }
    @Override public @NotNull ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override public @NotNull RecipeSerializer<?> getSerializer() {
        return AllRecipes.MELTING_SER.get();
    }
    @Override public @NotNull RecipeType<?> getType() {
        return type;
    }

    public static class Type implements RecipeType<MeltingRecipe> {
        private Type() {}
        public static final MeltingRecipe.Type INSTANCE = new MeltingRecipe.Type();
        @Override public String toString() {
            return "melting";
        }
    }

    public interface Factory {
        MeltingRecipe create(String var1, Ingredient var2, FluidStack var3);
    }
}
