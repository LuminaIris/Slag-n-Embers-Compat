package dev.lopyluna.slag.register;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.content.utils.Registration;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.lopyluna.slag.SlagEmbers.REG;

@SuppressWarnings("unused")
public class AllFluids {
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_OBSIDIAN = newMoltenFluid("Obsidian", () -> 0x0D0B12).register();

    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_AMETHYST =
            newMoltenFluid("Amethyst", () -> 0xBA8EE4).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_COPPER =
            newMoltenFluid("Copper", () -> 0xD46F4C).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_DIAMOND =
            newMoltenFluid("Diamond", () -> 0x59E0CD).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_EMERALD =
            newMoltenFluid("Emerald", () -> 0x39D66C).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_GOLD =
            newMoltenFluid("Gold", () -> 0xFBE870).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_IRON =
            newMoltenFluid("Iron", () -> 0xB8BFC4).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_LAPIS =
            newMoltenFluid("Lapis", () -> 0x325BB2).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_NETHERITE =
            newMoltenFluid("Netherite", () -> 0x585157).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_PRISMARINE =
            newMoltenFluid("Prismarine", () -> 0x86BEAF).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_QUARTZ =
            newMoltenFluid("Quartz", () -> 0xEBE4D5).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_REDSTONE =
            newMoltenFluid("Redstone", () -> 0xCB1909).register();
    public static final FluidEntry<LavaLikeFluid.Flowing> MOLTEN_ROSE_GOLD =
            newMoltenFluid("Rose Gold", () -> 0xFBA4AB).register();

    public static FluidBuilder<LavaLikeFluid.Flowing, Registration> newMoltenFluid(String type, Supplier<Integer> hexColor) {
        var name = "Molten " + type;
        String id = name.toLowerCase().replace(" ", "_");
        return standardFluidLavaLike(id, TintableFluidType.create(hexColor.get(), () -> 0.025f))
                .lang(name)
                .renderType(() -> RenderType::solid)
                .properties(b -> b
                        .lightLevel(12)
                        .viscosity(6000)
                        .density(1600)
                        .temperature(1000)
                        .canSwim(false)
                        .canDrown(false)
                        .pathType(PathType.LAVA)
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                ).fluidProperties(p -> p.levelDecreasePerBlock(2)
                        .tickRate(10)
                        .slopeFindDistance(3)
                        .explosionResistance(100f))
                .tag(AllTags.fluidC(id), FluidTags.LAVA)
                .source(LavaLikeFluid.Source::new)
                .bucket()
                .tag(AllTags.itemC("buckets/" + id))
                .build();
    }

    public static void register() {}


    public static FluidBuilder<LavaLikeFluid.Flowing, Registration> standardFluidLavaLike(String name, FluidBuilder.FluidTypeFactory typeFactory) {
        return REG.fluid(name, SlagEmbers.loc("fluid/" + name + "_still"), SlagEmbers.loc("fluid/" + name + "_flow"), typeFactory, LavaLikeFluid.Flowing::new).tag(AllTags.HOT_FLUIDS);
    }

    public static FluidBuilder<BaseFlowingFluid.Flowing, Registration> standardFluid(String name, FluidBuilder.FluidTypeFactory typeFactory) {
        return REG.fluid(name, SlagEmbers.loc("fluid/" + name + "_still"), SlagEmbers.loc("fluid/" + name + "_flow"), typeFactory);
    }

    @ParametersAreNonnullByDefault
    private abstract static class LavaLikeFluid extends BaseFlowingFluid {
        protected LavaLikeFluid(Properties properties) {
            super(properties);
        }

        public void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
            var above = pos.above();
            if (level.getBlockState(above).isAir() && !level.getBlockState(above).isSolidRender(level, above)) {
                if (random.nextInt(100) == 0) {
                    double d0 = (double)pos.getX() + random.nextDouble(), d1 = (double)pos.getY() + (double)1.0F, d2 = (double)pos.getZ() + random.nextDouble();
                    level.playLocalSound(d0, d1, d2, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
                }
                if (random.nextInt(200) == 0) level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }
        }

