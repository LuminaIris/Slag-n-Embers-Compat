package dev.lopyluna.slag.content.commands;

import dev.lopyluna.slag.SlagEmbers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = SlagEmbers.MOD_ID)
public class CommandEventHandler {
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ReloadModelsCommand.register(event.getDispatcher());
    }
}

