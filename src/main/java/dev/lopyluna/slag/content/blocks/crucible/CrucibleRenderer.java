package dev.lopyluna.slag.content.blocks.crucible;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.client.FluidRenderHelper;
import dev.lopyluna.slag.content.blocks.multiblock.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class CrucibleRenderer extends SafeBlockEntityRenderer<CrucibleBE> {

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(CrucibleBE be, float pt, PoseStack ms, MultiBufferSource bs, int light, int overlay) {
        if (!be.isController()) return;
        var fluidLevel = be.getFluidLevel();
        if (fluidLevel == null) return;

        float capHeight = 1 / 16f;
        float tankHullWidth = 0.1f / 16f + 1 / 128f;
        float minPuddleHeight = 4 / 16f;
        float totalHeight = be.getHeight() - 2 * capHeight - minPuddleHeight;

        float level = fluidLevel.getValue(pt);
        if (level < 1 / (512f * totalHeight)) return;
        float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);

        float xMax = tankHullWidth + be.getWidthX() - 2f * tankHullWidth;
        float zMax = tankHullWidth + be.getWidthZ() - 2f * tankHullWidth;
        float yBase = totalHeight + capHeight + minPuddleHeight - clampedLevel;
        List<FluidStack> all = new ArrayList<>();
        if (be.hasTank()) {
            for (FluidStack fs : be.getFluids()) if (!fs.isEmpty()) all.add(fs);
            else {
                FluidStack one = be.getTankInventory().getFluid();
                if (!one.isEmpty()) all.add(one);
            }
        }
        if (all.isEmpty()) return;

        List<FluidStack> liquids = new ArrayList<>();
        int totalMb = 0;
        for (FluidStack f : all) {
            totalMb += f.getAmount();
            liquids.add(f);
        }

        if (totalMb == 0) return;

        float unit = clampedLevel / (float) totalMb;
        final float eps = 1f / 512f;

        ms.pushPose();
        ms.translate(0, clampedLevel - totalHeight, 0);

        float yCur = yBase;
        for (int i = 0; i < liquids.size(); i++) {
            FluidStack f = liquids.get(i);
            float h = Math.max(0f, f.getAmount() * unit);
            if (i == liquids.size() - 1) h = yBase + clampedLevel - yCur;

            if (h > eps) {
                boolean renderTop = i == liquids.size() - 1;
                FluidRenderHelper.renderFluidBox(f, tankHullWidth, yCur, tankHullWidth, xMax, yCur + h, zMax, bs, ms, light, false, renderTop);
                yCur += h;
            }
        }
        ms.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(CrucibleBE be) {
        return be.isController();
    }
}