package dev.lopyluna.slag.content.blocks.drain;

import dev.lopyluna.slag.config.SlagServerConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DrainBE extends BlockEntity {
    public DrainState drainState = DrainState.OFF;
    public FluidStack drainingFluid = FluidStack.EMPTY;
    public DrainBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }
    private IFluidHandler getFluidHandlerDir(Direction dir) {
        assert level != null;
        var pos = worldPosition.relative(dir);
        if (!level.isLoaded(pos)) return null;
        var be = level.getBlockEntity(pos);
        if (be == null) return null;
        return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, dir.getOpposite());
    }

    public IFluidHandler getInputHandler() {
        return getFluidHandlerDir(getBlockState().getValue(DrainBlock.FACING).getOpposite());
    }
    public IFluidHandler getOutputHandler() {
        return getFluidHandlerDir(Direction.DOWN);
    }

    public void checkPowered() {
        if (level == null) return;
        drainState = level.hasNeighborSignal(worldPosition) ? DrainState.POWERED : drainState == DrainState.POURING ? DrainState.POURING : DrainState.OFF;
    }

    public void cycleDrain() {
        if (drainState != DrainState.POWERED) drainState = drainState == DrainState.OFF ? DrainState.POURING : DrainState.OFF;
    }

    public void update() {
        setChanged();
        if (level == null) return;
        var state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
    }

    int random = 0;
    public void tick() {
        if (level == null || level.isClientSide) return;
        if (drainState == DrainState.OFF) {
            if (level.random.nextInt(6) == 0) random = Mth.clamp(level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).getCommandResult() * 2, 0, 128);
            if (random != 0 && level.random.nextInt(129 - random) == 0) checkPowered();
        }

        var prevFluid = drainingFluid.copy();
        var prevState = drainState;

        var drain = drain(drainState);
        if (drain != null && drain != drainState) drainState = drain;
        if (!FluidStack.isSameFluidSameComponents(prevFluid, drainingFluid) || prevState != drainState) update();
    }

    public DrainState drain(DrainState cur) {
        drainingFluid = FluidStack.EMPTY;
        if (cur == DrainState.OFF) return null;
        var inputInv = getInputHandler();
        if (inputInv == null) return null;
        var outputInv = getOutputHandler();
        if (outputInv == null) return null;

        var inputFluid = inputInv.getFluidInTank(0);
        if (inputFluid.isEmpty()) return DrainState.OFF;
        var outputFluid = outputInv.getFluidInTank(0);
        var outputAmount = outputFluid.getAmount();
        var outputCapacity = outputInv.getTankCapacity(0);
        if (outputAmount >= outputCapacity) return cur == DrainState.POWERED ? cur : DrainState.OFF;
        var targetDrain = Mth.clamp(outputFluid.isEmpty() ? 1 : SlagServerConfigs.DRAIN_MB_SPEED.get(), 0, outputCapacity - outputAmount);
        if (targetDrain == 0) return cur == DrainState.POWERED ? cur : DrainState.OFF;


        var drained = inputInv.drain(targetDrain, IFluidHandler.FluidAction.SIMULATE);
        if (drained.getAmount() != outputInv.fill(drained, IFluidHandler.FluidAction.SIMULATE)) return cur == DrainState.POWERED ? cur : DrainState.OFF;

        drainingFluid = inputFluid;
        inputInv.drain(targetDrain, IFluidHandler.FluidAction.EXECUTE);
        outputInv.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        return cur;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (drainState == null || drainState == DrainState.OFF) drainState = DrainState.fromString(tag.getString("state"));
        this.drainingFluid = FluidStack.parseOptional(registries, tag.getCompound("Fluid"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (drainState != null) tag.putString("state", "" + drainState);
        if (!this.drainingFluid.isEmpty()) tag.put("Fluid", this.drainingFluid.save(registries));
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
