package dev.lopyluna.slag.content.items.modular;

import com.mojang.serialization.Codec;
import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.register.AllDynamicTypes;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static net.minecraft.world.item.ItemStack.isSameItemSameComponents;

@SuppressWarnings({"deprecation", "unused"})
public class DataDynamicParts implements TooltipComponent {
    public static final DataDynamicParts EMPTY = new DataDynamicParts(new ArrayList<>());

    public static final Codec<DataDynamicParts> CODEC = ItemStack.CODEC.listOf().xmap(DataDynamicParts::new, parts -> parts.items);
    public static final StreamCodec<RegistryFriendlyByteBuf, DataDynamicParts> STREAM_CODEC =
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(DataDynamicParts::new, parts -> parts.items);

    public List<ItemStack> items;

    public DataDynamicParts(List<ItemStack> items) {
        this.items = items;
    }

    public ItemStack getItem(Item item) {
        for (var itemstack : this.items) if (itemstack.is(item)) return itemstack;
        return ItemStack.EMPTY;
    }

    public boolean contains(Item item) {
        for (var itemstack : this.items) if (itemstack.is(item)) return true;
        return false;
    }
    public boolean contains(ItemStack stack) {
        for (var itemstack : this.items) if (isSameItemSameComponents(itemstack, stack)) return true;
        return false;
    }


    public boolean containsDynamicPartSegment(IDynamicPart part, ItemStack stack) {
        return containsDynamicPartSegment(part.getPartSegment(stack));
    }
    public boolean containsDynamicPartSegment(TagKey<Item> part) {
        for (var itemstack : this.items) if (itemstack.getItem() instanceof IDynamicPart dynamic && dynamic.getPartSegment(itemstack).equals(part)) return true;
        return false;
    }

    public boolean hasAllDynamicPartSegments(List<TagKey<Item>> required) {
        if (required == null) return false;
        var have = new ArrayList<TagKey<Item>>();
        for (var s : this.items) {
            if (!(s.getItem() instanceof IDynamicPart p)) continue;
            var seg = p.getPartSegment(s);
            if (seg == null) return false;
            have.add(seg);
        }
        return containsAll(have, required);
    }

    public boolean containsAll(List<TagKey<Item>> have, List<TagKey<Item>> need) {
        if (have == null || need == null) return false;
        for (var n : need) if (!have.contains(n)) return false;
        for (var h : have) if (!need.contains(h)) return false;
        return have.size() == need.size();
    }


    public ItemStack getItemUnsafe(int index) {
        return this.items.get(index);
    }
    public Stream<ItemStack> itemCopyStream() {
        return this.items.stream().map(ItemStack::copy);
    }
    public List<ItemStack> items() {
        return this.items;
    }
    public List<ItemStack> itemsCopy() {
        if (isEmpty()) return new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();
        for (var stack : this.items) items.add(stack.copy());
        return items;
    }
    public List<ItemStack> getAllNonDynamicParts() {
        if (isEmpty()) return new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();
        for (var stack : this.items) {
            if (stack.getItem() instanceof IDynamicPart) continue;
            items.add(stack.copy());
        }
        return items;
    }
    public List<ItemStack> getAllDynamicParts() {
        if (isEmpty()) return new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();
        for (var stack : this.items) {
            if (!(stack.getItem() instanceof IDynamicPart)) continue;
            items.add(stack.copy());
        }
        return items;
    }
    public List<ItemStack> itemCopyRandom(Random random) {
        if (isEmpty()) return new ArrayList<>();
        List<ItemStack> items = itemsCopy();
        if (random == null) {
            var stick = ItemStack.EMPTY;
            int i = 0;
            for (var stack : items) {
                if (stack.is(Items.STICK)) {
                    stick = stack;
                    items.remove(i);
                    break;
                }
                i++;
            }
            if (!stick.isEmpty()) items.addFirst(stick);
            return items;
        }
        Collections.shuffle(items, random);
        var stick = ItemStack.EMPTY;
        int i = 0;
        for (var stack : items) {
            if (stack.is(Items.STICK)) {
                stick = stack;
                items.remove(i);
                break;
            }
            i++;
        }
        if (!stick.isEmpty()) items.addFirst(stick);
        return items;
    }

