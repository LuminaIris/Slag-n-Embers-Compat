package dev.lopyluna.slag.content.blocks.crucible;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.config.SlagServerConfigs;
import dev.lopyluna.slag.content.blocks.multiblock.connectivity.ConnectivityHandler;
import dev.lopyluna.slag.content.blocks.smart.SmartBlock;
import dev.lopyluna.slag.content.utils.ShapeUtils;
import dev.lopyluna.slag.register.AllBETypes;
import dev.lopyluna.slag.register.AllLangs;
import dev.lopyluna.slag.register.AllSoundTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static dev.lopyluna.slag.content.blocks.crucible.CrucibleBE.*;

@ParametersAreNonnullByDefault
public class CrucibleBlock extends SmartBlock<CrucibleBE> {
    public static final MapCodec<CrucibleBlock> CODEC = simpleCodec(CrucibleBlock::new);

    public CrucibleBlock(Properties properties) {
        super(properties.forceSolidOn());
        registerDefaultState(super.defaultBlockState().setValue(WINDOW, false).setValue(TOP, true).setValue(BOTTOM, true).setValue(SHAPE, Shape.PLAIN));
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltips, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pContext, pTooltips, pFlag);
        pTooltips.add(AllLangs.tr("dynamic_multiblock").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Class<CrucibleBE> getBlockEntityClass() {
        return CrucibleBE.class;
    }

    @Override
    public @NotNull BlockEntityType<CrucibleBE> getBlockEntityType() {
        return AllBETypes.CRUCIBLE.get();
    }
    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.getBlockEntity(pos) instanceof CrucibleBE cbe && cbe.getControllerBE() instanceof CrucibleBE be) {
            if (entity instanceof LivingEntity living) {
                var tank = be.getTankInventory();

                if (!living.getActiveEffects().isEmpty() && tank.containsLiquid(NeoForgeMod.MILK.get())) living.removeEffectsCuredBy(EffectCures.MILK);
                if (tank.containsHotLiquid()) living.lavaHurt();
                else if (living.isOnFire() && tank.canExtinguishEntity(living)) {
                    living.extinguishFire();
                    entity.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
                }

            }
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (level.getBlockEntity(pos) instanceof CrucibleBE cbe && cbe.getControllerBE() instanceof CrucibleBE be) {
            var fluids = be.getTankInventory().getFluids();
            if (fluids == null || fluids.isEmpty()) return;
            var totalSize = be.getTotalSize();
            var fillState = be.getFillState();
            for (var fluid : fluids) if (random.nextInt(fluids.size() * totalSize) <= totalSize * 0.25f * fillState) {
                if ((fluid.is(Fluids.WATER) || fluid.is(NeoForgeMod.MILK)) && random.nextInt(96) == 0) level.playLocalSound((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false);
                fluid.getFluid().defaultFluidState().animateTick(level, pos, random);
            }
        }
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return !state.getValue(BOTTOM) && !entity.isDescending();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WINDOW, TOP, BOTTOM, SHAPE);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        if (oldState.getBlock() == state.getBlock()) return;
        if (moved) return;
        withBlockEntityDo(level, pos, CrucibleBE::updateConnectivity);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        CrucibleBE at = ConnectivityHandler.partAt(getBlockEntityType(), world, pos);
        if (at == null || !at.hasLevel()) return 0;
        var be = at.getControllerBE();
        if (be == null || !be.isWindow()) return 0;
        return at.getLuminosity();
    }
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            if (!(level.getBlockEntity(pos) instanceof CrucibleBE tankBE)) return;
            level.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(tankBE);
        }
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.getMainHandItem().isEmpty() && player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof CrucibleBE be) {
            var ctrl = be.getControllerBE();
            if (ctrl == null) ctrl = be;
            ctrl.setWindows(!ctrl.isWindow());
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!SlagServerConfigs.INSERT_FLUID_ITEM_INTO_CRUCIBLE.get()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level.getBlockEntity(pos) instanceof CrucibleBE be)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        var ctrl = be.getControllerBE();
        if (ctrl == null) ctrl = be;
        var tank = ctrl.getTankInventory();

        var itemHandler = held.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        FluidStack available = itemHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (available.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        int fillable = tank.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (fillable != available.getAmount()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (!level.isClientSide) {
            FluidStack drained = itemHandler.drain(fillable, IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                int accepted = tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                if (accepted > 0 && !player.getAbilities().instabuild) player.setItemInHand(hand, itemHandler.getContainer());

                ctrl.sendDataImmediately();
                ctrl.setChanged();
            }
        }
        var soundFill = getFillSound(available);
        var soundEmpty = getEmptySound(available);
        if (soundFill != null) level.playSound(null, pos, soundFill, SoundSource.BLOCKS, .5f, 1);
        if (soundEmpty != null) player.playSound(soundEmpty, .5f, 1);
        return ItemInteractionResult.SUCCESS;
    }


    public static SoundEvent getFillSound(FluidStack fluid) {
        SoundEvent soundevent = fluid.getFluid().getFluidType().getSound(fluid, SoundActions.BUCKET_FILL);
        if (soundevent == null) soundevent = isTag(fluid, FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
        return soundevent;
    }
    public static SoundEvent getEmptySound(FluidStack fluid) {
        SoundEvent soundevent = fluid.getFluid().getFluidType().getSound(fluid, SoundActions.BUCKET_EMPTY);
        if (soundevent == null) soundevent = isTag(fluid, FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        return soundevent;
    }
    @SuppressWarnings("deprecation")
    public static boolean isTag(Fluid fluid, TagKey<Fluid> tag) {
        return fluid.is(tag);
    }
    public static boolean isTag(FluidStack fluid, TagKey<Fluid> tag) {
        return isTag(fluid.getFluid(), tag);
    }

    @Override
    protected @NotNull VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context.equals(CollisionContext.empty())) return Shapes.block();
        return state.getValue(WINDOW) ? Shapes.empty() : super.getVisualShape(state, level, pos, context);
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Boolean bottom = state.getValue(BOTTOM);
        Shape shape = state.getValue(SHAPE);
        var shaper = ShapeUtils.shape(Shapes.empty());
        if (bottom) shaper = shaper.add(0, 0, 0, 16, 4, 16);
        var wall = ShapeUtils.shape(0, 0, 0, 16, 16, 3).forHorizontal(Direction.NORTH);

        var newShape = switch (shape) {
            case PLAIN -> shaper.add(wall.get(Direction.NORTH)).add(wall.get(Direction.SOUTH)).add(wall.get(Direction.EAST)).add(wall.get(Direction.WEST));
            case NW -> shaper.add(wall.get(Direction.NORTH)).add(wall.get(Direction.WEST));
            case SW -> shaper.add(wall.get(Direction.SOUTH)).add(wall.get(Direction.WEST));
            case NE -> shaper.add(wall.get(Direction.NORTH)).add(wall.get(Direction.EAST));
            case SE -> shaper.add(wall.get(Direction.SOUTH)).add(wall.get(Direction.EAST));
            case NORTH -> shaper.add(wall.get(Direction.NORTH));
            case SOUTH -> shaper.add(wall.get(Direction.SOUTH));
            case WEST -> shaper.add(wall.get(Direction.WEST));
            case EAST -> shaper.add(wall.get(Direction.EAST));
            default -> null;
        };
        if (newShape != null) shaper = newShape;
        return shaper.build();
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    public static final SoundType SILENCED_SOUND = new DeferredSoundType(0.1F, 1.5F,
            AllSoundTypes.CRUCIBLE::getBreakSound,
            AllSoundTypes.CRUCIBLE::getStepSound,
            AllSoundTypes.CRUCIBLE::getPlaceSound,
            AllSoundTypes.CRUCIBLE::getHitSound,
            AllSoundTypes.CRUCIBLE::getFallSound
    );

    @Override
    public @NotNull SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        var soundType = super.getSoundType(state, world, pos, entity);
        if (entity instanceof Player && entity.getPersistentData().contains("SilencePlacingSound")) return SILENCED_SOUND;
        return soundType;
    }
}
