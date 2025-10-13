package dev.lopyluna.slag.content.blocks.table;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lopyluna.slag.content.AllUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TableCastingRecipeSer implements RecipeSerializer<TableCastingRecipe> {
    private final TableCastingRecipe.Factory factory;
    private final MapCodec<TableCastingRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, TableCastingRecipe> streamCodec;

    public TableCastingRecipeSer(TableCastingRecipe.Factory factory) {
        this.factory = factory;
        this.codec = RecordCodecBuilder.mapCodec((instance) -> {
            var recipe = instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::getGroup),
                    TagKey.codec(Registries.ITEM).fieldOf("cast").forGetter(TableCastingRecipe::getCastType),
                    FluidStack.CODEC.fieldOf("ingredient").forGetter(TableCastingRecipe::getInput),
                    ItemStack.CODEC.fieldOf("result").forGetter(TableCastingRecipe::getOutput));
            Objects.requireNonNull(factory);
            return recipe.apply(instance, factory::create);
        });
        this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
    }
    @Override public @NotNull MapCodec<TableCastingRecipe> codec() {
        return codec;
    }
    @Override public @NotNull StreamCodec<RegistryFriendlyByteBuf, TableCastingRecipe> streamCodec() {
        return streamCodec;
    }
    @Override public String toString() {
        return "table_casting";
    }


    private TableCastingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        String s = buffer.readUtf();
        FluidStack input = FluidStack.STREAM_CODEC.decode(buffer);
        TagKey<Item> type = AllUtils.tagKeyStreamCodec(Registries.ITEM).decode(buffer);
        ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
        return this.factory.create(s, type, input, output);
    }

    private void toNetwork(RegistryFriendlyByteBuf buffer, TableCastingRecipe recipe) {
        buffer.writeUtf(recipe.getGroup());
        FluidStack.STREAM_CODEC.encode(buffer, recipe.getInput());
        AllUtils.tagKeyStreamCodec(Registries.ITEM).encode(buffer, recipe.getCastType());
        ItemStack.STREAM_CODEC.encode(buffer, recipe.getOutput());
    }

    public TableCastingRecipe create(String group, TagKey<Item> type, FluidStack input, ItemStack result) {
        return this.factory.create(group, type, input, result);
    }
}
