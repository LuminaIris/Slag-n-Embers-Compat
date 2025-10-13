package dev.lopyluna.slag.content.blocks.basin;

import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BasinCastingRecipe implements Recipe<BasinCastingRecipe.SingleRecipeFluidInput> {
    protected final RecipeType<?> type;
    protected final String group;

    private final FluidStack input;
    private final ItemStack output;

    public BasinCastingRecipe(String group, FluidStack inputs, ItemStack output) {
        this(AllRecipes.BASIN_CASTING.get(), group, inputs, output);
    }
    public BasinCastingRecipe(RecipeType<?> type, String group, FluidStack inputs, ItemStack output) {
        this.type = type;
        this.group = group;
        this.input = inputs;
        this.output = output;
    }

    public FluidStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    @Override
    public boolean matches(SingleRecipeFluidInput fluidInput, Level level) {
        return input.is(fluidInput.fluid);
    }

    public boolean hasEnoughFluid(FluidStack stack) {
        return input.is(stack.getFluid()) && stack.getAmount() >= input.getAmount();
    }

    @Override
    public @NotNull ItemStack assemble(SingleRecipeFluidInput singleRecipeFluidInput, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AllRecipes.BASIN_CASTING_SER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return type;
    }

    public static class Type implements RecipeType<BasinCastingRecipe> {
        private Type() {
        }
        public static final BasinCastingRecipe.Type INSTANCE = new BasinCastingRecipe.Type();
        @Override
        public String toString() {
            return "basin_casting";
        }
    }

    public interface Factory {
        BasinCastingRecipe create(String var1, FluidStack var2, ItemStack var3);
    }

    public record SingleRecipeFluidInput(Fluid fluid) implements RecipeInput {
        @Override
        public boolean isEmpty() {
            return fluid == null || fluid == Fluids.EMPTY;
        }
        @Override
        public int hashCode() {
            return fluid.hashCode();
        }
        @Override
        public @NotNull ItemStack getItem(int i) {
            return ItemStack.EMPTY;
        }
        public int size() {
            return 1;
        }
    }

}
