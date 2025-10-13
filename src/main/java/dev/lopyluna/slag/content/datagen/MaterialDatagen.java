package dev.lopyluna.slag.content.datagen;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.MaterialType;
import dev.lopyluna.slag.register.AllMaterials;
import dev.lopyluna.slag.register.AllRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MaterialDatagen extends DatapackBuiltinEntriesProvider {

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
        .add(AllRegistries.MATERIAL_TYPE_REGISTRY_KEY, bootstrap -> {
            for (MaterialType material : AllMaterials.MATERIAL_TYPES) registerMaterial(bootstrap, material);
        });

    private static void registerMaterial(BootstrapContext<MaterialType> bootstrap, MaterialType material) {
        ResourceKey<MaterialType> key = ResourceKey.create(AllRegistries.MATERIAL_TYPE_REGISTRY_KEY, SlagEmbers.loc(material.id));
        bootstrap.register(key, material);
    }

    public MaterialDatagen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(SlagEmbers.MOD_ID));
    }
}

