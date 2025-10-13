package dev.lopyluna.slag.events;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.MaterialType;
import dev.lopyluna.slag.register.AllItems;
import dev.lopyluna.slag.register.AllRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = SlagEmbers.MOD_ID)
public class MaterialRegistryEvents {
    
    private static final List<MaterialType> cachedMaterials = new ArrayList<>();
    
    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        var server = event.getServer();
        var materialRegistry = server.registryAccess().registryOrThrow(AllRegistries.MATERIAL_TYPE_REGISTRY_KEY);

        cachedMaterials.clear();
        materialRegistry.forEach(cachedMaterials::add);
        
        SlagEmbers.LOGGER.info("Loaded {} materials from datapack registry", cachedMaterials.size());
    }

    public static List<MaterialType> getMaterials() {
        return cachedMaterials.isEmpty() ? AllItems.MATERIAL_TYPES : cachedMaterials;
    }
}

