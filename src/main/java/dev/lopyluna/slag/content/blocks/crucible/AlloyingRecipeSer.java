package dev.lopyluna.slag.content.blocks.crucible;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class AlloyingRecipeSer implements RecipeSerializer<AlloyingRecipe> {
    private final AlloyingRecipe.Factory factory;
    private final MapCodec<AlloyingRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> streamCodec;

    public AlloyingRecipeSer(AlloyingRecipe.Factory factory) {
        this.factory = factory;
        this.codec = RecordCodecBuilder.mapCodec((instance) -> {
            var recipe = instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::getGroup),
                    FluidStack.CODEC.listOf().fieldOf("ingredients").forGetter(AlloyingRecipe::getInputs),
                    FluidStack.CODEC.fieldOf("result").forGetter(AlloyingRecipe::getOutput));
            Objects.requireNonNull(factory);
            return recipe.apply(instance, factory::create);
        });
        this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
    }

    @Override
    public @NotNull MapCodec<AlloyingRecipe> codec() {
        return this.codec;
    }
    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> streamCodec() {
        return this.streamCodec;
    }

    @Override
    public String toString() {
        return "alloying";
    }

    private AlloyingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        String s = buffer.readUtf();
        List<FluidStack> inputs = FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
        FluidStack fluidStack = FluidStack.STREAM_CODEC.decode(buffer);
        return this.factory.create(s, inputs, fluidStack);
    }

    private void toNetwork(RegistryFriendlyByteBuf buffer, AlloyingRecipe recipe) {
        buffer.writeUtf(recipe.getGroup());
        FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.getInputs());
        FluidStack.STREAM_CODEC.encode(buffer, recipe.getOutput());
    }

    public AlloyingRecipe create(String group, List<FluidStack> inputs, FluidStack result) {
        return this.factory.create(group, inputs, result);
    }
}
