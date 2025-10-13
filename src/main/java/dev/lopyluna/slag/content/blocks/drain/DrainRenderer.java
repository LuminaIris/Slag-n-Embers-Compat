package dev.lopyluna.slag.content.blocks.drain;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.client.FluidRenderHelper;
import dev.lopyluna.slag.content.blocks.multiblock.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class DrainRenderer extends SafeBlockEntityRenderer<DrainBE> {
    public DrainRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(DrainBE be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        var fluid = be.drainingFluid;
        if (fluid == null || fluid.isEmpty() || be.drainState == null || be.drainState == DrainState.OFF) return;
        var level = be.getLevel();
        if (level == null) return;
        BlockPos pos = be.getBlockPos();
        Vec3 start = Vec3.atCenterOf(pos).add(0, -0.5, 0);
        Vec3 end = start.add(0, -32, 0);
        var ctx = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
        var hit = level.clip(ctx);
        double yHit = hit.getType() == HitResult.Type.BLOCK ? hit.getLocation().y() : end.y;
        double fallHeight = start.y - yHit;
        float minX = 6f / 16f;
        float maxX = 10f / 16f;
        float minZ = 6f / 16f;
        float maxZ = 10f / 16f;
        float topY = 10f / 16f;
        float bottomY = (float)(-fallHeight);

        FluidRenderHelper.renderFlowingFluidBox(fluid, minX, bottomY, minZ, maxX, topY, maxZ, buffer, ms, light, true, true);
    }
}
