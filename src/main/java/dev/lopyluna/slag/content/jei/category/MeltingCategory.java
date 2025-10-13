package dev.lopyluna.slag.content.jei.category;

import com.mojang.serialization.Codec;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.blocks.melter.MeltingRecipe;
import dev.lopyluna.slag.content.jei.EmbersRecipesJEI;
import dev.lopyluna.slag.register.AllBlocks;
import dev.lopyluna.slag.register.AllTags;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.common.Internal;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

import static dev.lopyluna.slag.content.blocks.crucible_interface.client.InterfaceScreen.createLang;

@ParametersAreNonnullByDefault
public class MeltingCategory extends AbstractRecipeCategory<RecipeHolder<MeltingRecipe>> {
    private final IDrawable tankBackground;
    private final IDrawable tankOverlay;
    private final IDrawable validHeaterSlot;

    public MeltingCategory(IGuiHelper guiHelper) {
        super(EmbersRecipesJEI.MELTING.get(), Component.translatableWithFallback("gui.slag.category.melting", "Melting"), guiHelper.createDrawableItemLike(AllBlocks.MELTER), 123, 54);

        ResourceLocation backgroundTexture = SlagEmbers.loc("textures/gui/jei.png");
        this.tankBackground = guiHelper.createDrawable(backgroundTexture, 0, 0, 32, 56);
        this.tankOverlay = guiHelper.createDrawable(backgroundTexture, 32, 0, 32, 56);
        this.validHeaterSlot = guiHelper.createDrawable(backgroundTexture, 64, 0, 20, 20);
    }

    @SuppressWarnings("removal")
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<MeltingRecipe> holder, IFocusGroup focuses) {
        var recipe = holder.value();

        builder.addInputSlot(20, 1)
                .setStandardSlotBackground()
                .addIngredients(recipe.getInput());

        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 20, 38)
                .setBackground(validHeaterSlot, -2, -2)
                .addItemStacks(AllUtils.getStacksFromTag(AllTags.MELTER_HEATER));

        var fluid = getResultFluid(recipe);

        builder.addOutputSlot(83, 3)
                .setFluidRenderer(1000, false, 24, 48)
                .setOverlay(tankOverlay, -4, -4)
                .setBackground(tankBackground, -4, -4)
                .addFluidStack(fluid.getFluid(), fluid.getAmount())
                .addRichTooltipCallback((s, t) -> {
                    var tooltipFlag = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
                    t.clear();
                    var tooltips = new ArrayList<Component>();
                    tooltips.add(fluid.getDisplayName());
                    createLang(fluid, tooltips).run();
                    if (tooltipFlag.advanced()) {
                        var loc = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
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
                })
        ;
    }

    public String getFormattedModNameForModIdWithoutDisplay(IModIdHelper helper, String modId) {
        return helper.getFormattedModNameForModId(modId);
    }

    public String getFormattedModNameForModId(IModIdHelper helper, String modId) {
        if (!helper.isDisplayingModNameEnabled()) return "";
        return helper.getFormattedModNameForModId(modId);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<MeltingRecipe> holder, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(200).setPosition(49, 17);
        builder.addAnimatedRecipeFlame(999999).setPosition(21, 20);
    }


    @Override public boolean isHandled(RecipeHolder<MeltingRecipe> holder) {
        var recipe = holder.value();
        if (recipe.isSpecial()) return false;
        return !recipe.getInput().hasNoItems();
    }
    @Override public ResourceLocation getRegistryName(RecipeHolder<MeltingRecipe> recipe) {
        return recipe.id();
    }
    @Override public @NotNull Codec<RecipeHolder<MeltingRecipe>> getCodec(ICodecHelper helper, @NotNull IRecipeManager manager) {
        return helper.getRecipeHolderCodec();
    }


    public static FluidStack getResultFluid(MeltingRecipe recipe) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) throw new NullPointerException("level must not be null.");
        RegistryAccess registryAccess = level.registryAccess();
        return recipe.getResultFluid(registryAccess);
    }
}
