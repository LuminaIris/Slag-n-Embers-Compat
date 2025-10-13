package dev.lopyluna.slag.content.blocks.melter.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.FluidRenderHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;

import static dev.lopyluna.slag.client.ClientUtils.renderFakeSlot;
import static dev.lopyluna.slag.content.blocks.crucible_interface.client.InterfaceScreen.createLang;

public class MelterScreen extends AbstractContainerScreen<MelterMenu> {
    static Minecraft mc = Minecraft.getInstance();
    private static final ResourceLocation TEXTURE = SlagEmbers.loc("textures/gui/melter.png");
    private static final ResourceLocation TEXTURE_OVERLAY = SlagEmbers.loc("textures/gui/melter_overlay.png");

    private static final int BOX_X = 112, BOX_Y = 19, BOX_W = 24, BOX_H = 48;
    private final Level level;

    public MelterScreen(MelterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        level = Minecraft.getInstance().level;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        var p = graphics.pose();

        p.pushPose();
        RenderSystem.enableBlend();
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        renderFluidLayers(graphics, mouseX, mouseY);
        RenderSystem.disableBlend();
        p.popPose();

        var melting = menu.isMelting();

        if (melting.getSecond()) graphics.blit(TEXTURE, leftPos + 57, topPos + 36, 176, 0, 14, 14);
        if (melting.getFirst() == -1f) return;
        int cook = Mth.ceil(melting.getFirst() * 24.0F);
        graphics.blit(TEXTURE, leftPos + 79, topPos + 34, 176, 14, cook, 16);
    }


    @Override
    public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        //renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);
        var p = gui.pose();
        var state = menu.getBelowState();
        if (!state.isAir()) {
            var data = menu.getBelowData();
            var stack = data.getFirst();
            if (stack.isEmpty()) return;
            stack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
            stack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
            var heatable = data.getSecond();
            var l = leftPos + 56;
            var t = topPos + 54;
            if (heatable) gui.blit(TEXTURE, leftPos + 54, topPos + 52, 176, 31, 20, 20);
            renderFakeSlot(gui, stack, l, t, heatable ? "✔" : "↓", this.imageWidth, this.font);
            if (mouseX >= l && mouseX < l + 18 && mouseY >= t && mouseY < t + 18) {
                var tooltipFlag = mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
                var tooltip = new ArrayList<Component>();

                tooltip.add(stack.getHoverName());
                if (tooltipFlag.advanced()) tooltip.add(Component.literal(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString()).withStyle(ChatFormatting.DARK_GRAY));

                gui.renderTooltip(font, tooltip, stack.getTooltipImage(), mouseX, mouseY);
            }
        }
        p.pushPose();
        RenderSystem.enableBlend();
        renderTooltip(gui, mouseX, mouseY);
        RenderSystem.disableBlend();
        p.popPose();
    }

    @Override
    protected void init() {
        super.init();

        titleLabelX = (imageWidth - font.width(title)) / 2;
    }


    @SuppressWarnings("removal")
    public void renderFluidLayers(GuiGraphics g, int mx, int my) {
        if (level == null) return;
        var fluid = menu.getFluid();
        if (fluid.isEmpty()) return;
        var capacity = menu.getCapacity();
        if (capacity <= 0) return;
        int amount = fluid.getAmount();
        if (amount <= 0) return;
        float fillState = Mth.clamp((float) amount / (float) capacity, 0f, 1f);

        int boxX = leftPos + BOX_X;
        int boxY = topPos  + BOX_Y;
        int boxW = BOX_W;

        int h = (int) (49f * fillState);
        int y = boxY + (int) (49f * (1f - fillState));

        FluidRenderHelper.drawFluidBox(g, boxX, y, boxW, h, fluid);

        var p = g.pose();
        p.pushPose();
        RenderSystem.enableBlend();
        g.blit(TEXTURE_OVERLAY, (width - imageWidth) / 2, (height - imageHeight) / 2, 0, 0, imageWidth, imageHeight);
        RenderSystem.disableBlend();
        p.popPose();

        if (mx >= boxX && mx < boxX + boxW && my >= y && my < y + h) {
            var tooltipFlag = mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
            var tooltip = new ArrayList<Component>();
            tooltip.add(fluid.getDisplayName());
            createLang(fluid, tooltip).run();
            if (tooltipFlag.advanced()) tooltip.add(Component.literal(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString()).withStyle(ChatFormatting.DARK_GRAY));

            g.renderTooltip(font, tooltip, Optional.empty(), mx, my);
        }
    }
}
