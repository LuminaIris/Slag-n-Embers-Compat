package dev.lopyluna.slag.content.items.modular_tool;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.render.CustomRenderedItemModel;
import dev.lopyluna.slag.client.render.CustomRenderedItemModelRenderer;
import dev.lopyluna.slag.client.render.PartialItemModelRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;

import static dev.lopyluna.slag.SlagEmbers.MOD_ID;
import static dev.lopyluna.slag.register.AllItems.testMixture;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ModularToolRenderer extends CustomRenderedItemModelRenderer {
    @Override protected void render(ItemStack stack, ItemRenderer itemRenderer, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
                                    ItemDisplayContext ctx, PoseStack ms, MultiBufferSource buf, int light, int overlay) {
        var level = mc.level;
        var player = mc.player;
        if (level == null || player == null) return;
        var item = stack.getItem();

        if (!(item instanceof ModularToolItem tool)) {
            renderer.render(model.getOriginalModel(), light);
            return;
        }

        var parts = tool.getParts(stack);
        var fireImmune = stack.has(DataComponents.FIRE_RESISTANT);

        var shaper = itemRenderer.getItemModelShaper();
        var manager = shaper.getModelManager();


        var nudge = ctx == ItemDisplayContext.GUI || ctx.firstPerson() ? 0.0001f : ctx == ItemDisplayContext.GROUND || ctx == ItemDisplayContext.FIXED ? 0.01f : 0.001f;
        int i = 1;

        var mixture = tool.getToolMixture(stack);
        var pureMixture = tool.getPureMixture(stack);

        var bool = !mixture.isEmpty() && item instanceof BakedModularToolItem;

        if (!bool) renderer.render(model.getOriginalModel(), light);
        var totalItems = parts == null || parts.isEmpty() ? 0 : parts.size();
        var currentIndex = 0;
        var random = new Random(totalItems * 100L + (parts == null ? 0L : parts.hashCode()));
        var randomRot = random.nextFloat() * 360;
        var randomSpeedMod = (3.25f + (random.nextFloat() * 1.25f)) * (random.nextBoolean() ? 1 : -1);
        if (parts != null && !parts.isEmpty()) for (var pStack : parts.itemCopyRandom(random)) {
            if (pStack.isEmpty()) continue;
            ms.pushPose();
            ms.scale(1 + (i * nudge), 1 + (i * nudge), 1 + (i * (nudge * 2f)));

            var loc = BuiltInRegistries.ITEM.getKey(pStack.getItem());
            if (bool) {
                var builtModel = pStack.is(Items.STICK) ?
                        manager.getModel(ModelResourceLocation.standalone(SlagEmbers.loc(fireImmune ? "item/handle_fire_proof_" + mixture : "item/handle_" + mixture))) : pureMixture.isEmpty() ?
                        manager.getModel(ModelResourceLocation.standalone(SlagEmbers.loc(loc.getNamespace(), "item/" + loc.getPath() + "_built"))) :
                        manager.getModel(ModelResourceLocation.standalone(SlagEmbers.loc(loc.getNamespace(), "item/" + loc.getPath() + "_" + pureMixture)));

                renderer.render(builtModel, light);
            } else {
                ms.pushPose();

                float angleStep = 360.0f / totalItems;
                float angle = angleStep * currentIndex;

                float rotAngle = ((AnimationTickHolder.getRenderTime() * randomSpeedMod) + randomRot % 360);

                float angleRad = (float) Math.toRadians(angle + rotAngle);

                float radius = totalItems != 1 ? 1f / 16.0f : 0.5f/16f;
                float offsetX = (float) Math.cos(angleRad) * radius;
                float offsetY = (float) Math.sin(angleRad) * radius;

                ms.translate(offsetX, offsetY, 0);

                renderer.render(itemRenderer.getModel(pStack, level, player, 0), light);
                ms.popPose();
            }
            i++;
            currentIndex++;
            ms.popPose();
        } else renderer.render(model.getOriginalModel(), light);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional e) {
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            var item = entry.getValue();
            if (!(item instanceof IToolPart tool)) continue;
            var key = BuiltInRegistries.ITEM.getKey(item);
            e.register(ModelResourceLocation.standalone(SlagEmbers.loc(key.getNamespace(), "item/" + key.getPath() + "_built")));
            for (var mixture : testMixture(tool.getPartSegment().getPath())) e.register(ModelResourceLocation.standalone(SlagEmbers.loc(key.getNamespace(), "item/" + key.getPath() + "_" + mixture)));
        }
        for (var mixture : List.of("pickaxe", "axe", "shovel", "hoe", "sword", "mattock", "prybar", "graip", "mallet", "hammer", "scythe", "maul", "paxel")) {
            e.register(ModelResourceLocation.standalone(SlagEmbers.loc("item/handle_fire_proof_" + mixture)));
            e.register(ModelResourceLocation.standalone(SlagEmbers.loc("item/handle_" + mixture)));
        }
    }
}
