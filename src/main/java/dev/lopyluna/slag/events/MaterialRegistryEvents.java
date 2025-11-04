package dev.lopyluna.slag.events;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.register.AllDynamicTypes;
import dev.lopyluna.slag.register.AllRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = SlagEmbers.MOD_ID)
public class MaterialRegistryEvents {

    //@SubscribeEvent
    //public static void onServerStarting(ServerStartingEvent event) {
    //}

    public static int tick = 0;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (tick > 1) tick--;
        if (tick == 1) {
            var server = event.getLevel().getServer();
            if (server == null) return;
            server.getPlayerList().getPlayers().forEach(AllDynamicTypes::syncToPlayer);
            //AllDynamicTypes.syncToAll();
            SlagEmbers.LOGGER.info("Synced material registry to all players");
            tick--;
        }
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        var server = event.getPlayerList().getServer();
        var materialRegistry = server.registryAccess().registryOrThrow(AllRegistries.MATERIAL_TYPE_REGISTRY_KEY);
        var partRegistry = server.registryAccess().registryOrThrow(AllRegistries.PART_TYPE_REGISTRY_KEY);
        var modularRegistry = server.registryAccess().registryOrThrow(AllRegistries.MODULAR_TYPE_REGISTRY_KEY);

        AllDynamicTypes.clear();
        materialRegistry.stream().filter(m -> !m.dontRegister).forEach(AllDynamicTypes::registerMaterial);
        partRegistry.stream().filter(p -> !p.dontRegister).forEach(AllDynamicTypes::registerPart);
        modularRegistry.stream().filter(m -> !m.dontRegister).forEach(AllDynamicTypes::registerModular);

        SlagEmbers.LOGGER.info("Loaded {} materials from datapack registry", AllDynamicTypes.getAllMaterialsList().size());
        SlagEmbers.LOGGER.info("Loaded {} parts from datapack registry", AllDynamicTypes.getAllPartsList().size());

        //server.getPlayerList().getPlayers().forEach(AllDynamicTypes::syncToPlayer);
        tick = 20;
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) AllDynamicTypes.syncToPlayer(player);
    }
}

