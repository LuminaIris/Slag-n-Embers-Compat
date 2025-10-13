package dev.lopyluna.slag.content.items.modular_tool;

import com.mojang.serialization.Codec;
import dev.lopyluna.slag.SlagEmbers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.stream.Stream;

import static net.minecraft.world.item.ItemStack.isSameItemSameComponents;

@SuppressWarnings({"deprecation", "unused"})
public class DataToolParts implements TooltipComponent {
    public static final DataToolParts EMPTY = new DataToolParts(new ArrayList<>());

    public static final Codec<DataToolParts> CODEC = ItemStack.CODEC.listOf().xmap(DataToolParts::new, parts -> parts.items);
    public static final StreamCodec<RegistryFriendlyByteBuf, DataToolParts> STREAM_CODEC =
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).map(DataToolParts::new, parts -> parts.items);

    public List<ItemStack> items;

    public DataToolParts(List<ItemStack> items) {
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
    public boolean containsPartSegment(IToolPart part) {
        return containsPartSegment(part.getPartSegment());
    }
    public boolean containsPartSegment(String part) {
        if (part.contains(":")) return containsPartSegment(ResourceLocation.parse(part));
        return containsPartSegment(SlagEmbers.loc(part));
    }
    public boolean containsPartSegment(ResourceLocation part) {
        for (var itemstack : this.items) if (itemstack.getItem() instanceof IToolPart toolPart && toolPart.getPartSegment().equals(part)) return true;
        return false;
    }

    public boolean hasAllPartSegments(String... required) {
        if (required == null) return false;
        List<ResourceLocation> loc = new ArrayList<>();
        for (var r : required) {
            if (r.contains(":")) loc.add(ResourceLocation.parse(r));
            loc.add(SlagEmbers.loc(r));
        }
        if (loc.isEmpty()) return false;

        Map<ResourceLocation, Integer> need = new HashMap<>();
        for (var r : loc) {
            if (r == null) return false;
            need.merge(r, 1, Integer::sum);
        }

        Map<ResourceLocation, Integer> have = new HashMap<>();
        int partCount = 0;
        for (var s : this.items) {
            if (!(s.getItem() instanceof IToolPart p)) continue;
            var seg = p.getPartSegment();
            if (seg == null) return false;
            have.merge(seg, 1, Integer::sum);
            partCount++;
        }

        if (partCount != loc.size()) return false;
        return have.equals(need);
    }

    public boolean hasAllPartSegments(ResourceLocation... required) {
        if (required == null) return false;

        Map<ResourceLocation, Integer> need = new HashMap<>();
        for (var r : required) {
            if (r == null) return false;
            need.merge(r, 1, Integer::sum);
        }

        Map<ResourceLocation, Integer> have = new HashMap<>();
        int partCount = 0;
        for (var s : this.items) {
            if (!(s.getItem() instanceof IToolPart p)) continue;
            var seg = p.getPartSegment();
            if (seg == null) return false;
            have.merge(seg, 1, Integer::sum);
            partCount++;
        }

        if (partCount != required.length) return false;
        return have.equals(need);
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
    public List<ItemStack> itemCopyRandom(Random random) {
        if (isEmpty()) return new ArrayList<>();
        List<ItemStack> items = itemsCopy();
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
        items.addFirst(stick);
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
        return this == other || (other instanceof DataToolParts parts && listMatches(this.items, parts.items));
    }

    public static boolean listMatches(List<ItemStack> list, List<ItemStack> other) {
        if (list.size() != other.size()) return false;
        for (int i = 0; i < list.size(); ++i) {
            int finalI = i;
            if (other.stream().noneMatch(s -> matches(list.get(finalI), s))) return false;
        }
        return true;
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
}
