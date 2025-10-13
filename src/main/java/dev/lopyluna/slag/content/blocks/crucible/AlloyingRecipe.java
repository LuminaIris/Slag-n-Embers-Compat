package dev.lopyluna.slag.content.blocks.crucible;

import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class AlloyingRecipe implements Recipe<AlloyingRecipe.AlloyRecipeInput> {

    protected final RecipeType<?> type;
    protected final String group;

    private final List<FluidStack> inputs;
    private final FluidStack output;

    public AlloyingRecipe(String group, List<FluidStack> inputs, FluidStack output) {
        this(AllRecipes.ALLOYING.get(), group, inputs, output);
    }
    public AlloyingRecipe(RecipeType<?> type, String group, List<FluidStack> inputs, FluidStack output) {
        this.type = type;
        this.group = group;
        this.inputs = inputs;
        this.output = output;
    }

    @Override
    public boolean matches(AlloyRecipeInput alloyRecipeInput, Level level) {
        var inputs = alloyRecipeInput.fluids;
        if (inputs.size() != this.inputs.size()) return false;
        for (var input : inputs) if (!inputs.contains(input)) return false;
        return true;
    }

    public FluidStack assembleWithFluid(AlloyRecipeInput alloyRecipeInput, HolderLookup.Provider provider) {
        return output.copy();
    }
    public FluidStack getResultFluid(HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public @NotNull String getGroup() {
        return group;
    }
    public List<FluidStack> getInputs() {
        return inputs;
    }
    public FluidStack getOutput() {
        return output;
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return false;
    }
    @Override
    public @NotNull ItemStack assemble(AlloyRecipeInput alloyRecipeInput, HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }
    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AllRecipes.ALLOYING_SER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return type;
    }

    public static class Type implements RecipeType<AlloyingRecipe> {
        private Type() {
        }
        public static final AlloyingRecipe.Type INSTANCE = new AlloyingRecipe.Type();
        @Override
        public String toString() {
            return "alloying";
        }
    }

    public interface Factory {
        AlloyingRecipe create(String var1, List<FluidStack> var2, FluidStack var3);
    }

    public record AlloyRecipeInput(List<FluidStack> fluids) implements RecipeInput {
        @Override
        public @NotNull ItemStack getItem(int i) {
            return ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return fluids.size();
        }

        @Override
        public boolean isEmpty() {
            return fluids.isEmpty();
        }

        @Override
        public int hashCode() {
            return hashStackList(fluids);
        }

        public static int hashStackList(List<FluidStack> list) {
            int i = 0;
            for(var f : list) i = i * 31 + FluidStack.hashFluidAndComponents(f);
            return i;
        }
    }
}
