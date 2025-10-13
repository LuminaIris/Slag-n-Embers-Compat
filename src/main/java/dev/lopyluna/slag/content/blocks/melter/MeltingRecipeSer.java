package dev.lopyluna.slag.content.blocks.melter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MeltingRecipeSer implements RecipeSerializer<MeltingRecipe> {
    private final MeltingRecipe.Factory factory;
    private final MapCodec<MeltingRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, MeltingRecipe> streamCodec;

    public MeltingRecipeSer(MeltingRecipe.Factory factory) {
        this.factory = factory;
        this.codec = RecordCodecBuilder.mapCodec((instance) -> {
            var recipe = instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::getGroup),
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(MeltingRecipe::getInput),
                    FluidStack.CODEC.fieldOf("result").forGetter(MeltingRecipe::getOutput));
            Objects.requireNonNull(factory);
            return recipe.apply(instance, factory::create);
        });
        this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
    }
    @Override public @NotNull MapCodec<MeltingRecipe> codec() {
        return codec;
    }
    @Override public @NotNull StreamCodec<RegistryFriendlyByteBuf, MeltingRecipe> streamCodec() {
        return streamCodec;
    }
    @Override public String toString() {
        return "melting";
    }


    private MeltingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        String s = buffer.readUtf();
        Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        FluidStack fluidStack = FluidStack.STREAM_CODEC.decode(buffer);
        return this.factory.create(s, input, fluidStack);
    }

    private void toNetwork(RegistryFriendlyByteBuf buffer, MeltingRecipe recipe) {
        buffer.writeUtf(recipe.getGroup());
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getInput());
        FluidStack.STREAM_CODEC.encode(buffer, recipe.getOutput());
    }

    public MeltingRecipe create(String group, Ingredient input, FluidStack result) {
        return this.factory.create(group, input, result);
    }
}
