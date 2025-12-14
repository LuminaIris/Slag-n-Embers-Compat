package dev.lopyluna.slag.content.jei.category;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.crucible.AlloyingRecipe;
import dev.lopyluna.slag.content.jei.EmbersRecipesJEI;
import dev.lopyluna.slag.register.AllBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.common.Internal;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

import static dev.lopyluna.slag.content.blocks.crucible_interface.client.InterfaceScreen.createLang;

@ParametersAreNonnullByDefault
public class AlloyingCategory extends AbstractRecipeCategory<RecipeHolder<AlloyingRecipe>> {
    private final IDrawable tankBackground;
    private final IDrawable tankOverlay;

    public AlloyingCategory(IGuiHelper guiHelper) {
        super(
                EmbersRecipesJEI.ALLOYING.get(),
                Component.translatableWithFallback("gui.slag.category.alloying", "Alloying"),
                guiHelper.createDrawableItemLike(AllBlocks.BASIN),
                123, 54);

        ResourceLocation backgroundTexture = SlagEmbers.loc("textures/gui/jei.png");
        this.tankBackground = guiHelper.createDrawable(backgroundTexture, 0, 0, 32, 56);
        this.tankOverlay = guiHelper.createDrawable(backgroundTexture, 32, 0, 32, 56);
    }


    @SuppressWarnings("removal")
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<AlloyingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();

        var fluidOut = recipe.getOutput();
        var fluidsIn = recipe.getInputs();

        var totalInAmount = 0;
        for (FluidStack fluidIn : fluidsIn) {
            totalInAmount += fluidIn.getAmount();
        }

        var totalCapacity = Math.max(totalInAmount, fluidOut.getAmount());

        builder.addOutputSlot(83, 3)
                .setFluidRenderer(totalCapacity, false, 24, 48)
                .setOverlay(tankOverlay, -4, -4)
                .setBackground(tankBackground, -4, -4)
                .addFluidStack(fluidOut.getFluid(), fluidOut.getAmount())
                .addRichTooltipCallback((s, t) -> {
                    var tooltipFlag = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
                    t.clear();
                    var tooltips = new ArrayList<Component>();
                    tooltips.add(fluidOut.getDisplayName());
                    createLang(fluidOut, tooltips).run();
                    if (tooltipFlag.advanced()) {
                        var loc = BuiltInRegistries.FLUID.getKey(fluidOut.getFluid());
                        tooltips.add(Component.literal(loc.toString()).withStyle(ChatFormatting.DARK_GRAY));
                        var helper = Internal.getJeiRuntime().getJeiHelpers().getModIdHelper();
                        tooltips.add(Component.literal(getFormattedModNameForModIdWithoutDisplay(helper, loc.getNamespace())).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC));
                        var name = getRegistryName(holder);
                        if (name != null) {
                            tooltips.add(Component.translatable("jei.tooltip.recipe.id", Component.literal(name.toString())).withStyle(ChatFormatting.DARK_GRAY));

                            var modID = name.getNamespace();
                            if (!modID.equals(getRecipeType().getUid().getNamespace())) {
                                var mod = getFormattedModNameForModId(helper, name.getNamespace());
                                if (!mod.isEmpty()) tooltips.add(Component.translatable("jei.tooltip.recipe.by", mod).withStyle(ChatFormatting.GRAY));
                            }
                        }
                    }
                    t.addAll(tooltips);
                });

        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 12, 3)
                .setOverlay(tankOverlay, -4, -4)
                .setBackground(tankBackground, -4, -4);

        for (int i = 0; i < fluidsIn.size(); i++) {
            var fluidIn = fluidsIn.get(i);
            var fluidAreaSize = (int) (((double) fluidIn.getAmount() / totalInAmount) * 48.0);
            SlagEmbers.LOGGER.info("Fluid area size: " + ((double) fluidIn.getAmount() / totalInAmount) * 48.0);
            builder.addInputSlot(12, 51 - ((i+1)*fluidAreaSize))
                    .setFluidRenderer(fluidIn.getAmount(), false, 24, fluidAreaSize)
                    .addFluidStack(fluidIn.getFluid(), fluidIn.getAmount())
                    .addRichTooltipCallback((s, t) -> {
                        var tooltipFlag = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
                        t.clear();
                        var tooltips = new ArrayList<Component>();
                        tooltips.add(fluidIn.getDisplayName());
                        createLang(fluidIn, tooltips).run();
                        if (tooltipFlag.advanced()) {
                            var loc = BuiltInRegistries.FLUID.getKey(fluidIn.getFluid());
                            tooltips.add(Component.literal(loc.toString()).withStyle(ChatFormatting.DARK_GRAY));
                            var helper = Internal.getJeiRuntime().getJeiHelpers().getModIdHelper();
                            tooltips.add(Component.literal(getFormattedModNameForModIdWithoutDisplay(helper, loc.getNamespace())).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC));
                            var name = getRegistryName(holder);
                            if (name != null) {
                                tooltips.add(Component.translatable("jei.tooltip.recipe.id", Component.literal(name.toString())).withStyle(ChatFormatting.DARK_GRAY));

                                var modID = name.getNamespace();
                                if (!modID.equals(getRecipeType().getUid().getNamespace())) {
                                    var mod = getFormattedModNameForModId(helper, name.getNamespace());
                                    if (!mod.isEmpty()) tooltips.add(Component.translatable("jei.tooltip.recipe.by", mod).withStyle(ChatFormatting.GRAY));
                                }
                            }
                        }
                        t.addAll(tooltips);
                    });
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<AlloyingRecipe> holder, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(200).setPosition(49, 17);
    }

    public String getFormattedModNameForModIdWithoutDisplay(IModIdHelper helper, String modId) {
        return helper.getFormattedModNameForModId(modId);
    }

    public String getFormattedModNameForModId(IModIdHelper helper, String modId) {
        if (!helper.isDisplayingModNameEnabled()) return "";
        return helper.getFormattedModNameForModId(modId);
    }
}
