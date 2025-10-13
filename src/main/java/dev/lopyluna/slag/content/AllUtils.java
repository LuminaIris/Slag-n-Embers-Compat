package dev.lopyluna.slag.content;

import com.mojang.datafixers.util.Function14;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.modular_tool.BakedModularToolItem;
import dev.lopyluna.slag.register.AllTags;
import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

import static com.tterrag.registrate.providers.RegistrateRecipeProvider.has;
import static dev.lopyluna.slag.SlagEmbers.REG;

@SuppressWarnings("unused")
public class AllUtils {
    public static <T extends BlockEntity> BlockEntityEntry<T> simpleBE(String name, BlockEntry<?> entry, BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return REG.blockEntity(name, factory).validBlock(entry).register();
    }
    public static <T extends BlockEntity> BlockEntityEntry<T> simpleBE(String name, BlockEntry<?> entry, NonNullFunction<BlockEntityRendererProvider.Context, BlockEntityRenderer<? super T>> renderer, BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return REG.blockEntity(name, factory).renderer(() -> renderer).validBlock(entry).register();
    }

    public static <T> StreamCodec<ByteBuf, TagKey<T>> tagKeyStreamCodec(ResourceKey<? extends Registry<T>> registry) {
        return ResourceLocation.STREAM_CODEC.map((loc) -> TagKey.create(registry, loc), TagKey::location);
    }

