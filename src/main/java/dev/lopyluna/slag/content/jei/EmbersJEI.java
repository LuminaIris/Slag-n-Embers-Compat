package dev.lopyluna.slag.content.jei;

import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.blocks.forge.DoubleSmeltingRecipe;
import dev.lopyluna.slag.content.blocks.forge.client.ForgeMenu;
import dev.lopyluna.slag.content.blocks.forge.client.ForgeScreen;
import dev.lopyluna.slag.content.blocks.melter.MeltingRecipe;
import dev.lopyluna.slag.content.blocks.melter.client.MelterMenu;
import dev.lopyluna.slag.content.blocks.melter.client.MelterScreen;
import dev.lopyluna.slag.content.items.modular_tool.DataToolParts;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.lopyluna.slag.register.AllCreativeTabs.getToolMixture;
import static dev.lopyluna.slag.register.AllCreativeTabs.testRodCount;
import static dev.lopyluna.slag.register.AllItems.MATERIAL_TYPES;

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
        //registration.addRecipeCategories(new SieveRecipeCategory(guiHelper));
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
        //Map<Ingredient, List<SieveRecipe>> grouped = new HashMap<>();
        //AllSieveProviders.register();
        //for (SieveRecipe recipe : SieveBlock.recipes) grouped.computeIfAbsent(recipe.ingredient, i -> new ArrayList<>()).add(recipe);
        //List<SieveRecipeJEI> recipes = grouped.entrySet().stream().map(entry -> new SieveRecipeJEI(entry.getKey(), entry.getValue())).toList();
        //registration.addRecipes(EmbersRecipesJEI.SIFTING, recipes);
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

    @SuppressWarnings("removal")
    @Override
    public void registerItemSubtypes(ISubtypeRegistration reg) {
        reg.registerSubtypeInterpreter(
                VanillaTypes.ITEM_STACK,
                AllItems.BAKED_TOOL.get(),
                (stack, ctx) -> {
                    var data = stack.get(AllDataComponents.TOOL_PARTS);
                    if (data == null || data.isEmpty()) return mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter.NONE;

                    var sb = new StringBuilder();
                    for (var s : data.itemsCopy()) {
                        if (s.isEmpty()) continue;
                        var id = BuiltInRegistries.ITEM.getKey(s.getItem());
                        sb.append(id).append('#').append(s.getCount()).append(';');
                    }
                    return sb.toString();
                }
        );
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime rt) {
        var im = rt.getIngredientManager();
        var variants = new java.util.ArrayList<ItemStack>();

        for (var material : MATERIAL_TYPES) getToolMixture(material).forEach((tool, parts) -> {
            if (parts.isEmpty()) return;
            var toolStack = AllItems.BAKED_TOOL.asStack();
            var list = net.minecraft.core.NonNullList.<ItemStack>create();
            list.addAll(parts);
            var rod = Items.STICK.getDefaultInstance();
            rod.setCount(testRodCount(tool));
            list.add(rod);
            toolStack.set(AllDataComponents.TOOL_PARTS, new DataToolParts(list));
            variants.add(toolStack);
        });
        im.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, variants);
    }
}
