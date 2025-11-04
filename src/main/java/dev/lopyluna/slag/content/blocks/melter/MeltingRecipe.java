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
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class MeltingRecipe implements Recipe<SingleRecipeInput> {
    protected final RecipeType<?> type;
    protected final String group;

    private final List<ItemStack> inputs;
    private final Ingredient input;
    private final List<FluidStack> outputs;

    public MeltingRecipe(String group, List<ItemStack> inputs, Ingredient input, List<FluidStack> outputs) {
        this(AllRecipes.MELTING.get(), group, inputs, input, outputs);
    }
    public MeltingRecipe(RecipeType<?> type, String group, List<ItemStack> inputs, Ingredient input, List<FluidStack> outputs) {
        this.type = type;
        this.group = group;
        this.inputs = inputs;
        this.input = input;
        this.outputs = outputs;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        if (input.item().isEmpty()) return false;
        if (!inputs.isEmpty()) for (var stack : inputs) {
            if (stack.isEmpty()) continue;
            if (ItemStack.isSameItemSameComponents(stack, input.item())) return true;
        }
        return this.input.test(input.item());
    }

    public List<FluidStack> assembleWithFluid(AlloyingRecipe.AlloyRecipeInput alloyRecipeInput, HolderLookup.Provider provider) {
        return getResultFluids(provider);
    }
    public List<FluidStack> getResultFluids(HolderLookup.Provider provider) {
        var list = new ArrayList<FluidStack>();
        var i = 0;
        for (var stack : outputs) {
            if (i > 12) break;
            list.add(stack.copy());
            i++;
        }
        return list;
    }

    @Override public @NotNull String getGroup() {
        return group;
    }
    public List<ItemStack> getInputs() {
        return inputs;
    }
    public Ingredient getInput() {
        return input;
    }
    public List<FluidStack> getOutputs() {
        return outputs;
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
        MeltingRecipe create(String var1, List<ItemStack> var2, Ingredient var3, List<FluidStack> var4);
    }
}
