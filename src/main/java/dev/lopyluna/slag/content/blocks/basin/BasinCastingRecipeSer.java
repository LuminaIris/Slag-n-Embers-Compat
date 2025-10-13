package dev.lopyluna.slag.content.blocks.basin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BasinCastingRecipeSer implements RecipeSerializer<BasinCastingRecipe> {
    private final BasinCastingRecipe.Factory factory;
    private final MapCodec<BasinCastingRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, BasinCastingRecipe> streamCodec;

    public BasinCastingRecipeSer(BasinCastingRecipe.Factory factory) {
        this.factory = factory;
        this.codec = RecordCodecBuilder.mapCodec((instance) -> {
            var recipe = instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::getGroup),
                    FluidStack.CODEC.fieldOf("ingredient").forGetter(BasinCastingRecipe::getInput),
                    ItemStack.CODEC.fieldOf("result").forGetter(BasinCastingRecipe::getOutput));
            Objects.requireNonNull(factory);
            return recipe.apply(instance, factory::create);
        });
        this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
    }
    @Override public @NotNull MapCodec<BasinCastingRecipe> codec() {
        return codec;
    }
    @Override public @NotNull StreamCodec<RegistryFriendlyByteBuf, BasinCastingRecipe> streamCodec() {
        return streamCodec;
    }
    @Override public String toString() {
        return "basin_casting";
    }


    private BasinCastingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        String s = buffer.readUtf();
        FluidStack input = FluidStack.STREAM_CODEC.decode(buffer);
        ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
        return this.factory.create(s, input, output);
    }

    private void toNetwork(RegistryFriendlyByteBuf buffer, BasinCastingRecipe recipe) {
        buffer.writeUtf(recipe.getGroup());
        FluidStack.STREAM_CODEC.encode(buffer, recipe.getInput());
        ItemStack.STREAM_CODEC.encode(buffer, recipe.getOutput());
    }

    public BasinCastingRecipe create(String group, FluidStack input, ItemStack result) {
        return this.factory.create(group, input, result);
    }
}
