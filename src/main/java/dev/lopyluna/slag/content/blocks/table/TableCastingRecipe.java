package dev.lopyluna.slag.content.blocks.table;

import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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
public class TableCastingRecipe implements Recipe<TableCastingRecipe.SingleRecipeFluidInput> {
    protected final RecipeType<?> type;
    protected final String group;

    private final TagKey<Item> castType;
    private final FluidStack input;
    private final ItemStack output;

    public TableCastingRecipe(String group, TagKey<Item> castType, FluidStack inputs, ItemStack output) {
        this(AllRecipes.TABLE_CASTING.get(), group, castType, inputs, output);
    }
    public TableCastingRecipe(RecipeType<?> type, String group, TagKey<Item> castType, FluidStack inputs, ItemStack output) {
        this.type = type;
        this.group = group;
        this.castType = castType;
        this.input = inputs;
        this.output = output;
    }

    public TagKey<Item> getCastType() {
        return castType;
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
        return AllRecipes.TABLE_CASTING_SER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return type;
    }

    public static class Type implements RecipeType<TableCastingRecipe> {
        private Type() {
        }
        public static final Type INSTANCE = new Type();
        @Override
        public String toString() {
            return "table_casting";
        }
    }

    public interface Factory {
        TableCastingRecipe create(String var1, TagKey<Item> var2, FluidStack var3, ItemStack var4);
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
