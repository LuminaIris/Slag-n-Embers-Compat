package dev.lopyluna.slag.register;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.content.types.PartType;
import dev.lopyluna.slag.network.packets.SyncTypesS2C;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class AllDynamicTypes {
    private static final Map<ResourceLocation, MaterialType> MATERIAL_TYPES = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, PartType> PART_TYPES = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, ModularType> MODULAR_TYPES = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, MaterialType> CLIENT_MATERIAL_TYPES = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, PartType> CLIENT_PART_TYPES = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, ModularType> CLIENT_MODULAR_TYPES = new ConcurrentHashMap<>();

    public static MaterialType registerMaterial(MaterialType materialType) {
        if (materialType == null || materialType.id == null) return null;
        MATERIAL_TYPES.put(materialType.id, materialType);
        return materialType;
    }

    public static PartType registerPart(PartType partType) {
        if (partType == null || partType.id == null) return null;
        PART_TYPES.put(partType.id, partType);
        return partType;
    }

    public static ModularType registerModular(ModularType modularType) {
        if (modularType == null || modularType.id == null) return null;
        MODULAR_TYPES.put(modularType.id, modularType);
        return modularType;
    }

    public static Optional<MaterialType> getMaterial(ResourceLocation id) {
        if (id == null) return Optional.empty();
        var result = MATERIAL_TYPES.get(id);
        if (result == null && FMLEnvironment.dist.isClient()) result = getClientMaterial(id);
        return Optional.ofNullable(result);
    }

    public static Optional<PartType> getPart(ResourceLocation id) {
        if (id == null) return Optional.empty();
        var result = PART_TYPES.get(id);
        if (result == null && FMLEnvironment.dist.isClient()) result = getClientPart(id);
        return Optional.ofNullable(result);
    }

    public static Optional<ModularType> getModular(ResourceLocation id) {
        if (id == null) return Optional.empty();
        var result = MODULAR_TYPES.get(id);
        if (result == null && FMLEnvironment.dist.isClient()) result = getClientModular(id);
        return Optional.ofNullable(result);
    }

    public static Collection<MaterialType> getAllMaterials() {
        return MATERIAL_TYPES.isEmpty() ? (FMLEnvironment.dist.isClient() ? getClientMaterials() : List.of()) : MATERIAL_TYPES.values();
    }

    public static List<MaterialType> getAllMaterialsList() {
        return new ArrayList<>(getAllMaterials());
    }

    public static Collection<PartType> getAllParts() {
        return PART_TYPES.isEmpty() ? (FMLEnvironment.dist.isClient() ? getClientParts() : List.of()) : PART_TYPES.values();
    }

    public static List<PartType> getAllPartsList() {
        return new ArrayList<>(getAllParts());
    }

    public static List<PartType> getAllPartsFromModular(ModularType type) {
        return getAllPartsList().stream().filter(part -> type.segments.contains(part.segmentPart)).toList();
    }

    public static Collection<ModularType> getAllModulars() {
        return MODULAR_TYPES.isEmpty() ? (FMLEnvironment.dist.isClient() ? getClientModulars() : List.of()) : MODULAR_TYPES.values();
    }

    public static List<ModularType> getAllModularsList() {
        return new ArrayList<>(getAllModulars());
    }

    public static void syncToPlayer(ServerPlayer player) {
        if (player == null) return;
        PacketDistributor.sendToPlayer(player, new SyncTypesS2C(getAllMaterialsList(), getAllPartsList(), getAllModularsList()));
        SlagEmbers.LOGGER.info("Syncing player {}", player.getScoreboardName());
    }

    public static void syncToAll() {
        PacketDistributor.sendToAllPlayers(new SyncTypesS2C(getAllMaterialsList(), getAllPartsList(), getAllModularsList()));
    }

    @OnlyIn(Dist.CLIENT)
    public static void syncFromServer(List<MaterialType> materials, List<PartType> parts, List<ModularType> modulars) {
        clearClient();
        
        if (materials != null && !materials.isEmpty()) for (MaterialType material : materials) CLIENT_MATERIAL_TYPES.put(material.id, material);
        if (parts != null && !parts.isEmpty()) for (PartType part : parts) CLIENT_PART_TYPES.put(part.id, part);
        if (modulars != null && !modulars.isEmpty()) for (ModularType modular : modulars) CLIENT_MODULAR_TYPES.put(modular.id, modular);
    }

    @OnlyIn(Dist.CLIENT)
    private static MaterialType getClientMaterial(ResourceLocation id) {
        if (id == null) return null;
        return CLIENT_MATERIAL_TYPES.get(id);
    }

    @OnlyIn(Dist.CLIENT)
    private static PartType getClientPart(ResourceLocation id) {
        if (id == null) return null;
        return CLIENT_PART_TYPES.get(id);
    }

    @OnlyIn(Dist.CLIENT)
    private static ModularType getClientModular(ResourceLocation id) {
        if (id == null) return null;
        return CLIENT_MODULAR_TYPES.get(id);
    }

    @OnlyIn(Dist.CLIENT)
    private static Collection<MaterialType> getClientMaterials() {
        if (CLIENT_MATERIAL_TYPES.isEmpty()) return List.of();
        return CLIENT_MATERIAL_TYPES.values();
    }

    @OnlyIn(Dist.CLIENT)
    private static Collection<PartType> getClientParts() {
        if (CLIENT_PART_TYPES.isEmpty()) return List.of();
        return CLIENT_PART_TYPES.values();
    }

    @OnlyIn(Dist.CLIENT)
    private static Collection<ModularType> getClientModulars() {
        if (CLIENT_MODULAR_TYPES.isEmpty()) return List.of();
        return CLIENT_MODULAR_TYPES.values();
    }

    public static void clear() {
        MATERIAL_TYPES.clear();
        PART_TYPES.clear();
        MODULAR_TYPES.clear();
    }

    @OnlyIn(Dist.CLIENT)
    public static void clearClient() {
        CLIENT_MATERIAL_TYPES.clear();
        CLIENT_PART_TYPES.clear();
        CLIENT_MODULAR_TYPES.clear();
    }
}

