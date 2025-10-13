package dev.lopyluna.slag.events;

import com.mojang.datafixers.util.Pair;
import dev.lopyluna.slag.content.blocks.basin.BasinBE;
import dev.lopyluna.slag.content.blocks.table.TableBE;
import dev.lopyluna.slag.content.items.modular_tool.BakedModularToolItem;
import dev.lopyluna.slag.register.AllRecipes;
import dev.lopyluna.slag.register.AllTags;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.fluids.FluidType;

import static dev.lopyluna.slag.SlagEmbers.MOD_ID;
import static dev.lopyluna.slag.content.blocks.basin.BasinBE.basinHandlers;
import static dev.lopyluna.slag.content.blocks.table.TableBE.tableHandlers;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ServerEvents {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        var server = event.getServer();
        setupTableHandlers(server);
        setupBasinHandlers(server);
        setupFluids(server);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Entity entity && entity.isInFluidType((type, height) -> HOT_TYPES.contains(type))) {
            entity.lavaHurt();
            entity.fallDistance *= entity.getFluidFallDistanceModifier(NeoForgeMod.LAVA_TYPE.value());
        }
    }

    private static final ObjectOpenHashSet<FluidType> HOT_TYPES = new ObjectOpenHashSet<>();
    public static void setupFluids(MinecraftServer server) {
        HOT_TYPES.clear();
        server.registryAccess().registryOrThrow(Registries.FLUID).getTag(AllTags.HOT_FLUIDS).ifPresent(tag -> tag.forEach(h -> HOT_TYPES.add(h.value().getFluidType())));
    }

    public static void setupTableHandlers(MinecraftServer server) {
        if (!tableHandlers.isEmpty()) return;
        var recipes = server.getRecipeManager().getAllRecipesFor(AllRecipes.TABLE_CASTING.get());
        if (recipes.isEmpty()) return;
        for (var holder : recipes) {
            var recipe = holder.value();
            var input = recipe.getInput();
            var fluid = input.getFluid();
            var type = recipe.getCastType();
            tableHandlers.put(Pair.of(fluid, type), new TableBE.TableHandler(fluid, input.getAmount(), type, recipe.getOutput()));
        }
    }

    public static void setupBasinHandlers(MinecraftServer server) {
        if (!basinHandlers.isEmpty()) return;
        var recipes = server.getRecipeManager().getAllRecipesFor(AllRecipes.BASIN_CASTING.get());
        if (recipes.isEmpty()) return;
        for (var holder : recipes) {
            var recipe = holder.value();
            var input = recipe.getInput();
            var fluid = input.getFluid();
            basinHandlers.put(fluid, new BasinBE.BasinHandler(fluid, input.getAmount(), recipe.getOutput()));
        }
    }

    @SubscribeEvent
    public static void onTakeItem(GrindstoneEvent.OnTakeItem event) {
        var stackA = event.getTopItem();
        var stackB = event.getBottomItem();

        if (stackA.getItem() instanceof BakedModularToolItem itemA && stackB.getItem() instanceof BakedModularToolItem itemB) {
            var partsA = itemA.getParts(stackA);
            var partsB = itemB.getParts(stackB);
            if (!partsA.equals(partsB)) event.setCanceled(true);

        }
    }
    @SubscribeEvent
    public static void OnPlaceItem(GrindstoneEvent.OnPlaceItem event) {
        var stackA = event.getTopItem();
        var stackB = event.getBottomItem();

        if (stackA.getItem() instanceof BakedModularToolItem itemA && stackB.getItem() instanceof BakedModularToolItem itemB) {
            var partsA = itemA.getParts(stackA);
            var partsB = itemB.getParts(stackB);
            if (!partsA.equals(partsB)) event.setCanceled(true);

        }
    }

    @SubscribeEvent
    public static void onAnvilChange(AnvilUpdateEvent event) {
        var stackA = event.getLeft();
        var stackB = event.getRight();

        if (stackA.getItem() instanceof BakedModularToolItem itemA && stackB.getItem() instanceof BakedModularToolItem itemB) {
            var partsA = itemA.getParts(stackA);
            var partsB = itemB.getParts(stackB);
            if (!partsA.equals(partsB)) event.setCanceled(true);

        }
    }
}
