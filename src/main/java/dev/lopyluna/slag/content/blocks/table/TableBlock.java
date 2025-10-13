package dev.lopyluna.slag.content.blocks.table;

import com.mojang.serialization.MapCodec;
import dev.lopyluna.slag.content.blocks.smart.SmartBlock;
import dev.lopyluna.slag.content.items.dynamic_mold.DynamicMoldItem;
import dev.lopyluna.slag.content.utils.ShapeUtils;
import dev.lopyluna.slag.register.AllBETypes;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllLangs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class TableBlock extends SmartBlock<TableBE> {
    public static final MapCodec<TableBlock> CODEC = simpleCodec(TableBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public TableBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }
    @Override protected @NotNull MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public Class<TableBE> getBlockEntityClass() { return TableBE.class; }
    @Override public BlockEntityType<? extends TableBE> getBlockEntityType() { return AllBETypes.TABLE.get(); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Nullable @Override public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
        if (!(level.getBlockEntity(pos) instanceof TableBE be) || be.coolingProgress > 0) return InteractionResult.PASS;
        var shift = player.isShiftKeyDown();
        var empty = player.getMainHandItem().isEmpty();
        if (shift && empty && be.tankInventory != null && !be.tankInventory.isEmpty()) {
            be.tankInventory.drain(be.tankInventory.getFluidAmount(), IFluidHandler.FluidAction.EXECUTE);
            return InteractionResult.SUCCESS;
        }
        if (be.itemInventory == null || (player.isShiftKeyDown() && !empty)) return InteractionResult.PASS;
        var stack = be.getStack();
        if (stack.isEmpty()) {
            var mold = be.getMold();
            if (mold.isEmpty()) return InteractionResult.PASS;
            ItemHandlerHelper.giveItemToPlayer(player, mold);
            be.itemInventory.getFirstMoldItem().setCount(0);
            return InteractionResult.SUCCESS;
        }
        ItemHandlerHelper.giveItemToPlayer(player, stack);
        be.itemInventory.getFirstItem().setCount(0);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var pass = ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level.getBlockEntity(pos) instanceof TableBE be) || player.isShiftKeyDown() || !(stack.getItem() instanceof DynamicMoldItem)) return pass;
        if (be.itemInventory == null || !stack.has(AllDataComponents.CAST_TYPE)) return pass;
        if (!be.getMold().isEmpty()) return pass;
        be.itemInventory.insertItem(1, stack.copyWithCount(1), false);
        stack.shrink(1);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ShapeUtils.shape(0, 0, 0, 5, 4, 5)
                .add(0, 0, 11, 5, 4, 16)
                .add(11, 0, 11, 16, 4, 16)
                .add(11, 0, 0, 16, 4, 5)
                .add(0, 4, 0, 16, 9, 16)
                .add(0, 9, 0, 2, 12, 16)
                .add(14, 9, 0, 16, 12, 16)
                .add(0, 9, 14, 16, 12, 16)
                .add(0, 9, 0, 16, 12, 2).build();
    }
}
