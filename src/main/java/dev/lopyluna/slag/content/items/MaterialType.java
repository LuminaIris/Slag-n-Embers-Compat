package dev.lopyluna.slag.content.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import org.joml.Math;

import java.util.Optional;
import java.util.function.Supplier;

public class MaterialType {
    final float speed;
    final float dura;
    final float tough;
    final float sharp;

    final float kbRes;
    final float defence;
    final float toughness;

    final float ench;


    public final String texture;
    public final String id;
    public final boolean fireProof;
    public final Supplier<Ingredient> repairMaterials;
    public final Supplier<Fluid> moltenFluid;

    public static final Codec<MaterialType> CODEC_TOOL = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(m -> m.id),
            Codec.FLOAT.fieldOf("speed").forGetter(m -> m.speed),
            Codec.FLOAT.fieldOf("dura").forGetter(m -> m.dura),
            Codec.FLOAT.fieldOf("tough").forGetter(m -> m.tough),
            Codec.FLOAT.fieldOf("sharp").forGetter(m -> m.sharp),
            Codec.FLOAT.fieldOf("ench").forGetter(m -> m.ench),

            Codec.BOOL.optionalFieldOf("fireproof", false).forGetter(m -> m.fireProof),
            Codec.STRING.optionalFieldOf("texture", "base").forGetter(m -> m.texture),
            Ingredient.CODEC.fieldOf("repair_ingredient").forGetter(m -> m.repairMaterials.get()),
            ResourceLocation.CODEC.optionalFieldOf("molten_fluid").forGetter(m -> {
                Fluid fluid = m.moltenFluid.get();
                if (fluid == null) return Optional.empty();
                return Optional.of(BuiltInRegistries.FLUID.getKey(fluid));
            })
        ).apply(instance, MaterialType::new)
    );

    public static final Codec<MaterialType> CODEC_ARMOR = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(m -> m.id),

                    Codec.FLOAT.fieldOf("kb_res").forGetter(m -> m.kbRes),
                    Codec.FLOAT.fieldOf("defence").forGetter(m -> m.defence),
                    Codec.FLOAT.fieldOf("toughness").forGetter(m -> m.toughness),

                    Codec.BOOL.optionalFieldOf("fireproof", false).forGetter(m -> m.fireProof),
                    Codec.STRING.optionalFieldOf("texture", "base").forGetter(m -> m.texture),
                    Ingredient.CODEC.fieldOf("repair_ingredient").forGetter(m -> m.repairMaterials.get()),
                    ResourceLocation.CODEC.optionalFieldOf("molten_fluid").forGetter(m -> {
                        Fluid fluid = m.moltenFluid.get();
                        if (fluid == null) return Optional.empty();
                        return Optional.of(BuiltInRegistries.FLUID.getKey(fluid));
                    })
            ).apply(instance, MaterialType::new)
    );

    @Override
    public int hashCode() {
        return id.hashCode() + texture.hashCode() + repairMaterials.get().hashCode() + moltenFluid.get().hashCode() ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MaterialType other) return id.equals(other.id) && texture.equals(other.texture) && repairMaterials.get().equals(other.repairMaterials.get()) && moltenFluid.get() == other.moltenFluid.get();
        return false;
    }

    public MaterialType(String id, float kbRes, float defence, float toughness, boolean fireProof, String texture, Ingredient repairIngredient, Optional<ResourceLocation> moltenFluidId) {
        this.id = id;

        this.kbRes = kbRes;
        this.defence = defence;
        this.toughness = toughness;

        this.speed = -1;
        this.dura = -1;
        this.tough = -1;
        this.sharp = -1;
        this.ench = -1;

        this.texture = texture;
        this.fireProof = fireProof;
        this.repairMaterials = () -> repairIngredient;
        this.moltenFluid = () -> moltenFluidId.map(BuiltInRegistries.FLUID::get).orElse(null);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private MaterialType(String id, float speed, float dura, float tough, float sharp, float ench, boolean fireProof, String texture, Ingredient repairIngredient, Optional<ResourceLocation> moltenFluidId) {
        this.id = id;

        this.speed = speed;
        this.dura = dura;
        this.tough = tough;
        this.sharp = sharp;
        this.ench = ench;

        this.kbRes = -1;
        this.defence = -1;
        this.toughness = -1;

        this.texture = texture;
        this.fireProof = fireProof;
        this.repairMaterials = () -> repairIngredient;
        this.moltenFluid = () -> moltenFluidId.map(BuiltInRegistries.FLUID::get).orElse(null);
    }

    public MaterialType(String id, float kbRes, float defence, float toughness, boolean fireProof, String texture, Supplier<Ingredient> repairMaterials, Supplier<Fluid> moltenFluid) {
        this.id = id;

        this.kbRes = kbRes;
        this.defence = defence;
        this.toughness = toughness;

        this.speed = -1;
        this.dura = -1;
        this.tough = -1;
        this.sharp = -1;
        this.ench = -1;

        this.texture = texture;
        this.fireProof = fireProof;
        this.repairMaterials = repairMaterials;
        this.moltenFluid = moltenFluid;
    }

    private MaterialType(String id, float speed, float dura, float tough, float sharp, float ench, boolean fireProof, String texture, Supplier<Ingredient> repairMaterials, Supplier<Fluid> moltenFluid) {
        this.id = id;

        this.speed = speed;
        this.dura = dura;
        this.tough = tough;
        this.sharp = sharp;
        this.ench = ench;

        this.kbRes = -1;
        this.defence = -1;
        this.toughness = -1;

        this.texture = texture;
        this.fireProof = fireProof;
        this.repairMaterials = repairMaterials;
        this.moltenFluid = moltenFluid;
    }

    public static class Builder {
        private float speed = 0;
        private float dura = 0;
        private float tough = 0;
        private float sharp = 0;
        private float ench = 0;

        private float kbRes = 0;
        private float defence = 0;
        private float toughness = 0;

        private boolean fireProof = false;
        private final Supplier<Ingredient> repair;
        private Supplier<Fluid> moltenFluid = () -> null;
        private String texture = "base";
        private final String id;

        public Builder(String id, Supplier<Ingredient> repairMaterial) {
            this.id = id;
            repair = repairMaterial;
        }

        public Builder setKBRes(float value) { kbRes = Math.max(value, 0f); return this; }
        public Builder setDefence(float value) { defence = Math.max(value, 0f); return this; }
        public Builder setToughness(float value) { toughness = Math.max(value, 0f); return this; }

        public Builder setSpeed(int value) { speed = Math.max(value, 0f); return this; }
        public Builder setDura(int value) { dura = Math.max(value, 0f); return this; }
        public Builder setTough(int value) { tough = Math.max(value, 0f); return this; }
        public Builder setSharp(float value) { sharp = Math.max(value, 0f); return this; }
        public Builder setEnch(int value) { ench = Math.max(value, 0f); return this; }
        public Builder setTexture(String texture) { this.texture = texture; return this; }
        public Builder moltenFluid(Supplier<Fluid> moltenFluid) { this.moltenFluid = moltenFluid; return this; }
        public Builder fireProof() { this.fireProof = true; return this; }

        public Builder apply(java.util.function.Function<Builder, Builder> func) {
            return func.apply(this);
        }

        public MaterialType registerTool() {
            return new MaterialType(id, speed, dura, tough, sharp, ench, fireProof, texture, repair, moltenFluid);
        }
        public MaterialType registerArmor() {
            return new MaterialType(id, kbRes, defence, toughness, fireProof, texture, repair, moltenFluid);
        }
    }
}
