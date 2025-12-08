package dev.lopyluna.slag.content.items.modular;

import com.mojang.serialization.Codec;
import dev.lopyluna.slag.content.items.dynamic_part.IDynamicPart;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllDynamicTypes;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
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
    public List<TagKey<Item>> getAllDynamicPartSegments() {
        if (isEmpty()) return new ArrayList<>();
        List<TagKey<Item>> items = new ArrayList<>();
        for (var stack : this.items) {
            if (!(stack.getItem() instanceof IDynamicPart p)) continue;
            var seg = p.getPartSegment(stack);
            if (seg == null) continue;
            items.add(seg);
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

    public List<ItemStack> getPossibleStacks(List<Object> list) {
        return list.stream().filter(o -> o instanceof ItemStack).map(o -> (ItemStack) o).toList();
    }

    @SuppressWarnings("unchecked")
    public List<TagKey<Item>> getPossibleTags(List<Object> list) {
        return list.stream().filter(o -> o instanceof TagKey).map(o -> (TagKey<Item>) o).toList();
    }

    public List<Object> getPossibleParts() {
        //var result = new ArrayList<>();
        //var modulars = AllDynamicTypes.getAllModulars();

        //if (this.isEmpty()) {
        //    for (var modular : modulars) {
        //        for (var sStack : modular.finalSegmentStacks) if (!containsStack(result, sStack)) result.add(sStack);
        //        for (var tag : modular.segments) if (!containsTag(result, tag)) result.add(tag);
        //    }
        //    return result;
        //}

        //for (var modular : modulars) {
        //    var missingParts = getMissingParts(modular);
        //    if (missingParts != null) for (var part : missingParts) if (!contains(result, part)) result.add(part);
        //}

        //return result;
        return new ArrayList<>();
    }


    public List<ModularType> getPossibleModulars() {
        //var result = new ArrayList<ModularType>();
        //var modulars = AllDynamicTypes.getAllModulars();

        //if (this.isEmpty()) {
        //    for (var modular : modulars) if (noModularType(result, modular)) result.add(modular);
        //    return result;
        //} else for (var modular : getPotentialModulars()) if (noModularType(result, modular)) result.add(modular);
        //return result;
        return new ArrayList<>();
    }

    //public List<Object> getPossiblePartsFromModulars() {
    //    var has = new ArrayList<>();
    //    var result = new ArrayList<>();
    //    var modulars = getPossibleModulars();

    //    for (var stack : this.items) {
    //        var item = stack.getItem();
    //        if (item instanceof IDynamicPart dynamic) has.add(dynamic.getPartSegment(stack));
    //        else has.add(stack);
    //    }

    //    var item = AllItems.DYNAMIC_PART.get();

    //    for (var modular : modulars) {


    //    }

    //    return result;
    //}

    private List<ModularType> getPotentialModulars() {
        var potential = new ArrayList<ModularType>();
        var ignore = new ArrayList<ModularType>();

        for (var modular : AllDynamicTypes.getAllModulars()) for (var item : this.items) if (!modular.contains(item)) ignore.add(modular);
        for (var modular : AllDynamicTypes.getAllModulars()) if (!ignore.contains(modular)) potential.add(modular);

        return potential;
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

    // Helpers
    // =====================================================================================================================================================================================

    private boolean containsInItems(ItemStack stack) {
        for (var item : this.items) if (isSameItemSameSomeComponents(item, stack)) return true;
        return false;
    }

    private boolean containsTagInItems(TagKey<Item> tag) {
        for (var item : this.items) {
            if (item.getItem() instanceof IDynamicPart part && part.getPartSegment(item).equals(tag)) return true;
            if (item.is(tag)) return true;
        }
        return false;
    }

    private static boolean noModularType(List<ModularType> modulars, ModularType modular) {
        for (var m : modulars) if (m.equals(modular)) return false;
        return true;
    }

    public static boolean isSameItemSameSomeComponents(ItemStack stack, ItemStack other) {
        if (stack.isEmpty() || other.isEmpty()) return false;
        if (!ItemStack.isSameItem(stack, other)) return false;
        if (stack.getCount() != other.getCount()) return false;
        return containsAllSameSomeComponents(stack.getComponents(), other.getComponents());
    }

    public static boolean containsAll(List<?> list, List<?> other) {
        if (list == null || other == null) return false;
        if (list.isEmpty() || other.isEmpty()) return false;
        if (list.size() != other.size()) return false;
        for (var o : other) if (!contains(list, o)) return false;
        return true;
    }

    @SuppressWarnings("unchecked")
    public static boolean contains(List<?> list, Object other) {
        for (var o : list) {
            if (o instanceof ItemStack s && other instanceof ItemStack oStack) if (isSameItemSameSomeComponents(s, oStack)) return true;
            if (o instanceof TagKey<?> t && other instanceof TagKey<?> oTag) if (t.equals(oTag)) return true;
            if (o instanceof ItemStack s && other instanceof TagKey<?> oTag) if (oTag.isFor(Registries.ITEM) && s.is((TagKey<Item>) oTag)) return true;
            if (o.equals(other)) return true;
        }
        return false;
    }

    public static boolean containsAllSameSomeComponents(DataComponentMap a, DataComponentMap b) {
        if (a == null || b == null) return false;
        var aSet = a.keySet();
        var bSet = b.keySet();
        aSet.remove(AllDataComponents.BUILT.get());
        aSet.remove(AllDataComponents.BAKED.get());
        aSet.remove(AllDataComponents.MODULAR_TYPE.get());
        bSet.remove(AllDataComponents.BUILT.get());
        bSet.remove(AllDataComponents.BAKED.get());
        bSet.remove(AllDataComponents.MODULAR_TYPE.get());
        return aSet.containsAll(bSet);
    }
}
