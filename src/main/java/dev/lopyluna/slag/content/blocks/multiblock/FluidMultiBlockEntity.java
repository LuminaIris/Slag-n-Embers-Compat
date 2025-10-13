package dev.lopyluna.slag.content.blocks.multiblock;

import dev.lopyluna.slag.content.blocks.crucible.CrucibleTank;
import dev.lopyluna.slag.content.blocks.multiblock.connectivity.ConnectivityHandler;
import dev.lopyluna.slag.content.blocks.smart.BlockEntityBehaviour;
import dev.lopyluna.slag.content.blocks.smart.SmartBlockEntity;
import dev.lopyluna.slag.content.utils.NBTHelper;
import dev.lopyluna.slag.register.AllBETypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static dev.lopyluna.slag.content.blocks.crucible.CrucibleBE.BOTTOM;
import static dev.lopyluna.slag.content.blocks.crucible.CrucibleBE.TOP;

@SuppressWarnings("unchecked")
public class FluidMultiBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer.FluidMulti {

    protected IFluidHandler fluidCapability;
    protected boolean forceFluidLevelUpdate;
    protected CrucibleTank tankInventory;
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected boolean updateCapability;
    protected boolean window;
    protected int luminosity;
    protected int widthX;
    protected int widthZ;
    protected int height;

    private static final int SYNC_RATE = 8;
    protected int syncCooldown;
    protected boolean queuedSync;

    // For rendering purposes only
    private LerpedFloat fluidLevel;

