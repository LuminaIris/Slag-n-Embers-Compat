package dev.lopyluna.slag.content.blocks.crucible_interface.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.FluidRenderHelper;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.network.packets.SelectFluidIndexC2S;
import dev.lopyluna.slag.register.AllLangs;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InterfaceScreen extends AbstractContainerScreen<InterfaceMenu> {
    private static final ResourceLocation TEXTURE = SlagEmbers.loc("textures/gui/interface.png");
    private static final ResourceLocation TEXTURE_OVERLAY = SlagEmbers.loc("textures/gui/interface_overlay.png");

    private static final int BOX_X = 39, BOX_Y = 19, BOX_W = 97, BOX_H = 48;
    private final Level level;

    public InterfaceScreen(InterfaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        level = Minecraft.getInstance().level;
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int iX = (width - imageWidth) / 2;
        int iY = (height - imageHeight) / 2;
        g.blit(TEXTURE, iX, iY, 0, 0, imageWidth, imageHeight);
        renderFluidLayers(g, mx, my);
    }

    @SuppressWarnings("removal")
    public void renderFluidLayers(GuiGraphics g, int mx, int my) {
        if (level == null) return;
        var fluids = menu.getFluids();
        if (fluids.isEmpty()) return;

        int boxX = leftPos + BOX_X;
        int boxY = topPos  + BOX_Y;
        int boxW = BOX_W;
        int boxH = BOX_H;

        int totalMb = 0;
        for (var f : fluids) totalMb += f.getAmount();
        if (totalMb <= 0) return;

        int yTop = boxY + boxH;
        int remainingH = boxH;
        int remainingMb = totalMb;

        for (int i = 0; i < fluids.size(); i++) {
            var fs = fluids.get(i);
            int amt = fs.getAmount();
            if (amt <= 0) continue;

            // proportional height, last segment absorbs rounding
            int h = (i == fluids.size() - 1) ? remainingH : Math.max(1, (int)Math.floor((amt / (double)remainingMb) * remainingH));
            int y = yTop - h;

            // segment
            FluidRenderHelper.drawFluidBox(g, boxX, y, boxW, h, fs);

            // highlight current drain (index 0)
            //if (i == 0) g.fill(boxX, y, boxX + boxW, y + h, 0x40FFFFFF);

            yTop -= h;
            remainingH -= h;
            remainingMb -= amt;
            if (remainingH <= 0) break;
        }

        var p = g.pose();
        p.pushPose();
        RenderSystem.enableBlend();
        g.blit(TEXTURE_OVERLAY, (width - imageWidth) / 2, (height - imageHeight) / 2, 0, 0, imageWidth, imageHeight);
        RenderSystem.disableBlend();
        p.popPose();

        if (mx >= boxX && mx < boxX + boxW && my >= boxY && my < boxY + boxH) {
            int yTop2 = boxY + boxH;
            int remH = boxH, remMb = totalMb;
            for (int i = 0; i < fluids.size(); i++) {
                var fs = fluids.get(i);
                int amt = fs.getAmount();
                if (amt <= 0) continue;
                int h = (i == fluids.size() - 1) ? remH : Math.max(1, (int)Math.floor((amt / (double)remMb) * remH));
                int y = yTop2 - h;
                if (my >= y && my < yTop2) {
                    var tooltipFlag = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
                    var tooltip = new ArrayList<Component>();
                    tooltip.add(fs.getDisplayName());
                    createLang(fs, tooltip).run();
                    if (tooltipFlag.advanced()) tooltip.add(Component.literal(BuiltInRegistries.FLUID.getKey(fs.getFluid()).toString()).withStyle(ChatFormatting.DARK_GRAY));

                    g.renderTooltip(font, tooltip, Optional.empty(), mx, my);
                    break;
                }
                yTop2 -= h; remH -= h; remMb -= amt;
            }
        }
    }

    public static Runnable createLang(FluidStack fs, List<Component> tooltip) {
        var shift = !hasShiftDown();
        var amount = fs.getAmount();
        if (fs.is(AllTags.MOLTEN_METALS)) return () -> {
            if (shift) createTooltipAmounts(tooltip, amount, pNS("blocks", MelterBE.BLOCK_SIZE), pNS("ingots", MelterBE.INGOT_SIZE), pNS("nuggets", MelterBE.NUGGET_SIZE));
            else extra(amount, tooltip);
        };
        if (fs.is(AllTags.MOLTEN_GEMS)) return () -> {
            if (shift) createTooltipAmounts(tooltip, amount, pNS("blocks", MelterBE.BLOCK_SIZE), pNS("gems", MelterBE.INGOT_SIZE), pNS("shards", MelterBE.SHARD_SIZE));
            else extra(amount, tooltip);
        };
        if (fs.is(AllTags.MOLTEN_CRYSTALS)) return () -> {
            if (shift) createTooltipAmounts(tooltip, amount, pNS("blocks", MelterBE.SMALL_BLOCK_SIZE), pNS("gems", MelterBE.INGOT_SIZE), pNS("shards", MelterBE.SHARD_SIZE));
            else extra(amount, tooltip);
        };
        if (fs.is(AllTags.MOLTEN_DUSTS)) return () -> {
            if (shift) createTooltipAmounts(tooltip, amount, pNS("blocks", MelterBE.BLOCK_SIZE), pNS("dusts", MelterBE.INGOT_SIZE), pNS("grits", MelterBE.NUGGET_SIZE));
            else extra(amount, tooltip);
        };
        if (fs.is(AllTags.MOLTEN_DUSTS_SMALL)) return () -> {
            if (shift) createTooltipAmounts(tooltip, amount, pNS("blocks", MelterBE.SMALL_BLOCK_SIZE), pNS("dusts", MelterBE.INGOT_SIZE), pNS("grits", MelterBE.NUGGET_SIZE));
            else extra(amount, tooltip);
        };
        if (fs.is(AllTags.MOLTEN_BALLS)) return () -> {
            if (shift) createTooltipAmounts(tooltip, amount, pNS("blocks", MelterBE.BLOCK_SIZE), pNS("balls", MelterBE.INGOT_SIZE));
            else extra(amount, tooltip);
        };
        if (fs.is(AllTags.MOLTEN_BALLS_SMALL)) return () -> {
            if (shift) createTooltipAmounts(tooltip, amount, pNS("blocks", MelterBE.SMALL_BLOCK_SIZE), pNS("balls", MelterBE.INGOT_SIZE));
            else extra(amount, tooltip);
        };
        return () -> extra(amount, tooltip);
    }

    @SafeVarargs
    public static void createTooltipAmounts(List<Component> tooltip, int amount, Pair<String, Integer>... pairs) {
        final int n = pairs.length;
        String[] keys = new String[n];
        int[] sizes   = new int[n + 1];

        for (int i = 0; i < n; i++) {
            var p = Objects.requireNonNull(pairs[i], "pair");
            keys[i]  = Objects.requireNonNull(p.getFirst(), "name");
            sizes[i] = Objects.requireNonNull(p.getSecond(), "size");
            if (sizes[i] <= 0) throw new IllegalArgumentException("size must be > 0");
            if (i > 0 && sizes[i] >= sizes[i - 1]) throw new IllegalArgumentException("sizes must be strictly descending");
        }
        sizes[n] = 1; // mB
        int[] m = breakdown(amount, sizes); // m[0..n-1]=units, m[n]=mB
        for (int i = 0; i < n; i++) AllLangs.trAmounts(tooltip, keys[i], m[i], true);
        AllLangs.trAmounts(tooltip, "mb", m[n], false);
    }

    public static Pair<String, Integer> pNS(String name, int size) {
        return Pair.of(name, size);
    }

    private static int[] breakdown(int amount, int... sizes) {
        int[] out = new int[sizes.length];
        int rem = amount;
        for (int i = 0; i < sizes.length; i++) {
            int v = sizes[i];
            if (v <= 0) throw new IllegalArgumentException("size must be > 0");
            out[i] = rem / v;
            rem    = rem % v;
        }
        return out;
    }

    public static void extra(int amount, List<Component> tooltip) {
        var b = (int) ((float) amount / 1000f);
        var mb = amount - (b*1000);
        AllLangs.trAmounts(tooltip, "buckets", b, true);
        AllLangs.trAmounts(tooltip, "mb", mb, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (level == null || !menu.isCrucible()) return super.mouseClicked(mx, my, button);
        if (button == 0) {
            int boxX = leftPos + BOX_X;
            int boxY = topPos  + BOX_Y;
            int boxH = BOX_H;

            if (mx >= boxX && mx < boxX + BOX_W && my >= boxY && my < boxY + boxH) {
                var fluids = menu.getFluids();
                if (!fluids.isEmpty()) {
                    int totalMb = 0; for (var f : fluids) totalMb += f.getAmount();
                    if (totalMb > 0) {
                        int yTop = boxY + boxH, remH = boxH, remMb = totalMb;
                        for (int i = 0; i < fluids.size(); i++) {
                            var fs = fluids.get(i);
                            int amt = fs.getAmount();
                            if (amt <= 0) continue;
                            int h = (i == fluids.size() - 1) ? remH : Math.max(1, (int)Math.floor((amt / (double)remMb) * remH));
                            int y = yTop - h;
                            if (my >= y && my < yTop && i != 0) {
                                var type = fs.getFluidType();
                                Optional.ofNullable(type.getSound(SoundActions.BUCKET_FILL)).ifPresent(s -> menu.player.playSound(s, 0.5f, 1f));
                                Optional.ofNullable(type.getSound(SoundActions.BUCKET_EMPTY)).ifPresent(s -> menu.player.playSound(s, 0.5f, 1f));
                                PacketDistributor.sendToServer(new SelectFluidIndexC2S(menu.getControllerPos(), i));
                                return true;
                            }
                            yTop -= h; remH -= h; remMb -= amt;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        var p = gui.pose();
        p.pushPose();
        RenderSystem.enableBlend();
        renderTooltip(gui, mouseX, mouseY);
        RenderSystem.disableBlend();
        p.popPose();
    }
}
