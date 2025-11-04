package dev.lopyluna.slag.content.blocks.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public interface IMultiBlockEntityContainer {

    BlockPos getController();
    <T extends BlockEntity & IMultiBlockEntityContainer> T getControllerBE();
    boolean isController();
    void setController(BlockPos pos);
    void removeController(boolean keepContents);
    BlockPos getLastKnownPos();

    void preventConnectivityUpdate();
    void notifyMultiUpdated();

    default void setExtraData(@Nullable Object data) {}
    @Nullable
    default Object getExtraData() { return null; }
    default Object modifyExtraData(Object data) { return data; }

    // multiblock structural information
    Direction.Axis getMainConnectionAxis();

    default Direction.Axis getMainAxisOf(BlockEntity be) {
        BlockState state = be.getBlockState();

        Direction.Axis axis;
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
        else if (state.hasProperty(BlockStateProperties.FACING)) axis = state.getValue(BlockStateProperties.FACING).getAxis();
        else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) axis = state.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis();
        else axis = Direction.Axis.Y;

        return axis;
    }

    int getMaxLength(Direction.Axis longAxis, int width);
    int getMaxWidth();
    default int getMaxWidthX() { return getMaxWidth(); }
    default int getMaxWidthZ() { return getMaxWidth(); }

    int getHeight();
    void setHeight(int height);
    int getWidthX();
    void setWidthX(int width);
    int getWidthZ();
    void setWidthZ(int width);

    interface Inventory extends IMultiBlockEntityContainer {
        default boolean hasInventory() { return false; }
    }

    interface Fluid extends IMultiBlockEntityContainer {
        // done here rather than through the Capability to allow greater flexibility
        default boolean hasTank() { return false; }

        default int getTankSize(int tank) {	return 0; }

        default void setTankSize(int tank, int blocks) {}

        default IFluidTank getTank(int tank) { return null; }

        default FluidStack getFluid(int tank) {	return FluidStack.EMPTY; }
    }

    interface FluidMulti extends IMultiBlockEntityContainer {
        default boolean hasTank() { return false; }

        default int getTankSize() {	return 0; }

        default int getTotalTankSize() { return 0; }

        default void setTankSize(int blocks) {}

        default IFluidTank getTank() { return null; }

        default List<FluidStack> getFluids() {	return new ArrayList<>(); }
    }
}
