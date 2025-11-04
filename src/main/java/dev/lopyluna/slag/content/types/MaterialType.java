package dev.lopyluna.slag.content.types;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lopyluna.slag.SlagEmbers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class MaterialType {
    public final ResourceLocation id;
    public int sortOrder;

    public final float speed;
    public final float dura;
    public final float tier;
    public final float sharp;

    public final float kbRes;
    public final float defense;
    public final float tough;

    public final float ench;

    public final String texture;
    public final boolean fireProof;
    public final Supplier<Ingredient> repairMaterials;
    public final Supplier<Fluid> moltenFluid;
    public final List<Integer> palette;

    public boolean dontRegister;

    private static final Codec<Integer> COLOR_CODEC = Codec.either(Codec.STRING.comapFlatMap(str -> { try {
        var hexStr = str.startsWith("0x") || str.startsWith("0X") ? str.substring(2) : str;
        int color = Integer.parseUnsignedInt(hexStr, 16);
        return DataResult.success(color);
    } catch (NumberFormatException e) {
        return DataResult.error(() -> "Invalid hex color: " + str);
    }}, color -> String.format("0x%06X", color)), Codec.INT).xmap(either -> either.map(i -> i, i -> i), Either::right);

    public static final Codec<MaterialType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(m -> m.id),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(m -> m.sortOrder),

            Codec.FLOAT.optionalFieldOf("knockback_resistance", 0f).forGetter(m -> m.kbRes),
            Codec.FLOAT.optionalFieldOf("defense", 0f).forGetter(m -> m.defense),
            Codec.FLOAT.optionalFieldOf("toughness", 0f).forGetter(m -> m.tough),

            Codec.FLOAT.optionalFieldOf("speed", 0f).forGetter(m -> m.speed),
            Codec.FLOAT.optionalFieldOf("durability", 0f).forGetter(m -> m.dura),
            Codec.FLOAT.optionalFieldOf("tier", 0f).forGetter(m -> m.tier),
            Codec.FLOAT.optionalFieldOf("sharpness", 0f).forGetter(m -> m.sharp),
            Codec.FLOAT.optionalFieldOf("enchantability", 0f).forGetter(m -> m.ench),

            Codec.BOOL.optionalFieldOf("fireproof", false).forGetter(m -> m.fireProof),
            Codec.STRING.optionalFieldOf("texture", "base").forGetter(m -> m.texture),
            Ingredient.CODEC.optionalFieldOf("repair_ingredient", Ingredient.EMPTY).forGetter(m -> m.repairMaterials.get()),
            ResourceLocation.CODEC.optionalFieldOf("molten_fluid").forGetter(m -> {
                Fluid fluid = m.moltenFluid.get();
                if (fluid == null) return Optional.empty();
                return Optional.of(BuiltInRegistries.FLUID.getKey(fluid));
            }),
            COLOR_CODEC.listOf().optionalFieldOf("palette", List.of()).forGetter(m -> m.palette))

            .apply(instance, MaterialType::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MaterialType> STREAM_CODEC = StreamCodec.of(MaterialType::toNetwork, MaterialType::fromNetwork);

    @Override
    public int hashCode() {
        var fluidHash = 0;
        if (moltenFluid != null && moltenFluid.get() != null) fluidHash = moltenFluid.get().hashCode();
        var repairHash = 0;
        if (repairMaterials != null && !repairMaterials.get().isEmpty() && repairMaterials.get() != null) repairHash = repairMaterials.get().hashCode();
        return id.hashCode() + texture.hashCode() + repairHash + fluidHash + palette.hashCode() + Objects.hash(kbRes, defense, tough, speed, dura, tier, sharp, ench, fireProof, sortOrder);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MaterialType other)) return false;
        if (other.fireProof != fireProof) return false;
        if (!Objects.equals(other.texture, texture)) return false;
        if (!other.repairMaterials.get().test(repairMaterials.get().getItems()[0])) return false;
        if (other.moltenFluid.get() != moltenFluid.get()) return false;
        if (other.kbRes != kbRes) return false;
        if (other.defense != defense) return false;
        if (other.tough != tough) return false;
        if (other.speed != speed) return false;
        if (other.dura != dura) return false;
        if (other.tier != tier) return false;
        if (other.sharp != sharp) return false;
        if (other.ench != ench) return false;
        if (!other.palette.equals(palette)) return false;
        if (other.sortOrder != sortOrder) return false;
        return other.id.equals(id);
    }

    private static MaterialType fromNetwork(RegistryFriendlyByteBuf buf) {
        ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
        int sortOrder = buf.readInt();
        float kbRes = buf.readFloat();
        float defense = buf.readFloat();
        float tough = buf.readFloat();
        float speed = buf.readFloat();
        float dura = buf.readFloat();
        float tier = buf.readFloat();
        float sharp = buf.readFloat();
        float ench = buf.readFloat();
        boolean fireProof = buf.readBoolean();
        String texture = buf.readUtf();
        Ingredient repairIngredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        boolean hasFluid = buf.readBoolean();
        Fluid fluid = hasFluid ? BuiltInRegistries.FLUID.get(ResourceLocation.STREAM_CODEC.decode(buf)) : null;
        List<Integer> palette = new ArrayList<>();
        int paletteSize = buf.readVarInt();
        for (int i = 0; i < paletteSize; i++) palette.add(buf.readInt());

        System.out.println("Material from network: " + id);
        System.out.println("  Speed: " + speed);
        System.out.println("  Dura: " + dura);
        System.out.println("  Tier: " + tier);
        System.out.println("  Sharp: " + sharp);
        return new MaterialType(id, sortOrder, kbRes, defense, tough, speed, dura, tier, sharp, ench, fireProof, texture, () -> repairIngredient, () -> fluid, palette);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, MaterialType type) {
        ResourceLocation.STREAM_CODEC.encode(buf, type.id);
        buf.writeInt(type.sortOrder);
        buf.writeFloat(type.kbRes);
        buf.writeFloat(type.defense);
        buf.writeFloat(type.tough);
        buf.writeFloat(type.speed);
        buf.writeFloat(type.dura);
        buf.writeFloat(type.tier);
        buf.writeFloat(type.sharp);
        buf.writeFloat(type.ench);
        buf.writeBoolean(type.fireProof);
        buf.writeUtf(type.texture);
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, type.repairMaterials.get());
        var fluid = type.moltenFluid.get();
        buf.writeBoolean(fluid != null);
        if (fluid != null) ResourceLocation.STREAM_CODEC.encode(buf, BuiltInRegistries.FLUID.getKey(fluid));
        buf.writeVarInt(type.palette.size());
        for (int color : type.palette) buf.writeInt(color);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public MaterialType(ResourceLocation id, int sortOrder, float kbRes, float defense, float tough, float speed, float dura, float tier, float sharp, float ench, boolean fireProof, String texture, Ingredient repairIngredient, Optional<ResourceLocation> moltenFluidId, List<Integer> palette) {
        dontRegister = id == null || id.getNamespace().isEmpty() || id.getPath().isEmpty() || id.getPath().equals("null") || id.getPath().equals("empty");
        this.id = id;
        this.sortOrder = sortOrder;

        this.kbRes = kbRes;
        this.defense = defense;
        this.tough = tough;

        this.speed = speed;
        this.dura = dura;
        this.tier = tier;
        this.sharp = sharp;
        this.ench = ench;

        this.texture = texture;
        this.fireProof = fireProof;
        this.repairMaterials = () -> repairIngredient;
        this.moltenFluid = () -> moltenFluidId.map(BuiltInRegistries.FLUID::get).orElse(null);
        this.palette = palette;
    }

    public MaterialType(ResourceLocation id, int sortOrder, float kbRes, float defense, float tough, float speed, float dura, float tier, float sharp, float ench, boolean fireProof, String texture, Supplier<Ingredient> repairIngredient, Supplier<Fluid> moltenFluid, List<Integer> palette) {
        dontRegister = id == null || id.getNamespace().isEmpty() || id.getPath().isEmpty() || id.getPath().equals("null") || id.getPath().equals("empty");
        this.id = id;
        this.sortOrder = sortOrder;

        this.kbRes = kbRes;
        this.defense = defense;
        this.tough = tough;

        this.speed = speed;
        this.dura = dura;
        this.tier = tier;
        this.sharp = sharp;
        this.ench = ench;

        this.texture = texture;
        this.fireProof = fireProof;
        this.repairMaterials = repairIngredient;
        this.moltenFluid = moltenFluid;
        this.palette = palette;
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private float kbRes = 0;
        private float defense = 0;
        private float tough = 0;

        private float speed = 0;
        private float dura = 0;
        private float tier = 0;
        private float sharp = 0;
        private float ench = 0;

        private boolean fireProof = false;
        private final Supplier<Ingredient> repair;
        private Supplier<Fluid> moltenFluid = () -> null;
        private List<Integer> palette = new ArrayList<>();
        
        private String texture = "base";
        private final ResourceLocation id;
        private int sortOrder = 0;

        public Builder(ResourceLocation id, Supplier<Ingredient> repairMaterial) {
            this.id = id;
            repair = repairMaterial;
        }
        public Builder(String id, Supplier<Ingredient> repairMaterial) {
            this.id = SlagEmbers.loc(id);
            repair = repairMaterial;
        }

        public Builder setSortOrder(int value) { sortOrder = value; return this; }

        public Builder setKBRes(float value) { kbRes = Math.max(value, 0f); return this; }
        public Builder setDefense(float value) { defense = Math.max(value, 0f); return this; }
        public Builder setTough(float value) { tough = Math.max(value, 0f); return this; }

        public Builder setSpeed(int value) { speed = Math.max(value, 0f); return this; }
        public Builder setDura(int value) { dura = Math.max(value, 0f); return this; }
        public Builder setTier(int value) { tier = Math.max(value, 0f); return this; }
        public Builder setSharp(float value) { sharp = Math.max(value, 0f); return this; }
        public Builder setEnch(int value) { ench = Math.max(value, 0f); return this; }
        public Builder setTexture(String texture) { this.texture = texture; return this; }
        public Builder moltenFluid(Supplier<Fluid> moltenFluid) { this.moltenFluid = moltenFluid; return this; }
        public Builder fireProof() { this.fireProof = true; return this; }
        public Builder palette(List<Integer> palette) { this.palette = palette; return this; }

        public Builder apply(Function<Builder, Builder> func) {
            return func.apply(this);
        }

        public MaterialType register() {
            return new MaterialType(id, sortOrder, kbRes, defense, tough, speed, dura, tier, sharp, ench, fireProof, texture, repair, moltenFluid, palette);
        }
    }
}
