package dev.lopyluna.slag.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class CustomRenderedItemModelRenderer extends BlockEntityWithoutLevelRenderer {
    public final Minecraft mc = Minecraft.getInstance();
    @SuppressWarnings("all") public CustomRenderedItemModelRenderer() {
        super(null, null);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        var itemRenderer = mc.getItemRenderer();
        var mainModel = (CustomRenderedItemModel) itemRenderer.getModel(stack, null, null, 0);
        var renderer = PartialItemModelRenderer.of(stack, transformType, ms, buffer, overlay);
        ms.pushPose();
        ms.translate(0.5F, 0.5F, 0.5F);
        render(stack, itemRenderer, mainModel, renderer, transformType, ms, buffer, light, overlay);
        ms.popPose();
    }

    protected abstract void render(ItemStack stack, ItemRenderer itemRenderer, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay);
}
