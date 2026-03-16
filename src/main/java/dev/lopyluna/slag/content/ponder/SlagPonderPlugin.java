package dev.lopyluna.slag.content.ponder;

import dev.lopyluna.slag.SlagEmbers;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SlagPonderPlugin implements PonderPlugin {
    @Override
    public @NotNull String getModId() {
        return SlagEmbers.MOD_ID;
    }

    @Override
    public void registerScenes(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        AllPonderScenes.register(helper);
    }
}
