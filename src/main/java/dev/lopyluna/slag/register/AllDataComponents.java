package dev.lopyluna.slag.register;

import com.mojang.serialization.Codec;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.items.modular_armor.ModularArmorItem;
import dev.lopyluna.slag.content.items.modular_tool.DataToolParts;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

import static dev.lopyluna.slag.SlagEmbers.REGISTER;

public class AllDataComponents {

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DataToolParts>> TOOL_PARTS = REGISTER.components()
            .registerComponentType("tool_parts", b -> b
                    .persistent(DataToolParts.CODEC).networkSynchronized(DataToolParts.STREAM_CODEC).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TagKey<Item>>> CAST_TYPE = REGISTER.components()
            .registerComponentType("cast_type", b -> b
                    .persistent(TagKey.codec(Registries.ITEM)).networkSynchronized(AllUtils.tagKeyStreamCodec(Registries.ITEM)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> CUTOUT = REGISTER.components()
            .registerComponentType("cutout", b -> b
                    .persistent(Codec.unit(Unit.INSTANCE)).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)).cacheEncoding());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ArmorItem.Type>> ARMOR_TYPE = REGISTER.components()
            .registerComponentType("armor_type", b -> b
                    .persistent(ArmorItem.Type.CODEC).networkSynchronized(ModularArmorItem.STREAM_CODEC).cacheEncoding());




    public static void register() {}
}
