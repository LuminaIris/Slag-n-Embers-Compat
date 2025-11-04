package dev.lopyluna.slag.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.ModularType;
import dev.lopyluna.slag.content.types.PartType;
import dev.lopyluna.slag.register.AllDynamicTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = SlagEmbers.MOD_ID, value = Dist.CLIENT)
public class ResourceFallbackGenerator {

    private static final Map<ResourceLocation, JsonObject> RUNTIME_MODELS = new ConcurrentHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path GENERATED_PACK_PATH;

    @SubscribeEvent
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        try {
            var resourceManager = Minecraft.getInstance().getResourceManager();
            Set<String> namespaces = resourceManager.getNamespaces();

            for (String namespace : namespaces) {
                String partPath = "models/item/dynamic_parts";
                String modularPath = "models/item/modular";

                try {
                    var partResources = resourceManager.listResources(partPath, path -> path.getPath().endsWith(".json") && path.getNamespace().equals(namespace));

                    for (var resourceKey : partResources.keySet()) {
                        String path = resourceKey.getPath();
                        if (path.startsWith("models/") && path.endsWith(".json")) {
                            String modelPath = path.substring("models/".length(), path.length() - ".json".length());
                            ResourceLocation modelLocation = ResourceLocation.fromNamespaceAndPath(namespace, modelPath);
                            event.register(ModelResourceLocation.standalone(modelLocation));
                        }
                    }
                    var modularResources = resourceManager.listResources(modularPath, path -> path.getPath().endsWith(".json") && path.getNamespace().equals(namespace));

                    for (var resourceKey : modularResources.keySet()) {
                        String path = resourceKey.getPath();
                        if (path.startsWith("models/") && path.endsWith(".json")) {
                            String modelPath = path.substring("models/".length(), path.length() - ".json".length());
                            ResourceLocation modelLocation = ResourceLocation.fromNamespaceAndPath(namespace, modelPath);
                            event.register(ModelResourceLocation.standalone(modelLocation));
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        try {
            GENERATED_PACK_PATH = getGeneratedPackPath();
            generateModelsFromRegistry();

            int generated = generateAllModels();
            int palettesGenerated = generateAllPalettes();

            generateAtlasFile(generated > 0 || palettesGenerated > 0);
            createPackMcmeta();

            var packLocationInfo = new PackLocationInfo(
                "slag_generated_models",
                Component.literal("Slag Generated Assets"),
                PackSource.BUILT_IN,
                Optional.empty()
            );

            var packResources = new PathPackResources.PathResourcesSupplier(GENERATED_PACK_PATH);
            var packSelectionConfig = new PackSelectionConfig(true, Pack.Position.BOTTOM, true);

            var pack = Pack.readMetaAndCreate(
                packLocationInfo,
                packResources,
                PackType.CLIENT_RESOURCES,
                packSelectionConfig
            );
            if (pack != null) event.addRepositorySource(consumer -> consumer.accept(pack));
        } catch (Exception ignored) {}
    }

    private static Path getGeneratedPackPath() {
        return FMLPaths.GAMEDIR.get().resolve("generated");
    }

    private static int generateAllModels() {
        int generated = 0;

        for (Map.Entry<ResourceLocation, JsonObject> entry : RUNTIME_MODELS.entrySet()) {
            ResourceLocation modelLocation = entry.getKey();
            JsonObject modelJson = entry.getValue();

            try {
                if (writeModelFileIfMissing(modelLocation, modelJson)) generated++;
            } catch (IOException ignored) {}
        }
        return generated;
    }

    private static boolean createPackMcmeta() throws IOException {
        Path packMcmeta = GENERATED_PACK_PATH.resolve("pack.mcmeta");

        if (Files.exists(packMcmeta)) return false;

        Files.createDirectories(GENERATED_PACK_PATH);

        JsonObject pack = new JsonObject();
        JsonObject packInfo = new JsonObject();
        packInfo.addProperty("pack_format", 34);
        packInfo.addProperty("description", "Runtime generated assets for Slag n' Embers");
        pack.add("pack", packInfo);

        String jsonString = GSON.toJson(pack);
        Files.writeString(packMcmeta, jsonString);
        return true;
    }

    private static boolean writeModelFileIfMissing(ResourceLocation modelLocation, JsonObject modelJson) throws IOException {
        String namespace = modelLocation.getNamespace();
        String path = modelLocation.getPath();

        Path modelFile = GENERATED_PACK_PATH.resolve("assets/" + namespace + "/models/" + path + ".json");

        if (Files.exists(modelFile)) return false;

        Files.createDirectories(modelFile.getParent());

        String jsonString = GSON.toJson(modelJson);
        Files.writeString(modelFile, jsonString);
        return true;
    }

    private static void generateModelsFromRegistry() {
        List<Pair<ResourceLocation, ResourceLocation>> requestedModels = getRequestedModels();

        for (Pair<ResourceLocation, ResourceLocation> pair : requestedModels) {
            ResourceLocation modelLocation = pair.getFirst();
            ResourceLocation textureLocation = pair.getSecond();

            String texturePath = textureLocation.getNamespace() + ":" + textureLocation.getPath();

            JsonObject model = new JsonObject();
            model.addProperty("parent", "minecraft:item/generated");

            JsonObject textures = new JsonObject();
            textures.addProperty("layer0", texturePath);
            model.add("textures", textures);

            RUNTIME_MODELS.put(modelLocation, model);
        }
    }

    private static boolean generateAtlasFile(boolean force) throws IOException {
        Path atlasFile = GENERATED_PACK_PATH.resolve("assets/minecraft/atlases/blocks.json");

        boolean fileExists = Files.exists(atlasFile);
        if (fileExists && !force) {
            var existingJson = GSON.fromJson(Files.readString(atlasFile), JsonObject.class);
            if (existingJson.has("sources") && !existingJson.getAsJsonArray("sources").isEmpty()) return false;
        }

        Files.createDirectories(atlasFile.getParent());

        JsonObject root;
        JsonArray sources;

        if (fileExists) {
            try {
                root = GSON.fromJson(Files.readString(atlasFile), JsonObject.class);
                sources = root.has("sources") ? root.getAsJsonArray("sources") : new JsonArray();
            } catch (Exception e) {
                root = new JsonObject();
                sources = new JsonArray();
            }
        } else {
            root = new JsonObject();
            sources = new JsonArray();
        }

        var materials = AllDynamicTypes.getAllMaterials();
        var parts = AllDynamicTypes.getAllParts();
        var modulars = AllDynamicTypes.getAllModulars();

        Map<String, Map<String, String>> materialsTextures = new HashMap<>();

        for (MaterialType material : materials) {
            String matID = material.id.getPath();
            String paletteKey = material.id.getNamespace() + ":color_palettes/" + matID;

            var texture = material.texture;
            if (materialsTextures.containsKey(texture)) {
                materialsTextures.get(texture).put(matID, paletteKey);
            } else {
                var matTextures = new HashMap<String, String>();
                matTextures.put(matID, paletteKey);
                materialsTextures.put(texture, matTextures);
            }
        }

        if (materialsTextures.isEmpty()) return false;

        boolean anyChanges = false;

        for (Map.Entry<String, Map<String, String>> entry : materialsTextures.entrySet()) {
            String textureCategory = entry.getKey();
            Map<String, String> mats = entry.getValue();

            JsonObject existingEntry = findSlagEntryByTextureCategory(sources, textureCategory, modulars, parts, false);

            if (existingEntry != null) {
                boolean changed = updatePalettedPermutation(existingEntry, modulars, parts, textureCategory, mats, false);
                if (changed) anyChanges = true;
            } else {
                sources.add(createPalettedPermutation(modulars, parts, textureCategory, mats, false));
                anyChanges = true;
            }

            JsonObject existingArmorEntry = findSlagEntryByTextureCategory(sources, textureCategory, modulars, parts, true);
            Map<String, String> armorMats = new HashMap<>();
            for (var matEntry : mats.entrySet()) {
                armorMats.put(matEntry.getKey(), matEntry.getValue().replace("color_palettes/", "color_palettes/armor/"));
            }

            if (existingArmorEntry != null) {
                boolean changed = updatePalettedPermutation(existingArmorEntry, modulars, parts, textureCategory, armorMats, true);
                if (changed) anyChanges = true;
            } else {
                sources.add(createPalettedPermutation(modulars, parts, textureCategory, armorMats, true));
                anyChanges = true;
            }
        }

        if (!anyChanges) return false;

        root.add("sources", sources);

        String jsonString = GSON.toJson(root);
        Files.writeString(atlasFile, jsonString);
        return true;
    }

    private static int generateAllPalettes() {
        var requestedPalettes = getRequestedPalettes();
        int generated = 0;
        for (var palette : requestedPalettes) {
            try {
                if (generatePaletteImage(palette.getFirst(), palette.getSecond())) generated++;
            } catch (IOException ignored) {}
        }
        return generated;
    }

    private static boolean generatePaletteImage(ResourceLocation loc, List<Integer> colors) throws IOException {
        String namespace = loc.getNamespace();
        String path = loc.getPath();
        Path modelFile = GENERATED_PACK_PATH.resolve("assets/" + namespace + "/textures/color_palettes/" + path + ".png");
        Path modelFileArmor = GENERATED_PACK_PATH.resolve("assets/" + namespace + "/textures/color_palettes/armor/" + path + ".png");

        if (Files.exists(modelFile)) return false;
        Files.createDirectories(modelFile.getParent());
        Files.createDirectories(modelFileArmor.getParent());
        var image = PaletteHelper.gradientToImage(8, colors);
        var armorColors = PaletteHelper.colorsToGradient(8, colors);
        armorColors.removeLast();
        var imageArmor = PaletteHelper.gradientToImage(9, armorColors);
        ImageIO.write(image, "png", modelFile.toFile());
        ImageIO.write(imageArmor, "png", modelFileArmor.toFile());
        return true;
    }

    private static JsonObject findSlagEntryByTextureCategory(JsonArray sources, String textureCategory, Collection<ModularType> modulars, Collection<PartType> parts, boolean isArmor) {
        String paletteKey = isArmor ? "slag:color_palettes/armor/base_palette" : "slag:color_palettes/base_palette";

        for (int i = 0; i < sources.size(); i++) {
            var element = sources.get(i);
            if (!element.isJsonObject()) continue;

            var obj = element.getAsJsonObject();
            if (!obj.has("type") || !obj.get("type").getAsString().equals("paletted_permutations")) continue;
            if (!obj.has("palette_key") || !obj.get("palette_key").getAsString().equals(paletteKey)) continue;
            if (!obj.has("textures") || !obj.get("textures").isJsonArray()) continue;

            var textures = obj.getAsJsonArray("textures");
            if (textures.isEmpty()) continue;

            String firstTexture = textures.get(0).getAsString();
            if (isArmor) {
                if (firstTexture.startsWith("slag:armors/" + textureCategory + "_layer_")) return obj;
            } else {
                String prefix = textureCategory.isEmpty() ? "slag:item/dynamic_parts/" : "slag:item/dynamic_parts/" + textureCategory + "/";
                if (firstTexture.startsWith(prefix)) return obj;
            }
        }
        return null;
    }

    private static boolean updatePalettedPermutation(JsonObject entry, Collection<ModularType> modulars, Collection<PartType> parts, String textureCategory, java.util.Map<String, String> materials, boolean isArmor) {
        com.google.gson.JsonArray existingTextures = entry.has("textures") ? entry.getAsJsonArray("textures") : new com.google.gson.JsonArray();
        Set<String> existingTextureSet = new HashSet<>();

        for (int i = 0; i < existingTextures.size(); i++) existingTextureSet.add(existingTextures.get(i).getAsString());

        int texturesAdded = 0;
        if (isArmor) {
            for (int layer = 1; layer <= 2; layer++) {
                String textureLocation = "slag:armors/" + textureCategory + "_layer_" + layer;
                if (!existingTextureSet.contains(textureLocation)) {
                    existingTextures.add(textureLocation);
                    texturesAdded++;
                }
            }
            for (PartType part : parts) {
                String partID = part.id.getPath();
                if (partID.contains("helmet") || partID.contains("chestplate") || partID.contains("leggings") || partID.contains("boots")) continue;
                for (int layer = 1; layer <= 2; layer++) {
                    String partTextureLocation = "slag:armors/" + partID + "/" + textureCategory + "_layer_" + layer;
                    if (!existingTextureSet.contains(partTextureLocation)) {
                        existingTextures.add(partTextureLocation);
                        texturesAdded++;
                    }
                }
            }
        } else {
            String prefix = textureCategory.isEmpty() ? "slag:item/dynamic_parts/" : "slag:item/dynamic_parts/" + textureCategory + "/";
            String modularPrefix = textureCategory.isEmpty() ? "slag:item/modular/" : "slag:item/modular/${modular}/" + textureCategory + "/";

            for (PartType part : parts) {
                String partID = part.id.getPath();
                String textureLocation = prefix + partID;
                if (!existingTextureSet.contains(textureLocation)) {
                    existingTextures.add(textureLocation);
                    texturesAdded++;
                }
                for (ModularType modular : modulars) {
                    if (!modular.segments.contains(part.segmentPart)) continue;
                    String modularID = modular.id.getPath();
                    String textLoc = modularPrefix.replace("${modular}", modularID) + partID;
                    if (!existingTextureSet.contains(textLoc)) {
                        existingTextures.add(textLoc);
                        texturesAdded++;
                    }
                }
            }
        }

        entry.add("textures", existingTextures);

        JsonObject existingPermutations = entry.has("permutations") ? entry.getAsJsonObject("permutations") : new JsonObject();

        int permutationsAdded = 0;
        for (var materialEntry : materials.entrySet()) {
            if (!existingPermutations.has(materialEntry.getKey())) permutationsAdded++;
            existingPermutations.addProperty(materialEntry.getKey(), materialEntry.getValue());
        }

        entry.add("permutations", existingPermutations);

        return texturesAdded > 0 || permutationsAdded > 0;
    }

    private static JsonObject createPalettedPermutation(Collection<ModularType> modulars, Collection<PartType> parts, String textureCategory, java.util.Map<String, String> materials, boolean isArmor) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "paletted_permutations");

        com.google.gson.JsonArray textures = new com.google.gson.JsonArray();

        if (isArmor) {
            for (int layer = 1; layer <= 2; layer++) textures.add("slag:armors/" + textureCategory + "_layer_" + layer);
            for (PartType part : parts) {
                var partID = part.id.getPath();
                if (partID.contains("helmet") || partID.contains("chestplate") || partID.contains("leggings") || partID.contains("boots")) continue;
                for (int layer = 1; layer <= 2; layer++) textures.add("slag:armors/" + partID + "/" + textureCategory + "_layer_" + layer);
            }
            entry.addProperty("palette_key", "slag:color_palettes/armor/base_palette");
        } else {
            String prefix = textureCategory.isEmpty() ? "slag:item/dynamic_parts/" : "slag:item/dynamic_parts/" + textureCategory + "/";
            String modularPrefix = textureCategory.isEmpty() ? "slag:item/modular/" : "slag:item/modular/${modular}/" + textureCategory + "/";

            for (PartType part : parts) {
                String partID = part.id.getPath();
                textures.add(prefix + partID);
                for (ModularType modular : modulars) {
                    if (!modular.segments.contains(part.segmentPart)) continue;
                    String modularID = modular.id.getPath();
                    textures.add(modularPrefix.replace("${modular}", modularID) + partID);
                }
            }
            entry.addProperty("palette_key", "slag:color_palettes/base_palette");
        }

        entry.add("textures", textures);

        JsonObject permutations = new JsonObject();
        for (var materialEntry : materials.entrySet()) permutations.addProperty(materialEntry.getKey(), materialEntry.getValue());
        entry.add("permutations", permutations);

        return entry;
    }

    public static void addRuntimeModel(ResourceLocation modelLocation, JsonObject modelJson) {
        RUNTIME_MODELS.put(modelLocation, modelJson);
    }

    public static void addRuntimeModel(ResourceLocation modelLocation, String textureLayer0) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:item/generated");

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", textureLayer0);
        model.add("textures", textures);

        RUNTIME_MODELS.put(modelLocation, model);
    }

    public static void clearRuntimeModels() {
        RUNTIME_MODELS.clear();
    }

    public static void reloadModels(boolean reloadPack) {
        try {
            var genPackPath = GENERATED_PACK_PATH == null;
            if (genPackPath) GENERATED_PACK_PATH = getGeneratedPackPath();
            generateModelsFromRegistry();
            int generated = generateAllModels();
            int palettesGenerated = generateAllPalettes();

            Path atlasFile = GENERATED_PACK_PATH.resolve("assets/minecraft/atlases/blocks.json");
            boolean atlasExists = Files.exists(atlasFile);
            boolean forceAtlas = reloadPack || generated > 0 || palettesGenerated > 0 || !atlasExists;

            var atlasChanged = generateAtlasFile(forceAtlas);

            var packCreated = createPackMcmeta();

            if (generated > 0 || palettesGenerated > 0 || atlasChanged || packCreated || genPackPath)
                if (reloadPack) Minecraft.getInstance().execute(() -> Minecraft.getInstance().reloadResourcePacks());
        } catch (IOException ignored) {}
    }

    public static List<Pair<ResourceLocation, ResourceLocation>> getRequestedModels() {
        var list = new ArrayList<Pair<ResourceLocation, ResourceLocation>>();
        var materials = AllDynamicTypes.getAllMaterials();
        var parts = AllDynamicTypes.getAllParts();
        var modulars = AllDynamicTypes.getAllModulars();

        for (MaterialType material : materials) {
            String matID = material.id.getPath();
            String targetMod = material.id.getNamespace();

            for (PartType part : parts) {
                String partID = part.id.getPath();

                String namePath = "item/dynamic_parts/" + matID + "/" + partID;
                String texturePath = "item/dynamic_parts/" + material.texture + "/" + partID + "_" + matID;
                list.add(Pair.of(SlagEmbers.loc(targetMod, namePath), SlagEmbers.loc(targetMod, texturePath)));

                for (ModularType modular : modulars) {
                    if (!modular.segments.contains(part.segmentPart)) continue;
                    String modularID = modular.id.getPath();

                    namePath = "item/modular/" + modularID + "/" + matID + "/" + partID;
                    texturePath = "item/modular/" + modularID + "/" + material.texture + "/" + partID + "_" + matID;
                    list.add(Pair.of(SlagEmbers.loc(targetMod, namePath), SlagEmbers.loc(targetMod, texturePath)));
                }
            }
        }
        return list;
    }
    public static List<Pair<ResourceLocation, List<Integer>>> getRequestedPalettes() {
        var list = new ArrayList<Pair<ResourceLocation, List<Integer>>>();
        var materials = AllDynamicTypes.getAllMaterials();

        for (MaterialType material : materials) {
            var palette = material.palette;
            if (palette != null && !palette.isEmpty()) list.add(Pair.of(material.id, material.palette));
        }
        return list;
    }
}
