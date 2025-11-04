package dev.lopyluna.slag.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.client.ArmorRendering;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
    public HumanoidArmorLayerMixin(RenderLayerParent<T, M> renderer) {super(renderer);}

    @Shadow protected abstract void renderGlint(PoseStack pose, MultiBufferSource buffer, int light, Model model);
    @Shadow protected abstract void renderTrim(Holder<ArmorMaterial> material, PoseStack pose, MultiBufferSource buffer, int light, ArmorTrim trim, Model model, boolean innerTexture);
    @Shadow protected abstract void setPartVisibility(A model, EquipmentSlot slot);
    @Shadow protected abstract Model getArmorModelHook(T entity, ItemStack itemStack, EquipmentSlot slot, A model);
    @Shadow protected abstract boolean usesInnerModel(EquipmentSlot slot);

    @SuppressWarnings("unchecked")
    @Inject(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V", at = @At("HEAD"), cancellable = true)
    private void renderArmorPiece(PoseStack pose, MultiBufferSource buffer, T living, EquipmentSlot slot, int light, A pModel,
                                  float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        ArmorRendering.renderArmorPiece(((HumanoidArmorLayer<T, M, A>) (Object) this),
                this::getParentModel, () -> setPartVisibility(pModel, slot), s -> getArmorModelHook(living, s, slot, pModel), () -> usesInnerModel(slot),
                (h, t, m, f) -> () -> this.renderTrim(h, pose, buffer, light, t, m, f), m -> () -> this.renderGlint(pose, buffer, light, m),
                pose, buffer, living, slot, light, pModel, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch, ci);
    }
}
