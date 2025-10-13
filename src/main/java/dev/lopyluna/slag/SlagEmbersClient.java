package dev.lopyluna.slag;

import dev.lopyluna.slag.client.render.CustomRenderedItemModel;
import dev.lopyluna.slag.client.render.CustomRenderedItems;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Map;
import java.util.function.Function;

import static dev.lopyluna.slag.SlagEmbers.MOD_ID;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class SlagEmbersClient {
    public SlagEmbersClient(IEventBus modEventBus) {
        modEventBus.addListener(SlagEmbersClient::onModelBake);
        modEventBus.addListener(SlagEmbersClient::clientInit);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
    }

    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> modelRegistry = event.getModels();
        CustomRenderedItems.forEach(item -> swapModels(modelRegistry, getItemModelLocation(item), CustomRenderedItemModel::new));
    }

    public static <T extends BakedModel> void swapModels(Map<ModelResourceLocation, BakedModel> modelRegistry, ModelResourceLocation location, Function<BakedModel, T> factory) {
        modelRegistry.put(location, factory.apply(modelRegistry.get(location)));
    }

    public static ModelResourceLocation getItemModelLocation(Item item) {
        return new ModelResourceLocation(RegisteredObjectsHelper.getKeyOrThrow(item), "inventory");
    }
}
