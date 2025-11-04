package dev.lopyluna.slag.content;

import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.items.modular.ModularItem;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllTags;
import io.netty.buffer.ByteBuf;
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
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

import static com.tterrag.registrate.providers.RegistrateRecipeProvider.has;
import static dev.lopyluna.slag.SlagEmbers.REG;

@SuppressWarnings("unused")
public class AllUtils {
    public static final LinkedHashMap<ResourceKey<TrimMaterial>, Float> TRIM_MATERIALS = new LinkedHashMap<>();
    static {
        TRIM_MATERIALS.put(TrimMaterials.QUARTZ, 0.1F);
        TRIM_MATERIALS.put(TrimMaterials.IRON, 0.2F);
        TRIM_MATERIALS.put(TrimMaterials.NETHERITE, 0.3F);
        TRIM_MATERIALS.put(TrimMaterials.REDSTONE, 0.4F);
        TRIM_MATERIALS.put(TrimMaterials.COPPER, 0.5F);
        TRIM_MATERIALS.put(TrimMaterials.GOLD, 0.6F);
        TRIM_MATERIALS.put(TrimMaterials.EMERALD, 0.7F);
        TRIM_MATERIALS.put(TrimMaterials.DIAMOND, 0.8F);
        TRIM_MATERIALS.put(TrimMaterials.LAPIS, 0.9F);
        TRIM_MATERIALS.put(TrimMaterials.AMETHYST, 1.0F);
    }

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


    public static boolean matchesAnyTag(ModularItem item, ItemStack self, TagKey<Item> tag) {
        if ((!item.hasModularType(self) || !self.has(AllDataComponents.BAKED))) return false;
        var modularType = item.getModularType(self);
        if (modularType == null) return false;
        for (var itemTag : modularType.itemTags) if (eqOrSuperset(tag, itemTag)) return true;
        return false;
    }

    public static boolean matchesAnyTag(IDynamicPart item, ItemStack self, TagKey<Item> tag) {
        var type = item.getPartType(self);
        if (type.isEmpty()) return false;
        for (var itemTag : type.get().itemTags) if (eqOrSuperset(tag, itemTag)) return true;
        return false;
    }

    public static List<TagKey<Item>> getTags(ModularItem item, ItemStack self) {
        if ((!item.hasModularType(self) || !self.has(AllDataComponents.BAKED))) return List.of();
        var modularType = item.getModularType(self);
        if (modularType == null) return List.of();
        return modularType.itemTags;
    }
    public static List<TagKey<Item>> getTags(IDynamicPart item, ItemStack self) {
        var type = item.getPartType(self);
        if (type.isEmpty()) return List.of();
        return type.get().itemTags;
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

    private static Optional<Registry<Item>> itemRegistry() {
        var srv = ServerLifecycleHooks.getCurrentServer();
        if (srv != null) return Optional.of(srv.registryAccess().registryOrThrow(Registries.ITEM));
        var mc = Minecraft.getInstance();
        if (mc.level != null) return Optional.of(mc.level.registryAccess().registryOrThrow(Registries.ITEM));
        return Optional.empty();
    }
}
