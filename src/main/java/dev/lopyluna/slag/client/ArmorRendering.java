package dev.lopyluna.slag.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lopyluna.slag.content.items.modular.ModularEquipablesItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class ArmorRendering {

    public static <T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> void renderArmorPiece(HumanoidArmorLayer<T, M, A> humanoidArmorLayer,
            Supplier<M> getParentModel, Runnable setPartVisibility, Function<ItemStack, Model> getArmorModelHook, Supplier<Boolean> usesInnerModel,
            PropertyDispatch.QuadFunction<Holder<ArmorMaterial>, ArmorTrim, Model, Boolean, Runnable> renderTrim, Function<Model, Runnable> renderGlint,
            PoseStack pose, MultiBufferSource buffer, T living, EquipmentSlot slot, int light, A pModel,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        var stack = living.getItemBySlot(slot);
        if (stack.getItem() instanceof ModularEquipablesItem item) {
            if (item.getEquipmentSlot(stack) == slot) {
                getParentModel.get().copyPropertiesTo(pModel);
                setPartVisibility.run();
                var model = getArmorModelHook.apply(stack);
                boolean flag = usesInnerModel.get();
                var value = item.getPotentialArmorMaterials().value();

                var atlas = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
                TextureAtlasSprite spriteBase;

                var extensions = IClientItemExtensions.of(stack);
                extensions.setupModelAnimations(living, stack, slot, model, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
                int fallbackColor = extensions.getDefaultDyeColor(stack);

                for (int layerIdx = 0; layerIdx < value.layers().size(); layerIdx++) {
                    var layer = value.layers().get(layerIdx);
                    int j = extensions.getArmorLayerTintColor(stack, living, layer, layerIdx, fallbackColor);

                    var textures = item.getArmorTextures(stack, living, slot, layer, flag);
                    var base = textures.getFirst();
                    spriteBase = atlas.getSprite(base);

                    if (j != 0) model.renderToBuffer(pose, spriteBase.wrap(buffer.getBuffer(RenderType.armorCutoutNoCull(spriteBase.atlasLocation()))), light, OverlayTexture.NO_OVERLAY, j);
                    if (j != 0) for (var textureLocation : textures) {
                        if (textureLocation == null) continue;
                        if (textureLocation.equals(base)) continue;

                        VertexConsumer vc;
                        if (textureLocation.getPath().contains(".png")) vc = buffer.getBuffer(RenderType.armorCutoutNoCull(textureLocation));
                        else {
                            var sprite = atlas.getSprite(textureLocation);
                            vc = sprite.wrap(buffer.getBuffer(RenderType.armorCutoutNoCull(sprite.atlasLocation())));
                        }

                        model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, j);
                    }
                }

                var armortrim = stack.get(DataComponents.TRIM);
                if (armortrim != null) renderTrim.apply(item.getPotentialArmorMaterials(), armortrim, model, flag).run();
                if (stack.hasFoil()) renderGlint.apply(model).run();
            }
            ci.cancel();
        }
    }
}
