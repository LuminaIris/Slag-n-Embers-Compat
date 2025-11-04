package dev.lopyluna.slag.content.jei;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.forge.DoubleSmeltingRecipe;
import dev.lopyluna.slag.content.blocks.forge.client.ForgeMenu;
import dev.lopyluna.slag.content.blocks.forge.client.ForgeScreen;
import dev.lopyluna.slag.content.blocks.melter.MeltingRecipe;
import dev.lopyluna.slag.content.blocks.melter.client.MelterMenu;
import dev.lopyluna.slag.content.blocks.melter.client.MelterScreen;
import dev.lopyluna.slag.content.items.modular.DataDynamicParts;
import dev.lopyluna.slag.content.jei.category.DoubleSmeltingCategory;
import dev.lopyluna.slag.content.jei.category.MeltingCategory;
import dev.lopyluna.slag.register.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.plugins.vanilla.crafting.CategoryRecipeValidator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
@JeiPlugin
public class EmbersJEI implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return SlagEmbers.loc("main");
    }

    @Nullable private IRecipeCategory<RecipeHolder<DoubleSmeltingRecipe>> forgeCategory;
    @Nullable private IRecipeCategory<RecipeHolder<MeltingRecipe>> melterCategory;

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var jeiHelpers = registration.getJeiHelpers();
        var guiHelper = jeiHelpers.getGuiHelper();

        registration.addRecipeCategories(forgeCategory = new DoubleSmeltingCategory(guiHelper));
        registration.addRecipeCategories(melterCategory = new MeltingCategory(guiHelper));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        ErrorUtil.checkNotNull(forgeCategory, "furnaceCategory");
        ErrorUtil.checkNotNull(melterCategory, "melterCategory");
        var ingredientManager = registration.getIngredientManager();
        var level = Minecraft.getInstance().level;
        if (level == null) return;


        registration.addRecipes(EmbersRecipesJEI.DOUBLE_SMELTING.get(), getBrickForgeRecipes(forgeCategory, level, ingredientManager));
        registration.addRecipes(EmbersRecipesJEI.MELTING.get(), getMelterRecipes(melterCategory, level, ingredientManager));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(ForgeScreen.class, 78, 32, 28, 23, EmbersRecipesJEI.DOUBLE_SMELTING.get(), RecipeTypes.FUELING);
        registration.addRecipeClickArea(MelterScreen.class, 78, 32, 28, 23, EmbersRecipesJEI.MELTING.get());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(ForgeMenu.class, AllMenuTypes.FORGE.get(), EmbersRecipesJEI.DOUBLE_SMELTING.get(), 0, 2, 4, 36);
        registration.addRecipeTransferHandler(MelterMenu.class, AllMenuTypes.MELTER.get(), EmbersRecipesJEI.MELTING.get(), 0, 1, 1, 36);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(AllBlocks.FORGE, RecipeTypes.FUELING);
        registration.addRecipeCatalyst(AllBlocks.FORGE, EmbersRecipesJEI.DOUBLE_SMELTING.get());
        registration.addRecipeCatalyst(AllBlocks.MELTER, EmbersRecipesJEI.MELTING.get());
    }

    public List<RecipeHolder<DoubleSmeltingRecipe>> getBrickForgeRecipes(IRecipeCategory<RecipeHolder<DoubleSmeltingRecipe>> forgeCategory, ClientLevel level, IIngredientManager manager) {
        CategoryRecipeValidator<DoubleSmeltingRecipe> validator = new CategoryRecipeValidator<>(forgeCategory, manager, 1);
        return getValidHandledRecipes(level.getRecipeManager(), AllRecipes.DOUBLE_SMELTING.get(), validator);
    }
    public List<RecipeHolder<MeltingRecipe>> getMelterRecipes(IRecipeCategory<RecipeHolder<MeltingRecipe>> forgeCategory, ClientLevel level, IIngredientManager manager) {
        CategoryRecipeValidator<MeltingRecipe> validator = new CategoryRecipeValidator<>(forgeCategory, manager, 1);
        return getValidHandledRecipes(level.getRecipeManager(), AllRecipes.MELTING.get(), validator);
    }
    private static <C extends RecipeInput, T extends Recipe<C>> List<RecipeHolder<T>> getValidHandledRecipes(RecipeManager recipeManager, RecipeType<T> recipeType, CategoryRecipeValidator<T> validator) {
        return recipeManager.getAllRecipesFor(recipeType).stream().filter(validator::isRecipeHandled).toList();
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration reg) {
        reg.registerSubtypeInterpreter(AllItems.DYNAMIC_PART.get(), EmbersSubtypeInterpreters.PART_INSTANCE);
        reg.registerSubtypeInterpreter(AllItems.MODULAR_ITEM.get(), EmbersSubtypeInterpreters.MODULAR_INSTANCE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime rt) {
        var im = rt.getIngredientManager();

        var variants = new ArrayList<ItemStack>();

        var materials = AllDynamicTypes.getAllMaterials().stream()
                .sorted(Comparator.comparingInt(type -> type.sortOrder)).toList();
        var parts = AllDynamicTypes.getAllParts().stream()
                .sorted(Comparator.comparingInt(type -> type.sortOrder)).toList();
        var modulars = AllDynamicTypes.getAllModulars().stream()
                .sorted(Comparator.comparingInt(type -> type.sortOrder)).toList();

        for (var material : materials) for (var part : parts) {
            var item = AllItems.DYNAMIC_PART.get();
            var stack = item.getDefaultInstance();

            item.setMaterialType(stack, material);
            item.setPartType(stack, part);

            variants.add(stack);
        }

        for (var material : materials) for (var modular : modulars) {
            var result = modular.getResultStack();
            if (!result.isEmpty()) continue;
            var baseTool = AllItems.MODULAR_ITEM.asStack();
            var toolParts = new ArrayList<ItemStack>();
            for (var part : AllDynamicTypes.getAllPartsFromModular(modular)) {
                var dynamicPart = AllItems.DYNAMIC_PART.get();
                var stack = dynamicPart.getDefaultInstance();
                dynamicPart.setMaterialType(stack, material);
                dynamicPart.setPartType(stack, part);
                stack.set(AllDataComponents.BUILT, modular.id);
                toolParts.add(stack);
            }

            if (modular.finalSegmentStacks != null && !modular.finalSegmentStacks.isEmpty()) toolParts.addAll(modular.finalSegmentStacks);

            if (material.fireProof) baseTool.set(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);

            baseTool.set(AllDataComponents.DYNAMIC_PARTS, new DataDynamicParts(toolParts));
            baseTool.set(AllDataComponents.BAKED, modular.id);
            baseTool.set(AllDataComponents.MODULAR_TYPE, modular.id);

            variants.add(baseTool);
        }

        im.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, variants);
    }
}
