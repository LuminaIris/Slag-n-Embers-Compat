package dev.lopyluna.slag.events;

import dev.lopyluna.slag.SlagEmbers;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@SuppressWarnings({"removal"})
@EventBusSubscriber(modid = SlagEmbers.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class GameClientEvents {
    static Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        if (mc.level == null || mc.player == null) return;
    }
}