    public FluidMultiBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tankInventory = createInventory();
        forceFluidLevelUpdate = true;
        updateConnectivity = false;
        updateCapability = false;
        window = false;
        height = 1;
        widthX = 1;
        widthZ = 1;
        refreshCapability();
    }

    public boolean isWindow() {
        return window;
    }

    public int getLuminosity() {
        return luminosity;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, AllBETypes.CRUCIBLE.get(), (be, context) -> {
            if (be.fluidCapability == null) be.refreshCapability();
            return be.fluidCapability;
        });
    }

    protected CrucibleTank createInventory() {
        return new CrucibleTank(getCapacityMultiplier(), this::onFluidStackChanged);
    }

    public void updateConnectivity() {
        assert level != null;
        updateConnectivity = false;
        if (level.isClientSide) return;
        if (!isController()) return;
        ConnectivityHandler.formMulti(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (syncCooldown > 0) {
            syncCooldown--;
            if (syncCooldown == 0 && queuedSync)
                sendData();
        }

        if (lastKnownPos == null)
            lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition)) {
            onPositionChanged();
            return;
        }

        if (updateCapability) {
            updateCapability = false;
            refreshCapability();
        }
        if (updateConnectivity) updateConnectivity();
        if (fluidLevel != null) fluidLevel.tickChaser();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.getX() == controller.getX() && worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
    }

    @Override
    public void initialize() {
        super.initialize();
        assert level != null;
        sendData();
        if (level.isClientSide) invalidateRenderBoundingBox();
    }

    protected void onPositionChanged() {
        removeController(true);
        lastKnownPos = worldPosition;
    }

    protected void onFluidStackChanged(List<FluidStack> newFluids) {
        if (level == null) return;

        for (int y = 0; y < height; y++) for (int x = 0; x < widthX; x++) for (int z = 0; z < widthZ; z++) {
            BlockPos pos = this.worldPosition.offset(x, y, z);
            FluidMultiBlockEntity part = ConnectivityHandler.partAt(getType(), level, pos);
            if (part == null) continue;
            level.updateNeighbourForOutputSignal(pos, part.getBlockState().getBlock());
        }


        if (!level.isClientSide) {
            setChanged();
            sendData();
        } else {
            if (fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(getFillState());
            fluidLevel.chase(getFillState(), 0.5f, LerpedFloat.Chaser.EXP);
        }
    }

    @SuppressWarnings("unused")
    protected void setLuminosity(int luminosity) { //TODO: ADD LUMINOSITY
        assert level != null;
        if (level.isClientSide) return;
        if (this.luminosity == luminosity) return;
        this.luminosity = luminosity;
        sendData();
    }

    @SuppressWarnings("unchecked")
    @Override
    public FluidMultiBlockEntity getControllerBE() {
        assert level != null;
        if (isController() || !hasLevel()) return this;
        if (level.getBlockEntity(getController()) instanceof FluidMultiBlockEntity be) return be;
        return null;
    }

    public void applyFluidTankSize(int blocks) {
        var newCap = (blocks) * getCapacityMultiplier();
        tankInventory.setCapacity(newCap);
        int overflow = tankInventory.getTotalFluidAmount() - newCap;
        if (overflow > 0) tankInventory.drain(overflow, IFluidHandler.FluidAction.EXECUTE);
        forceFluidLevelUpdate = true;
    }

    public void removeController(boolean keepFluids) {
        assert level != null;
        if (level.isClientSide) return;
        updateConnectivity = true;
        if (!keepFluids) applyFluidTankSize(1);
        controller = null;
        widthX = 1;
        widthZ = 1;
        height = 1;

        onFluidStackChanged(tankInventory.getFluids());

        BlockState state = getBlockState();
        if (state.hasProperty(BOTTOM) && state.hasProperty(TOP)) {
            state = state.setValue(BOTTOM, true);
            state = state.setValue(TOP, true);
            level.setBlock(worldPosition, state, 22);
        }

        refreshCapability();
        setChanged();
        sendData();
    }

    @SuppressWarnings("unused")
    public void toggleWindows() {
        FluidMultiBlockEntity be = getControllerBE();
        if (be == null) return;
        be.setWindows(!be.window);
    }

    public void sendDataImmediately() {
        syncCooldown = 0;
        queuedSync = false;
        sendData();
    }

    @Override
    public void sendData() {
        if (syncCooldown > 0) {
            queuedSync = true;
            return;
        }
        super.sendData();
        queuedSync = false;
        syncCooldown = SYNC_RATE;
    }

    public void setWindows(boolean window) {
        this.window = window;
    }

    @Override
    public void setController(BlockPos controller) {
        assert level != null;
        if (level.isClientSide) return;
        if (controller.equals(this.controller)) return;
        this.controller = controller;
        refreshCapability();
        setChanged();
        sendData();
    }

    public void refreshCapability() {
        fluidCapability = handlerForCapability();
        invalidateCapabilities();
    }

    private IFluidHandler handlerForCapability() {
        return isController() ? tankInventory : ((getControllerBE() != null) ? getControllerBE().handlerForCapability() : new FluidTank(0));
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        if (isController()) return super.createRenderBoundingBox().expandTowards(widthX - 1, height - 1, widthZ - 1);
        else return super.createRenderBoundingBox();
    }

    @SuppressWarnings("unused")
    @Nullable
    public FluidMultiBlockEntity getOtherFluidMultiBlockEntity(Direction direction) {
        assert level != null;
        BlockEntity otherBE = level.getBlockEntity(worldPosition.relative(direction));
        if (otherBE instanceof FluidMultiBlockEntity be) return be;
        return null;
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        assert level != null;

        BlockPos controllerBefore = controller;
        int prevWX = widthX;
        int prevWZ = widthZ;
        int prevHeight = height;
        int prevLum = luminosity;

        updateConnectivity = compound.contains("Uninitialized");
        luminosity = compound.getInt("Luminosity");

        lastKnownPos = null;
        if (compound.contains("LastKnownPos")) lastKnownPos = NBTHelper.readBlockPos(compound, "LastKnownPos");

        controller = null;
        if (compound.contains("Controller")) controller = NBTHelper.readBlockPos(compound, "Controller");

        if (isController()) {
            window = compound.getBoolean("Window");
            widthX = compound.getInt("WidthX");
            widthZ = compound.getInt("WidthZ");
            height = compound.getInt("Height");
            tankInventory.setCapacity(getTotalSize() * getCapacityMultiplier());

            tankInventory.readFromNBT(registries, compound.getCompound("TankContent"));
            if (tankInventory.getSpace() < 0) tankInventory.drain(-tankInventory.getSpace(), IFluidHandler.FluidAction.EXECUTE);
        }

        if (compound.contains("ForceFluidLevel") || fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(getFillState());

        updateCapability = true;

        if (!clientPacket) return;

        boolean changeOfController = !Objects.equals(controllerBefore, controller);
        if (changeOfController || prevWX != widthX || prevWZ != widthZ || prevHeight != height) {
            if (hasLevel()) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
            if (isController()) tankInventory.setCapacity(getCapacityMultiplier() * getTotalSize());
            invalidateRenderBoundingBox();
        }
        if (isController()) {
            float fillState = getFillState();
            if (compound.contains("ForceFluidLevel") || fluidLevel == null)
                fluidLevel = LerpedFloat.linear().startWithValue(fillState);
            fluidLevel.chase(fillState, 0.5f, LerpedFloat.Chaser.EXP);
        }
        if (luminosity != prevLum && hasLevel()) level.getChunkSource().getLightEngine().checkBlock(worldPosition);

        if (compound.contains("LazySync")) fluidLevel.chase(fluidLevel.getChaseTarget(), 0.125f, LerpedFloat.Chaser.EXP);

    }

    public float getFillState() {
        return (float) tankInventory.getTotalFluidAmount() / tankInventory.getCapacity();
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        if (updateConnectivity)
            compound.putBoolean("Uninitialized", true);
        if (lastKnownPos != null)
            compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController())
            compound.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {
            compound.putBoolean("Window", window);
            compound.put("TankContent", tankInventory.writeToNBT(registries, new CompoundTag()));
            compound.putInt("WidthX", widthX);
            compound.putInt("WidthZ", widthZ);
            compound.putInt("Height", height);
        }
        compound.putInt("Luminosity", luminosity);
        super.write(compound, registries, clientPacket);

        if (!clientPacket)
            return;
        if (forceFluidLevelUpdate)
            compound.putBoolean("ForceFluidLevel", true);
        if (queuedSync)
            compound.putBoolean("LazySync", true);
        forceFluidLevelUpdate = false;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public CrucibleTank getTankInventory() {
        return tankInventory;
    }

    public int getTotalSize() {
        return widthX * widthZ * height;
    }

    public int getTotalTankSize() {
        return getTotalSize() * getCapacityMultiplier();
    }

    public static int getCapacityMultiplier() {
        return 1000;
    }

    public LerpedFloat getFluidLevel() {
        return fluidLevel;
    }

    @SuppressWarnings("unused")
    public void setFluidLevel(LerpedFloat fluidLevel) {
        this.fluidLevel = fluidLevel;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        assert level != null;
        BlockState state = this.getBlockState();
        if (state.hasProperty(BOTTOM) && state.hasProperty(TOP)) { // safety
            state = state.setValue(BOTTOM, getController().getY() == getBlockPos().getY());
            state = state.setValue(TOP, getController().getY() + height - 1 == getBlockPos().getY());
            level.setBlock(getBlockPos(), state, 6);
        }
        if (isController()) setWindows(window);
        onFluidStackChanged(tankInventory.getFluids());
        setChanged();
    }

    @Override
    public void setExtraData(@Nullable Object data) {
        if (data instanceof Boolean) window = (boolean) data;
    }

    @Override
    @Nullable
    public Object getExtraData() {
        return window;
    }

    @Override
    public Object modifyExtraData(Object data) {
        if (data instanceof Boolean windows) {
            windows |= window;
            return windows;
        }
        return data;
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return Direction.Axis.Y;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        if (longAxis == Direction.Axis.Y) return 9;
        return getMaxWidth();
    }

    @Override
    public int getMaxWidth() {
        return 7;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getWidthX() {
        return widthX;
    }
    @Override
    public int getWidthZ() {
        return widthZ;
    }

    @Override
    public void setWidthX(int width) {
        this.widthX = width;
    }

    @Override
    public void setWidthZ(int width) {
        this.widthZ = width;
    }

    @Override
    public boolean hasTank() {
        return true;
    }

    @Override
    public int getTankSize() {
        return getCapacityMultiplier();
    }

    @Override
    public void setTankSize(int blocks) {
        applyFluidTankSize(blocks);
    }

    @Override
    public IFluidTank getTank() {
        return tankInventory;
    }

    @Override
    public List<FluidStack> getFluids() {
        return tankInventory.getFluidCopy();
    }
}
