package dev.lopyluna.slag.register;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.items.MaterialType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = SlagEmbers.MOD_ID)
public class AllRegistries {

    public static final ResourceKey<Registry<MaterialType>> MATERIAL_TYPE_REGISTRY_KEY = 
        ResourceKey.createRegistryKey(SlagEmbers.loc("materials"));

    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(MATERIAL_TYPE_REGISTRY_KEY, MaterialType.CODEC_TOOL, MaterialType.CODEC_TOOL);
    }
}

