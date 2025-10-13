package dev.lopyluna.slag.content.blocks.basin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.client.AlphaBufferSource;
import dev.lopyluna.slag.client.SolidBufferSource;
import dev.lopyluna.slag.content.blocks.multiblock.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BasinRenderer extends SafeBlockEntityRenderer<BasinBE> {
    public BasinRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(BasinBE be, float pt, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderFluid(be, pt, ms, buffer, light);
        ItemStack stack = be.resultItemStack.isEmpty() ? be.getStack() : be.resultItemStack;
        boolean preview = !be.resultItemStack.isEmpty() && be.getStack().isEmpty();
        if (stack.isEmpty()) return;
        var mc = Minecraft.getInstance();

        ms.pushPose();
        ms.translate(0.5f, 0.575f, 0.5f);
        ms.scale(1.5f, 1.5f, 1.5f);
        float cooling = !preview ? 1f : be.coolingTarget == 0 ? 0f : Mth.clamp(((float) be.coolingProgress / (float) be.coolingTarget) - 0.2f, 0f, 1f);
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, ms, new AlphaBufferSource(buffer, cooling), mc.level, 0);
        ms.popPose();
    }

    public void renderFluid(BasinBE be, float pt, PoseStack ms, MultiBufferSource buffer, int light) {
        var fluidLevel = be.getFluidLevel();
        if (fluidLevel == null) return;
        var tank = be.tankInventory;
        var fluidStack = tank.getFluid();
        if (fluidStack.isEmpty()) return;

        float capHeight = 1 / 16f;
        float tankHullWidth = 0.1f / 16f + 1 / 128f;
        float minPuddleHeight = 4 / 16f;
        float totalHeight = 1 - 2 * capHeight - minPuddleHeight;

        float level = fluidLevel.getValue(pt);
        if (level < 1 / (512f * totalHeight)) return;
        float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);
        float xMax = tankHullWidth + 1 - 2 * tankHullWidth, yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel, yMax = yMin + clampedLevel, zMax = tankHullWidth + 1 - 2 * tankHullWidth;

        ms.pushPose();
        ms.translate(0, clampedLevel - totalHeight, 0);
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluidStack, tankHullWidth, yMin, tankHullWidth, xMax, yMax, zMax, new SolidBufferSource(buffer).getBuffer(RenderType.SOLID), ms, light, false, true);
        ms.popPose();
    }
}
