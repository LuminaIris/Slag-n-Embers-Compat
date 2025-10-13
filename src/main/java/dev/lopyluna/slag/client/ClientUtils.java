package dev.lopyluna.slag.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class ClientUtils {

    public static void renderFluidShape(FluidStack fluid, VoxelShape shape, MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
        shape.optimize().forAllBoxes((x1, y1, z1, x2, y2, z2) -> NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluid, (float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2, buffer, ms, light, renderBottom, invertGasses));
    }

    public static void renderFakeSlot(GuiGraphics guiGraphics, ItemStack itemstack, int x, int y, @Nullable String countString, int imageWidth, Font font) {
        int j1 = x + y * imageWidth;
        guiGraphics.renderFakeItem(itemstack, x, y, j1);
        guiGraphics.renderItemDecorations(font, itemstack, x, y, countString);
    }
}
