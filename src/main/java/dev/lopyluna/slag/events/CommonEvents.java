package dev.lopyluna.slag.events;

import dev.lopyluna.slag.content.blocks.SimpleBE;
import dev.lopyluna.slag.content.blocks.basin.BasinBE;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.content.blocks.multiblock.FluidMultiBlockEntity;
import dev.lopyluna.slag.content.blocks.table.TableBE;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import static dev.lopyluna.slag.SlagEmbers.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var be : SimpleBE.regCap) be.registerCapabilities(event);
        FluidMultiBlockEntity.registerCapabilities(event);
        BasinBE.registerCapabilities(event);
        MelterBE.registerCapabilities(event);
        TableBE.registerCapabilities(event);
    }
}
