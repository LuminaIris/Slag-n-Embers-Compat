package dev.lopyluna.slag.content.utils;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import java.util.function.ToIntFunction;

public class BlockHelper {
    public static Boolean always(BlockState state, BlockGetter blockGetter, BlockPos pos, EntityType<?> entity) {
        return true;
    }
    private static boolean always(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return true;
    }

    public static Boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos, EntityType<?> entity) {
        return false;
    }
    private static boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return false;
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> empty() {
        return (ctx, p) -> {};
    }

    public static ToIntFunction<BlockState> litBlockEmission(int i) {
        return (blockState) -> (Boolean) blockState.getValue(BlockStateProperties.LIT) ? i : 0;
    }

    public static void genDirectional(ConfiguredModel.Builder<?> builder, BlockState state) {
        switch (state.getValue(BlockStateProperties.FACING)) {
            case DOWN -> builder.rotationX(90);
            case UP -> builder.rotationX(270);
            case NORTH -> {}
            case SOUTH -> builder.rotationY(180);
            case WEST -> builder.rotationY(270);
            case EAST -> builder.rotationY(90);
        }
    }

    public static void genHorizontalDirectional(ConfiguredModel.Builder<?> builder, BlockState state) {
        var dir = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) :
                state.hasProperty(BlockStateProperties.FACING) ? state.getValue(BlockStateProperties.FACING) :
                state.hasProperty(BlockStateProperties.FACING_HOPPER) ? state.getValue(BlockStateProperties.FACING_HOPPER) :
                Direction.NORTH;
        switch (dir) {
            case SOUTH -> builder.rotationY(180);
            case WEST -> builder.rotationY(270);
            case EAST -> builder.rotationY(90);
            default -> {}
        }
    }

    public static void genHorizontalDirectional(DataGenContext<Block, ? extends Block> c, RegistrateBlockstateProvider p) {
        p.getVariantBuilder(c.get()).forAllStates(state -> {
            var builder = ConfiguredModel.builder();
            builder.modelFile(p.models().getExistingFile(p.modLoc("block/" + c.getName())));
            genHorizontalDirectional(builder, state);
            return builder.build();
        });
    }

    public static ModelFile getExistingModel(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov, String... suffix) {
        StringBuilder string = new StringBuilder("/block");
        for (var suf : suffix) if (!suf.isEmpty()) string.append("_").append(suf);
        final String location = "block/" + ctx.getName() + string;
        return prov.models().getExistingFile(prov.modLoc(location.contains("empty") ? "block/" + ctx.getName() + "/empty" : location));
        //to generate all the models with correct naming
        //return prov.models().withExistingParent(prov.modLoc(location.contains("empty") ? "block/" + ctx.getName() + "/empty" : location).toString(), prov.modLoc("block/" + ctx.getName() + "/empty"));
    }
}
