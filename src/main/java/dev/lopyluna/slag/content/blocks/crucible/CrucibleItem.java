package dev.lopyluna.slag.content.blocks.crucible;

import dev.lopyluna.slag.content.AllUtils;
import dev.lopyluna.slag.content.blocks.multiblock.connectivity.ConnectivityHandler;
import dev.lopyluna.slag.content.blocks.smart.SmartBlock;
import dev.lopyluna.slag.content.utils.NBTHelper;
import dev.lopyluna.slag.register.AllBETypes;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CrucibleItem extends BlockItem {
    public CrucibleItem(Block block, Properties properties) {
        super(block, properties);
    }
    @Override
    public @NotNull InteractionResult place(BlockPlaceContext ctx) {
        InteractionResult initialResult = super.place(ctx);
        if (!initialResult.consumesAction()) return initialResult;
        tryMultiPlace(ctx);
        return initialResult;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        var server = level.getServer();
        if (server == null) return false;
        var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            CompoundTag nbt = data.copyTag();
            nbt.remove("Luminosity");
            nbt.remove("Size");
            nbt.remove("Height");
            nbt.remove("Controller");
            nbt.remove("LastKnownPos");
            if (nbt.contains("TankContent")) {
                var provider = server.registryAccess();
                var fluids = NBTHelper.readFluidList(nbt.getCompound("TankContent").getList("Fluids", Tag.TAG_COMPOUND), provider);

                int totalAmount = fluids.stream().mapToInt(FluidStack::getAmount).sum();
                int baseCapacity = CrucibleBE.getCapacityMultiplier();

                if (totalAmount > baseCapacity) {
                    int toRemove = totalAmount - baseCapacity;
                    for (int i = fluids.size() - 1; i >= 0 && toRemove > 0; i--) {
                        var fluid = fluids.get(i);
                        int take = Math.min(fluid.getAmount(), toRemove);
                        fluid.shrink(take);
                        toRemove -= take;
                        if (fluid.isEmpty()) fluids.remove(i);
                    }
                }

                var tag = new CompoundTag();
                tag.put("Fluids", NBTHelper.writeFluidList(fluids, provider));
                nbt.put("TankContent", tag);
            }
            BlockEntity.addEntityType(nbt, ((SmartBlock<?>) this.getBlock()).getBlockEntityType());
            stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(nbt));
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    private void tryMultiPlace(BlockPlaceContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null) return;
        if (player.isShiftKeyDown()) return;
        Direction face = ctx.getClickedFace();

        ItemStack stack = ctx.getItemInHand();
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockPos placedOnPos = pos.relative(face.getOpposite());
        BlockState placedOnState = level.getBlockState(placedOnPos);

        if (!(placedOnState.getBlock() instanceof CrucibleBlock)) return;
        if (AllUtils.tagPresentInHotbar(player, AllTags.BLACKLISTED_HOTBAR_ITEMS)) return;
        var crucibleAt = ConnectivityHandler.partAt(AllBETypes.CRUCIBLE.get(), level, placedOnPos);
        if (crucibleAt == null) return;
        var ctrlBE = crucibleAt.getControllerBE();
        if (ctrlBE == null) return;
        int widthZ = ctrlBE.getWidthZ();
        int widthX = ctrlBE.getWidthX();
        int height = ctrlBE.getHeight();
        if (1 >= widthZ || 1 >= widthX || 0 >= height) return;
        int crucibleToPlace = 0;

        var startPos = switch (face) {
            case DOWN -> ctrlBE.getBlockPos().below();
            case UP -> ctrlBE.getBlockPos().above(height);
            case NORTH -> ctrlBE.getBlockPos().north();
            case SOUTH -> ctrlBE.getBlockPos().south(widthZ);
            case WEST -> ctrlBE.getBlockPos().west();
            case EAST -> ctrlBE.getBlockPos().east(widthX);
        };
        var axis = face.getAxis();
        if (startPos.get(axis) != pos.get(axis)) return;

        var aAxis = switch (axis) {
            case Z, Y -> Direction.Axis.X;
            case X -> Direction.Axis.Y;
        };
        var bAxis = switch (axis) {
            case X, Y -> Direction.Axis.Z;
            case Z -> Direction.Axis.Y;
        };
        var aTarget = switch (axis) {
            case Z, Y -> widthX;
            case X -> height;
        };
        var bTarget = switch (axis) {
            case X, Y -> widthZ;
            case Z -> height;
        };

        for (int aOff = 0; aOff < aTarget; aOff++) for (int bOff = 0; bOff < bTarget; bOff++) {
            var offPos = startPos.relative(aAxis, aOff).relative(bAxis, bOff);
            var state = level.getBlockState(offPos);
            if (state.getBlock() instanceof CrucibleBlock) continue;
            if (!state.canBeReplaced()) return;
            crucibleToPlace++;
        }
        if (!player.isCreative() && stack.getCount() < crucibleToPlace) return;

        for (int aOff = 0; aOff < aTarget; aOff++) for (int bOff = 0; bOff < bTarget; bOff++) {
            var offPos = startPos.relative(aAxis, aOff).relative(bAxis, bOff);
            var state = level.getBlockState(offPos);
            if (state.getBlock() instanceof CrucibleBlock) continue;
            var ctxAt = BlockPlaceContext.at(ctx, offPos, face);
            player.getPersistentData().putBoolean("SilencePlacingSound", true);
            super.place(ctxAt);
            player.getPersistentData().remove("SilencePlacingSound");
        }
    }
}