    public static boolean tagPresentInHotbar(Player player, TagKey<Item> tag) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < Inventory.getSelectionSize(); i++) if (inv.getItem(i).is(tag)) return true;
        return false;
    }

    public static List<ItemStack> getStacksFromTag(TagKey<Block> tag) {
        var opt = BuiltInRegistries.BLOCK.getTag(tag);
        if (opt.isEmpty()) return List.of();

        var out = new ArrayList<ItemStack>();
        for (var holder : opt.get()) {
            var stack = getStackFromBlock(holder.value(), true);
            if (!stack.isEmpty()) out.add(stack);
        }
        return out;
    }

    public static ItemStack getStackFromBlock(Block block, boolean requireHeater) {
        var state = block.defaultBlockState();
        var fState = state.getFluidState();
        if (!state.is(AllTags.MELTER_HEATER) && requireHeater) return ItemStack.EMPTY;
        ItemStack stack;
        if (state.getBlock() instanceof BaseFireBlock) {
            stack = Items.BLAZE_POWDER.getDefaultInstance();
        } else if (!fState.isEmpty()) {
            var type = fState.getType();
            stack = type.getBucket().getDefaultInstance();
        } else stack = state.getBlock().asItem().getDefaultInstance();
        stack.set(DataComponents.ITEM_NAME, block.getName());
        if (stack.isEmpty()) return stack.transmuteCopy(Items.BARRIER);
        return stack;
    }

    public static void compressible9x(DataGenContext<Item, Item> c, RegistrateRecipeProvider p, Ingredient decompact, Ingredient compact, ItemLike decompactResult, ItemLike compactResult) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, compactResult, 1)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', decompact)
                .unlockedBy("has_decompact", has(decompactResult))
                .save(p, SlagEmbers.loc("crafting/ingot_to_block_" + c.getName()));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, decompactResult, 9)
                .requires(compact)
                .unlockedBy("has_compact", has(compactResult))
                .save(p, SlagEmbers.loc("crafting/ingot_from_block_" + c.getName()));
    }

    public static ItemPredicate ingredientToPredicate(Ingredient ing) {
        var items = Arrays.stream(ing.getItems()).map(ItemStack::getItem).distinct().toArray(ItemLike[]::new);
        if (items.length == 0) return ItemPredicate.Builder.item().of(Items.AIR).build();
        return ItemPredicate.Builder.item().of(items).build();
    }

    private static Optional<Registry<Item>> itemRegistry() {
        var srv = ServerLifecycleHooks.getCurrentServer();
        if (srv != null) return Optional.of(srv.registryAccess().registryOrThrow(Registries.ITEM));
        var mc = Minecraft.getInstance();
        if (mc.level != null) return Optional.of(mc.level.registryAccess().registryOrThrow(Registries.ITEM));
        return Optional.empty();
    }

    private static boolean eqOrSuperset(TagKey<Item> tag, TagKey<Item> child) {
        if (tag.equals(child)) return true;
        var regOpt = itemRegistry();
        if (regOpt.isEmpty()) return false;
        var reg = regOpt.get();

        var parentSet = reg.getTag(tag).orElse(null);
        var childSet  = reg.getTag(child).orElse(null);
        if (parentSet == null || childSet == null) return false;

        HashSet<Holder<Item>> parent = new HashSet<>();
        parentSet.forEach(parent::add);
        for (Holder<Item> h : childSet) if (!parent.contains(h)) return false;
        return true;
    }

    public static Boolean matchesToolTag(BakedModularToolItem toolItem, ItemStack self, TagKey<Item> tag) {
        Boolean flag = null;
        for (var part : toolItem.getToolParts(self)) {
            switch (part.getPartSegment().getPath()) {
                case "pickaxe_head" -> flag = eqOrSuperset(tag, ItemTags.PICKAXES) ? true : null;
                case "axe_head"     -> flag = eqOrSuperset(tag, ItemTags.AXES) ? true : null;
                case "shovel_head"  -> flag = eqOrSuperset(tag, ItemTags.SHOVELS) ? true : null;
                case "hoe_head"     -> flag = eqOrSuperset(tag, ItemTags.HOES) ? true : null;
                case "sword_blade"  -> flag = eqOrSuperset(tag, ItemTags.SWORDS) ? true : null;
            }
            if (flag != null) break;
        }
        return flag;
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> StreamCodec<B, C> streamComposite(
            final StreamCodec<? super B, T1> codec1, final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2, final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3, final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4, final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5, final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6, final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7, final Function<C, T7> getter7,
            final StreamCodec<? super B, T8> codec8, final Function<C, T8> getter8,
            final StreamCodec<? super B, T9> codec9, final Function<C, T9> getter9,
            final StreamCodec<? super B, T10> codec10, final Function<C, T10> getter10,
            final StreamCodec<? super B, T11> codec11, final Function<C, T11> getter11,
            final StreamCodec<? super B, T12> codec12, final Function<C, T12> getter12,
            final StreamCodec<? super B, T13> codec13, final Function<C, T13> getter13,
            final StreamCodec<? super B, T14> codec14, final Function<C, T14> getter14,
            final Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, C> factory
    ) {
        return new StreamCodec<>() {
            @Override
            public @NotNull C decode(@NotNull B b) {
                T1 t1 = codec1.decode(b);
                T2 t2 = codec2.decode(b);
                T3 t3 = codec3.decode(b);
                T4 t4 = codec4.decode(b);
                T5 t5 = codec5.decode(b);
                T6 t6 = codec6.decode(b);
                T7 t7 = codec7.decode(b);
                T8 t8 = codec8.decode(b);
                T9 t9 = codec9.decode(b);
                T10 t10 = codec10.decode(b);
                T11 t11 = codec11.decode(b);
                T12 t12 = codec12.decode(b);
                T13 t13 = codec13.decode(b);
                T14 t14 = codec14.decode(b);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14);
            }

            @Override
            public void encode(@NotNull B b, @NotNull C p_331912_) {
                codec1.encode(b, getter1.apply(p_331912_));
                codec2.encode(b, getter2.apply(p_331912_));
                codec3.encode(b, getter3.apply(p_331912_));
                codec4.encode(b, getter4.apply(p_331912_));
                codec5.encode(b, getter5.apply(p_331912_));
                codec6.encode(b, getter6.apply(p_331912_));
                codec7.encode(b, getter7.apply(p_331912_));
                codec8.encode(b, getter8.apply(p_331912_));
                codec9.encode(b, getter9.apply(p_331912_));
                codec10.encode(b, getter10.apply(p_331912_));
                codec11.encode(b, getter11.apply(p_331912_));
                codec12.encode(b, getter12.apply(p_331912_));
                codec13.encode(b, getter13.apply(p_331912_));
                codec14.encode(b, getter14.apply(p_331912_));
            }
        };
    }
}