    public int size() {
        return this.items.size();
    }
    public boolean isEmpty() {
        return this.items == null || this.items.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof DataDynamicParts parts && itemMatches(this.items, parts.items));
    }

    public static boolean itemMatches(List<ItemStack> list, List<ItemStack> other) {
        if (list.size() != other.size()) return false;
        for (var stack : list) if (!containsSaidStack(other, stack)) return false;
        return true;
    }
    public static boolean containsSaidStack(List<ItemStack> list, ItemStack stack) {
        for (var s : list) if (matches(s, stack)) return true;
        return false;
    }
    public static boolean matches(ItemStack stack, ItemStack other) {
        if (stack.getItem() != other.getItem()) return false;
        return stack.getCount() == other.getCount() && isSameItemSameComponents(stack, other);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashStackList(this.items);
    }

    @Override
    public String toString() {
        return "ToolParts" + this.items;
    }

    public List<ItemStack> getPossibleStacks() {
        return getPossibleParts().stream().filter(o -> o instanceof ItemStack).map(o -> (ItemStack) o).toList();
    }

    @SuppressWarnings("unchecked")
    public List<TagKey<Item>> getPossibleTags() {
        return getPossibleParts().stream().filter(o -> o instanceof TagKey).map(o -> (TagKey<Item>) o).toList();
    }

    public List<ItemStack> getPossibleStacks(List<Object> list) {
        return list.stream().filter(o -> o instanceof ItemStack).map(o -> (ItemStack) o).toList();
    }

    @SuppressWarnings("unchecked")
    public List<TagKey<Item>> getPossibleTags(List<Object> list) {
        return list.stream().filter(o -> o instanceof TagKey).map(o -> (TagKey<Item>) o).toList();
    }

    public List<Object> getPossibleParts() {
        var result = new ArrayList<>();
        var modulars = AllDynamicTypes.getAllModulars();

        if (this.isEmpty()) {
            for (var modular : modulars) {
                for (var sStack : modular.finalSegmentStacks) if (!containsStack(result, sStack)) result.add(sStack);
                for (var tag : modular.segments) if (!containsTag(result, tag)) result.add(tag);
            }
            return result;
        }

        for (var modular : modulars) {
            var missingParts = getMissingParts(modular);
            if (missingParts != null) for (var part : missingParts) if (!contains(result, part)) result.add(part);
        }

        return result;
    }

    public List<Object> getPossiblePartCombinations(ModularType modular) {
        return getMissingParts(modular);
    }

    private List<Object> getMissingParts(ModularType modular) {
        var missing = new ArrayList<>();
        var matched = new ArrayList<>();

        for (var item : this.items) if (!modular.contains(item)) return null;

        for (var sStack : modular.finalSegmentStacks) {
            if (containsInItems(sStack)) matched.add(sStack);
            else missing.add(sStack);
        }

        for (var tag : modular.segments) {
            if (containsTagInItems(tag)) matched.add(tag);
            else missing.add(tag);
        }

        if (matched.isEmpty()) return null;
        return missing;
    }

    private boolean containsInItems(ItemStack stack) {
        for (var item : this.items) if (ItemStack.isSameItemSameComponents(item, stack) && item.getCount() == stack.getCount()) return true;
        return false;
    }

    private boolean containsTagInItems(TagKey<Item> tag) {
        for (var item : this.items) if (item.getItem() instanceof IDynamicPart part && part.getPartSegment(item).equals(tag)) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean contains(List<Object> stacks, Object o) {
        if (o instanceof ItemStack stack) return containsStack(stacks, stack);
        if (o instanceof TagKey<?> tag) return containsTag(stacks, (TagKey<Item>) tag);
        return false;
    }

    private boolean contains(List<Object> stacks, ItemStack stack) {
        if (containsTag(stacks, stack)) return true;
        return !(stack.getItem() instanceof IDynamicPart) && containsStack(stacks, stack);
    }

    private boolean containsStack(List<Object> stacks, ItemStack stack) {
        for (var o : stacks) if (o instanceof ItemStack part && ItemStack.isSameItemSameComponents(stack, part) && stack.getCount() == part.getCount()) return true;
        return false;
    }
    private boolean containsTag(List<Object> stacks, ItemStack stack) {
        if (stack.getItem() instanceof IDynamicPart part) {
            var tag = part.getPartSegment(stack);
            for (var o : stacks) if (o instanceof TagKey<?>(ResourceKey<? extends Registry<?>> registry, ResourceLocation location) &&
                    location == tag.location() && registry.location() == tag.registry().location()) return true;
        }
        return false;
    }
    private boolean containsTag(List<Object> stacks, TagKey<Item> tag) {
        for (var o : stacks) if (o instanceof TagKey<?>(ResourceKey<? extends Registry<?>> registry, ResourceLocation location) &&
                location == tag.location() && registry.location() == tag.registry().location()) return true;
        return false;
    }
}