        @SuppressWarnings("deprecation")
        public void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
            if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
                int i = random.nextInt(3);
                if (i > 0) {
                    var relPos = pos;
                    for (int j = 0; j < i; ++j) {
                        relPos = relPos.offset(random.nextInt(3) - 1, 1, random.nextInt(3) - 1);
                        if (!level.isLoaded(relPos)) return;

                        var relState = level.getBlockState(relPos);
                        if (relState.isAir()) {
                            if (hasFlammableNeighbours(level, relPos)) {
                                level.setBlockAndUpdate(relPos, EventHooks.fireFluidPlaceBlockEvent(level, relPos, pos, BaseFireBlock.getState(level, relPos)));
                                return;
                            }
                        } else if (relState.blocksMotion()) return;
                    }
                } else for (int k = 0; k < 3; ++k) {
                    var offset = pos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
                    if (!level.isLoaded(offset)) return;
                    if (level.isEmptyBlock(offset.above()) && isFlammable(level, offset, Direction.UP)) level.setBlockAndUpdate(offset.above(), EventHooks.fireFluidPlaceBlockEvent(level, offset.above(), pos, BaseFireBlock.getState(level, offset)));
                }
            }
        }

        private boolean hasFlammableNeighbours(LevelReader level, BlockPos pos) {
            for (var dir : Direction.values()) if (isFlammable(level, pos.relative(dir), dir.getOpposite())) return true;
            return false;
        }

        @SuppressWarnings("deprecation")
        private boolean isFlammable(LevelReader pLevel, BlockPos pPos, Direction pFace) {
            if (pPos.getY() >= pLevel.getMinBuildHeight() && pPos.getY() < pLevel.getMaxBuildHeight() && !pLevel.hasChunkAt(pPos)) return false;
            var state = pLevel.getBlockState(pPos);
            return state.ignitedByLava() && state.isFlammable(pLevel, pPos, pFace);
        }

        protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
            this.fizz(level, pos);
        }

        private void fizz(LevelAccessor level, BlockPos pos) {
            level.levelEvent(1501, pos, 0);
        }

        @Override
        protected boolean isRandomlyTicking() {
            return true;
        }

        public static class Flowing extends LavaLikeFluid {
            protected Flowing(Properties properties) { super(properties); this.registerDefaultState(this.getStateDefinition().any().setValue(LEVEL, 7)); }
            @Override protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) { super.createFluidStateDefinition(builder); builder.add(LEVEL); }
            @Override public boolean isSource(FluidState fluidState) { return false; }
            @Override public int getAmount(FluidState fluidState) { return fluidState.getValue(LEVEL); }
        }
        public static class Source extends LavaLikeFluid {
            protected Source(Properties properties) { super(properties); }
            @Override public boolean isSource(FluidState fluidState) { return true; }
            @Override public int getAmount(FluidState fluidState) { return 8; }
        }
    }

    @ParametersAreNonnullByDefault
    private static class TintableFluidType extends AllFluids.TintedFluidType {
        private Vector3f fogColor;
        private Supplier<Float> fogDistance;

        public static FluidBuilder.FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance) {
            return (p, s, f) -> {
                var fluidType = new TintableFluidType(p, s, f);
                fluidType.fogColor = new Color(fogColor, false).asVectorF();
                fluidType.fogDistance = fogDistance;
                return fluidType;
            };
        }

        private TintableFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) { super(properties, stillTexture, flowingTexture); }


        @Override protected int getTintColor(FluidStack stack) {
            return NO_TINT;
        }
        @Override public int getTintColor(FluidState state, BlockAndTintGetter world, BlockPos pos) {
            return 0x00ffffff;
        }
        @Override protected Vector3f getCustomFogColor() {
            return fogColor;
        }
        @Override protected float getFogDistanceModifier() {
            return fogDistance.get();
        }

    }


    @SuppressWarnings("removal")
    @ParametersAreNonnullByDefault
    public static abstract class TintedFluidType extends FluidType {

        protected static final int NO_TINT = 0xffffffff;
        private final ResourceLocation stillTexture;
        private final ResourceLocation flowingTexture;

        public TintedFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
            super(properties);
            this.stillTexture = stillTexture;
            this.flowingTexture = flowingTexture;
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override public @NotNull ResourceLocation getStillTexture() { return stillTexture; }
                @Override public @NotNull ResourceLocation getFlowingTexture() { return flowingTexture; }
                @Override public int getTintColor(FluidStack stack) { return TintedFluidType.this.getTintColor(stack); }
                @Override public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) { return TintedFluidType.this.getTintColor(state, getter, pos); }

                @Override
                public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                    var customFogColor = TintedFluidType.this.getCustomFogColor();
                    return customFogColor == null ? fluidFogColor : customFogColor;
                }

                @Override
                public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                    float modifier = TintedFluidType.this.getFogDistanceModifier();
                    float baseWaterFog = 96.0f;
                    if (modifier != 1f) {
                        RenderSystem.setShaderFogShape(FogShape.CYLINDER);
                        RenderSystem.setShaderFogStart(-8);
                        RenderSystem.setShaderFogEnd(baseWaterFog * modifier);
                    }
                }
            });
        }
        protected abstract int getTintColor(FluidStack stack);
        protected abstract int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos);
        protected Vector3f getCustomFogColor() {
            return null;
        }
        protected float getFogDistanceModifier() {
            return 1f;
        }
    }
}
