package dev.lopyluna.slag.content.types;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.utils.Function20;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class PartType {
    public final ResourceLocation id;
    public int sortOrder;

    public float speedMod;
    public float duraMod;
    public float tierMod;
    public float sharpMod;

    public float kbResMod;
    public float defenseMod;
    public float toughMod;

    public float enchMod;

    public float speed;
    public float dura;
    public float tier;
    public float sharp;

    public float kbRes;
    public float defense;
    public float tough;

    public float ench;

    public final TagKey<Item> segmentPart;
    public final List<TagKey<Item>> itemTags;

    public boolean dontRegister;

    public static final Codec<PartType> CODEC = RecordCodecBuilder.create(instance -> group(instance,
            ResourceLocation.CODEC.fieldOf("id").forGetter(m -> m.id),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(m -> m.sortOrder),
            Codec.FLOAT.optionalFieldOf("speed_modifier", 1f).forGetter(m -> m.speedMod),
            Codec.FLOAT.optionalFieldOf("durability_modifier", 1f).forGetter(m -> m.duraMod),
            Codec.FLOAT.optionalFieldOf("tier_modifier", 1f).forGetter(m -> m.tierMod),
            Codec.FLOAT.optionalFieldOf("sharpness_modifier", 1f).forGetter(m -> m.sharpMod),

            Codec.FLOAT.optionalFieldOf("knockback_resistance_modifier", 1f).forGetter(m -> m.kbResMod),
            Codec.FLOAT.optionalFieldOf("defence_modifier", 1f).forGetter(m -> m.defenseMod),
            Codec.FLOAT.optionalFieldOf("toughness_modifier", 1f).forGetter(m -> m.toughMod),
            Codec.FLOAT.optionalFieldOf("enchantability_modifier", 1f).forGetter(m -> m.enchMod),

            Codec.FLOAT.optionalFieldOf("speed", 0f).forGetter(m -> m.speed),
            Codec.FLOAT.optionalFieldOf("durability", 0f).forGetter(m -> m.dura),
            Codec.FLOAT.optionalFieldOf("tier", 0f).forGetter(m -> m.tier),
            Codec.FLOAT.optionalFieldOf("sharpness", 0f).forGetter(m -> m.sharp),
            Codec.FLOAT.optionalFieldOf("knockback_resistance", 0f).forGetter(m -> m.kbRes),
            Codec.FLOAT.optionalFieldOf("defence", 0f).forGetter(m -> m.defense),
            Codec.FLOAT.optionalFieldOf("toughness", 0f).forGetter(m -> m.tough),
            Codec.FLOAT.optionalFieldOf("enchantability", 0f).forGetter(m -> m.ench),

            TagKey.codec(Registries.ITEM).fieldOf("segment_part").forGetter(m -> m.segmentPart),
            TagKey.codec(Registries.ITEM).listOf().optionalFieldOf("item_tags", new ArrayList<>()).forGetter(m -> m.itemTags)
            ).apply(instance, PartType::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PartType> STREAM_CODEC = StreamCodec.of(PartType::toNetwork, PartType::fromNetwork);

    @Override
    public int hashCode() {
        var segmentPartHash = 0;
        if (segmentPart != null) segmentPartHash = segmentPart.hashCode();
        return 31 * id.hashCode() + 31 * itemTagsHash() + 31 * segmentPartHash + Objects.hash(speedMod, duraMod, tierMod, sharpMod, kbResMod, defenseMod, toughMod, enchMod, sortOrder);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PartType other)) return false;
        if (other.speedMod != speedMod) return false;
        if (other.duraMod != duraMod) return false;
        if (other.tierMod != tierMod) return false;
        if (other.sharpMod != sharpMod) return false;
        if (other.kbResMod != kbResMod) return false;
        if (other.defenseMod != defenseMod) return false;
        if (other.toughMod != toughMod) return false;
        if (other.enchMod != enchMod) return false;
        if (!other.segmentPart.equals(segmentPart)) return false;
        if (other.sortOrder != sortOrder) return false;
        if (equalItemTags(other.itemTags)) return false;
        return other.id.equals(id);
    }

    private static PartType fromNetwork(RegistryFriendlyByteBuf buf) {
        ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
        int sortOrder = buf.readInt();
        float speedMod = buf.readFloat();
        float duraMod = buf.readFloat();
        float tierMod = buf.readFloat();
        float sharpMod = buf.readFloat();
        float kbResMod = buf.readFloat();
        float defenseMod = buf.readFloat();
        float toughMod = buf.readFloat();
        float enchMod = buf.readFloat();
        float speed = buf.readFloat();
        float dura = buf.readFloat();
        float tier = buf.readFloat();
        float sharp = buf.readFloat();
        float kbRes = buf.readFloat();
        float defense = buf.readFloat();
        float tough = buf.readFloat();
        float ench = buf.readFloat();
        TagKey<Item> segmentPart = AllUtils.tagKeyStreamCodec(Registries.ITEM).decode(buf);

        boolean hasItemTags = buf.readBoolean();
        var itemTags = new ArrayList<TagKey<Item>>();
        if (hasItemTags) {
            int itemTagsSize = buf.readVarInt();
            if (itemTagsSize > 0) for (int i = 0; i < itemTagsSize; i++) itemTags.add(AllUtils.tagKeyStreamCodec(Registries.ITEM).decode(buf));
        }
        return new PartType(id, sortOrder, speedMod, duraMod, tierMod, sharpMod, kbResMod, defenseMod, toughMod, enchMod, speed, dura, tier, sharp, kbRes, defense, tough, ench, segmentPart, itemTags);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, PartType type) {
        ResourceLocation.STREAM_CODEC.encode(buf, type.id);
        buf.writeInt(type.sortOrder);
        buf.writeFloat(type.speedMod);
        buf.writeFloat(type.duraMod);
        buf.writeFloat(type.tierMod);
        buf.writeFloat(type.sharpMod);
        buf.writeFloat(type.kbResMod);
        buf.writeFloat(type.defenseMod);
        buf.writeFloat(type.toughMod);
        buf.writeFloat(type.enchMod);
        buf.writeFloat(type.speed);
        buf.writeFloat(type.dura);
        buf.writeFloat(type.tier);
        buf.writeFloat(type.sharp);
        buf.writeFloat(type.kbRes);
        buf.writeFloat(type.defense);
        buf.writeFloat(type.tough);
        buf.writeFloat(type.ench);
        AllUtils.tagKeyStreamCodec(Registries.ITEM).encode(buf, type.segmentPart);
        
        buf.writeBoolean(type.itemTags != null && !type.itemTags.isEmpty());
        if (type.itemTags != null && !type.itemTags.isEmpty()) {
            buf.writeVarInt(type.itemTags.size());
            for (var tag : type.itemTags) AllUtils.tagKeyStreamCodec(Registries.ITEM).encode(buf, tag);
        }
    }

    private PartType(ResourceLocation id, int sortOrder, float speedMod, float duraMod, float tierMod, float sharpMod, float kbResMod, float defenseMod, float toughMod, float enchMod, float speed, float dura, float tier, float sharp, float kbRes, float defense, float tough, float ench, TagKey<Item> segmentPart, List<TagKey<Item>> itemTags) {
        dontRegister = id == null || id.getNamespace().isEmpty() || id.getPath().isEmpty() || id.getPath().equals("null") || id.getPath().equals("empty");
        this.id = id;
        this.sortOrder = sortOrder;
        this.speedMod = speedMod;
        this.duraMod = duraMod;
        this.tierMod = tierMod;
        this.sharpMod = sharpMod;
        this.kbResMod = kbResMod;
        this.defenseMod = defenseMod;
        this.toughMod = toughMod;
        this.enchMod = enchMod;
        this.speed = speed;
        this.dura = dura;
        this.tier = tier;
        this.sharp = sharp;
        this.kbRes = kbRes;
        this.defense = defense;
        this.tough = tough;
        this.ench = ench;
        this.segmentPart = segmentPart;
        this.itemTags = itemTags;
    }

    public static class Builder {
        private float speedMod = 1;
        private float duraMod = 1;
        private float tierMod = 1;
        private float sharpMod = 1;
        private float kbResMod = 1;
        private float defenceMod = 1;
        private float toughMod = 1;
        private float enchMod = 1;
        private float speed;
        private float dura;
        private float tier;
        private float sharp;
        private float kbRes;
        private float defence;
        private float tough;
        private float ench;
        private final ResourceLocation id;
        private int sortOrder = 0;
        private TagKey<Item> segmentPart;
        private List<TagKey<Item>> itemTags = new ArrayList<>();

        public Builder(ResourceLocation id) {
            this.id = id;
        }
        public Builder(String id) {
            this.id = SlagEmbers.loc(id);
        }

        public Builder setSortOrder(int value) { sortOrder = value; return this; }

        public Builder setSpeedMod(float value) { speedMod = Math.max(value, 0f); return this; }
        public Builder setDuraMod(float value) { duraMod = Math.max(value, 0f); return this; }
        public Builder setTierMod(float value) { tierMod = Math.max(value, 0f); return this; }
        public Builder setSharpMod(float value) { sharpMod = Math.max(value, 0f); return this; }
        public Builder setKbResMod(float value) { kbResMod = Math.max(value, 0f); return this; }
        public Builder setDefenceMod(float value) { defenceMod = Math.max(value, 0f); return this; }
        public Builder setToughMod(float value) { toughMod = Math.max(value, 0f); return this; }
        public Builder setEnchMod(float value) { enchMod = Math.max(value, 0f); return this; }
        public Builder setSpeed(float value) { speed = Math.max(value, 0f); return this; }
        public Builder setDura(float value) { dura = Math.max(value, 0f); return this; }
        public Builder setTier(float value) { tier = Math.max(value, 0f); return this; }
        public Builder setSharp(float value) { sharp = Math.max(value, 0f); return this; }
        public Builder setKbRes(float value) { kbRes = Math.max(value, 0f); return this; }
        public Builder setDefence(float value) { defence = Math.max(value, 0f); return this; }
        public Builder setTough(float value) { tough = Math.max(value, 0f); return this; }
        public Builder setEnch(float value) { ench = Math.max(value, 0f); return this; }
        public Builder setSegmentPart(TagKey<Item> value) { segmentPart = value; return this; }

        public Builder itemTags(List<TagKey<Item>> itemTags) { this.itemTags = itemTags; return this; }
        @SafeVarargs public final Builder itemTags(TagKey<Item>... itemTags) { this.itemTags = List.of(itemTags); return this; }
        public Builder addItemTag(TagKey<Item> itemTag) { this.itemTags.add(itemTag); return this; }
        public Builder addItemTags(List<TagKey<Item>> itemTags) { this.itemTags.addAll(itemTags); return this; }
        @SafeVarargs public final Builder addItemTags(TagKey<Item>... itemTags) { this.itemTags.addAll(List.of(itemTags)); return this; }


        public PartType register() {
            return new PartType(id, sortOrder, speedMod, duraMod, tierMod, sharpMod, kbResMod, defenceMod, toughMod, enchMod, speed, dura, tier, sharp, kbRes, defence, tough, ench, segmentPart, itemTags);
        }
    }


    public int itemTagsHash() {
        int hash = 0;
        if (itemTags != null && !itemTags.isEmpty()) {
            hash = itemTags.hashCode();
            for (var tag : itemTags) hash += tag.hashCode();
        }
        return hash;
    }

    public boolean equalItemTags(List<TagKey<Item>> other) {
        if (this.itemTags == null || other == null || (this.itemTags.isEmpty() != other.isEmpty())) return false;
        for (var tag : other) if (!itemTags.contains(tag)) return false;
        return itemTags.size() == other.size();
    }

    private static <F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20> P20
            <F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20> group(final Applicative<F, ?> instance, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14, final App<F, T15> t15, final App<F, T16> t16, final App<F, T17> t17, final App<F, T18> t18, final App<F, T19> t19, final App<F, T20> t20) {
        return new P20<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20);
    }

    @SuppressWarnings("all")
    static final class P20<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20> {
        private final App<F, T1> t1;
        private final App<F, T2> t2;
        private final App<F, T3> t3;
        private final App<F, T4> t4;
        private final App<F, T5> t5;
        private final App<F, T6> t6;
        private final App<F, T7> t7;
        private final App<F, T8> t8;
        private final App<F, T9> t9;
        private final App<F, T10> t10;
        private final App<F, T11> t11;
        private final App<F, T12> t12;
        private final App<F, T13> t13;
        private final App<F, T14> t14;
        private final App<F, T15> t15;
        private final App<F, T16> t16;
        private final App<F, T17> t17;
        private final App<F, T18> t18;
        private final App<F, T19> t19;
        private final App<F, T20> t20;

        public P20(final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14, final App<F, T15> t15, final App<F, T16> t16, final App<F, T17> t17, final App<F, T18> t18, final App<F, T19> t19, final App<F, T20> t20) {
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
            this.t7 = t7;
            this.t8 = t8;
            this.t9 = t9;
            this.t10 = t10;
            this.t11 = t11;
            this.t12 = t12;
            this.t13 = t13;
            this.t14 = t14;
            this.t15 = t15;
            this.t16 = t16;
            this.t17 = t17;
            this.t18 = t18;
            this.t19 = t19;
            this.t20 = t20;
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final Function20<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R> function) {
            return apply(instance, instance.point(function));
        }

        public <R> App<F, R> apply(final Applicative<F, ?> instance, final App<F, Function20<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R>> function) {
            return ap20(instance, function, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20);
        }

        private <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R> App<F, R> ap20(final Applicative<F, ?> instance, final App<F, Function20<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R>> func, final App<F, T1> t1, final App<F, T2> t2, final App<F, T3> t3, final App<F, T4> t4, final App<F, T5> t5, final App<F, T6> t6, final App<F, T7> t7, final App<F, T8> t8, final App<F, T9> t9, final App<F, T10> t10, final App<F, T11> t11, final App<F, T12> t12, final App<F, T13> t13, final App<F, T14> t14, final App<F, T15> t15, final App<F, T16> t16, final App<F, T17> t17, final App<F, T18> t18, final App<F, T19> t19, final App<F, T20> t20) {
            return instance.ap4(instance.ap16(instance.map(Function20::curry16, func), t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16), t17, t18, t19, t20);
        }
    }
}
