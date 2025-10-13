package dev.lopyluna.slag.content.blocks.basin;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.blocks.smart.SmartBlock;
import dev.lopyluna.slag.content.utils.ShapeUtils;
import dev.lopyluna.slag.register.AllBETypes;
import dev.lopyluna.slag.register.AllLangs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static dev.lopyluna.slag.content.blocks.crucible.CrucibleBlock.getEmptySound;
import static dev.lopyluna.slag.content.blocks.crucible.CrucibleBlock.getFillSound;

@ParametersAreNonnullByDefault
public class BasinBlock extends SmartBlock<BasinBE> {
    public static final MapCodec<BasinBlock> CODEC = simpleCodec(BasinBlock::new);
    public BasinBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.empty());
        tooltip.add(AllLangs.tr("cast_shift_clear").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(" ").append(AllLangs.tr("cast_shift_clear.desc")).withStyle(ChatFormatting.BLUE));
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BasinBE be) || be.coolingProgress > 0) return InteractionResult.PASS;
        var shift = player.isShiftKeyDown();
        var empty = player.getMainHandItem().isEmpty();
        if (shift && empty && be.tankInventory != null && !be.tankInventory.isEmpty()) {
            be.tankInventory.drain(be.tankInventory.getFluidAmount(), IFluidHandler.FluidAction.EXECUTE);
            return InteractionResult.SUCCESS;
        }
        if (be.itemInventory == null || (player.isShiftKeyDown() && !empty)) return InteractionResult.PASS;
        var stack = be.getStack();
        if (stack.isEmpty()) return InteractionResult.PASS;
        ItemHandlerHelper.giveItemToPlayer(player, stack);
        be.itemInventory.getFirstItem().setCount(0);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (held.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level.getBlockEntity(pos) instanceof BasinBE be)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        var tank = be.getTankInventory();

        var itemHandler = held.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemHandler == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        FluidStack available = itemHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (available.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        int fillable = tank.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (fillable <= 0) return ItemInteractionResult.CONSUME;

        if (!level.isClientSide) {
            FluidStack drained = itemHandler.drain(fillable, IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                int accepted = tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                if (accepted > 0 && !player.getAbilities().instabuild) player.setItemInHand(hand, itemHandler.getContainer());

                be.sendDataImmediately();
                be.setChanged();
            }
        }
        var soundFill = getFillSound(available);
        var soundEmpty = getEmptySound(available);
        if (soundFill != null) level.playSound(null, pos, soundFill, SoundSource.BLOCKS, .5f, 1);
        if (soundEmpty != null) player.playSound(soundEmpty, .5f, 1);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        var at = level.getBlockEntity(pos);
        if (at == null || !at.hasLevel() || !(at instanceof BasinBE be)) return 0;
        return be.getLuminosity();
    }

    @Override
    public Class<BasinBE> getBlockEntityClass() {
        return BasinBE.class;
    }

    @Override
    public BlockEntityType<? extends BasinBE> getBlockEntityType() {
        return AllBETypes.BASIN.get();
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var shaper = ShapeUtils.shape(0, 0, 0, 16, 3, 16);
        var wall = ShapeUtils.shape(0, 0, 0, 16, 16, 2).forHorizontal(Direction.NORTH);
        var newShape = shaper.add(wall.get(Direction.NORTH)).add(wall.get(Direction.SOUTH)).add(wall.get(Direction.EAST)).add(wall.get(Direction.WEST));
        if (newShape != null) shaper = newShape;
        return shaper.build();
    }
}
