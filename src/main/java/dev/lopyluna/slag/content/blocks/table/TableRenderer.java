package dev.lopyluna.slag.content.blocks.table;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.lopyluna.slag.client.AlphaBufferSource;
import dev.lopyluna.slag.client.SolidBufferSource;
import dev.lopyluna.slag.content.blocks.melter.MelterBlock;
import dev.lopyluna.slag.content.blocks.multiblock.renderer.SafeBlockEntityRenderer;
import dev.lopyluna.slag.register.AllDataComponents;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TableRenderer extends SafeBlockEntityRenderer<TableBE> {
    public TableRenderer(BlockEntityRendererProvider.Context context) {}
    Minecraft mc = Minecraft.getInstance();

    @Override
    protected void renderSafe(TableBE be, float pt, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderFluid(be, pt, ms, buffer, light);
        var itemRenderer = mc.getItemRenderer();
        renderResultItem(be, itemRenderer, ms, buffer, light, overlay);
        renderMoldItem(be, itemRenderer, ms, buffer, light, overlay);

    }

    public void renderMoldItem(TableBE be, ItemRenderer ir, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        ItemStack stack = be.moldItemStack.isEmpty() ? be.getMold() : be.moldItemStack;
        if (stack.isEmpty()) return;
        var base = stack.getItem().getDefaultInstance();
        var cutout = stack.copy();
        base.set(AllDataComponents.CUTOUT, Unit.INSTANCE);
        cutout.set(AllDataComponents.CUTOUT, Unit.INSTANCE);

        ms.pushPose();
        ms.translate(0.5f, 0.625f, 0.5f);
        ms.mulPose(Axis.XP.rotationDegrees(90));
        var state = be.getBlockState();
        var facing = state.hasProperty(MelterBlock.FACING) ? state.getValue(MelterBlock.FACING) : Direction.NORTH;
        ms.mulPose(Axis.ZP.rotationDegrees(AngleHelper.horizontalAngle(facing.getAxis() == Direction.Axis.Z ? facing.getOpposite() : facing)));
        ms.scale(12.1f/16f, 12f/16f, 12.1f/16f);

        ms.pushPose();
        ms.translate(0f, 0f, 0.1f);
        ir.renderStatic(base, ItemDisplayContext.FIXED, light, overlay, ms, buffer, mc.level, 0);
        ms.popPose();

        ms.pushPose();
        ms.scale(1f, 1f, 2.6f);
        ms.translate(0f, 0f, -0.002f);
        ir.renderStatic(cutout, ItemDisplayContext.FIXED, light, overlay, ms, buffer, mc.level, 0);
        ms.popPose();

        ms.popPose();
    }

    public void renderResultItem(TableBE be, ItemRenderer ir, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        ItemStack stack = be.resultItemStack.isEmpty() ? be.getStack() : be.resultItemStack;
        boolean preview = !be.resultItemStack.isEmpty() && be.getStack().isEmpty();
        if (stack.isEmpty()) return;

        ms.pushPose();
        ms.translate(0.5f, 0.672f, 0.5f);
        ms.mulPose(Axis.XP.rotationDegrees(90));
        var state = be.getBlockState();
        var facing = state.hasProperty(MelterBlock.FACING) ? state.getValue(MelterBlock.FACING) : Direction.NORTH;
        ms.mulPose(Axis.ZP.rotationDegrees(AngleHelper.horizontalAngle(facing.getAxis() == Direction.Axis.Z ? facing.getOpposite() : facing)));
        ms.scale(12.1f/16f, 12f/16f, 12.1f/16f);
        float cooling = !preview ? 1f : be.coolingTarget == 0 ? 0f : Mth.clamp(((float) be.coolingProgress / (float) be.coolingTarget) - 0.2f, 0f, 1f);
        ir.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, ms, new AlphaBufferSource(buffer, cooling), mc.level, 0);
        ms.popPose();
    }

    public void renderFluid(TableBE be, float pt, PoseStack ms, MultiBufferSource buffer, int light) {
        var fluidLevel = be.getFluidLevel();
        if (fluidLevel == null) return;
        var tank = be.tankInventory;
        var fluidStack = tank.getFluid();
        if (fluidStack.isEmpty()) return;

        float capHeight = 5f / 16f;
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
