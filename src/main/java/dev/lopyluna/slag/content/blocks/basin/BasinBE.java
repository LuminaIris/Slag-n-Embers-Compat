package dev.lopyluna.slag.content.blocks.basin;

import dev.lopyluna.slag.content.blocks.multiblock.LerpedFloat;
import dev.lopyluna.slag.content.blocks.smart.BlockEntityBehaviour;
import dev.lopyluna.slag.content.blocks.smart.SmartBlockEntity;
import dev.lopyluna.slag.register.AllBETypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasinBE extends SmartBlockEntity {
    public static final Map<Fluid, BasinBE.BasinHandler> basinHandlers = new HashMap<>();

    protected IItemHandler itemCapability;
    protected IFluidHandler fluidCapability;
    protected boolean forceFluidLevelUpdate;
    protected BasinInventory itemInventory;
    protected BasinTank tankInventory;
    protected boolean updateCapability;
    protected int luminosity;

    private static final int SYNC_RATE = 8;
    protected int syncCooldown;
    protected boolean queuedSync;

    public int coolingTarget;
    public int coolingProgress;

    public ItemStack resultItemStack = ItemStack.EMPTY;

    // For rendering purposes only
    private LerpedFloat fluidLevel;

    public BasinBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);

        itemInventory = new BasinInventory(1, this);
        tankInventory = createInventory(getCapacity());
        forceFluidLevelUpdate = true;

        updateCapability = false;
        refreshCapability();
    }

    public int getCapacity() {
        var fluid = getFluidStack();
        if (fluid.isEmpty()) return 1000;
        var handler = basinHandlers.get(fluid.getFluid());
        return handler == null ? 1000 : handler.capacity();
    }

    public ItemStack getResultStack() {
        var item = getStack();
        if (!item.isEmpty()) return item;
        var fluid = getFluidStack();
        if (fluid.isEmpty()) return ItemStack.EMPTY;
        var handler = basinHandlers.get(fluid.getFluid());
        return handler == null ? ItemStack.EMPTY : handler.result().copy();
    }

    public int getLuminosity() {
        return luminosity;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, AllBETypes.BASIN.get(), (be, context) -> {
            if (!be.getStack().isEmpty()) return null;
            if (be.fluidCapability == null) be.refreshCapability();
            return be.fluidCapability;
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AllBETypes.BASIN.get(), (be, context) -> {
            if (be.itemCapability == null) be.refreshCapability();
            return be.itemCapability;
        });
    }

    @Override
    public void tick() {
        super.tick();
        if (syncCooldown > 0) {
            syncCooldown--;
            if (syncCooldown == 0 && queuedSync)
                sendData();
        }

        if (updateCapability) {
            updateCapability = false;
            refreshCapability();
        }
        if (fluidLevel != null) fluidLevel.tickChaser();
        if (level == null || level.isClientSide || basinHandlers.isEmpty()) return;

        var recipe = tickRecipe(level);
        if (recipe == null) return;
        if (!recipe) {
            coolingProgress = 0;
            setPreview(ItemStack.EMPTY);
        }
    }

    public Boolean tickRecipe(Level level) {
        var stack = getStack();
        if (!stack.isEmpty()) {
            setPreview(stack);
            return null;
        }

        var fluidStack = getFluidStack();
        if (fluidStack.isEmpty()) return false;

        var handler = basinHandlers.get(fluidStack.getFluid());
        if (handler == null) return false;

        var tank = getTankInventory();
        if (tank == null) return false;
        var item = getItemInventory();
        if (item == null) return false;

        var result = getResultStack();
        setPreview(result);

        var capacity = tank.getCapacity();
        if (capacity != tank.getFluidAmount()) return true;

        coolingTarget = Mth.clamp((int) ((float) capacity * 0.5f), 4, 256);
        if (coolingTarget > coolingProgress) ++coolingProgress;
        else {
            coolingProgress = 0;
            item.setItem(0, result);
            tank.drain(capacity, IFluidHandler.FluidAction.EXECUTE);
            level.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1F);
        }
        if (!level.isClientSide) { setChanged(); sendData(); }
        return true;
    }

    protected BasinTank createInventory(int capacity) {
        return new BasinTank(this, capacity, this::onFluidStackChanged);
    }

    public void refreshCapability() {
        fluidCapability = handlerForCapability();
        itemCapability = handlerForCapabilityItem();
        invalidateCapabilities();
    }

    private IItemHandler handlerForCapabilityItem() {
        return itemInventory;
    }
    private IFluidHandler handlerForCapability() {
        return tankInventory;
    }

    protected void onFluidStackChanged(FluidStack newFluids) {
        if (level == null) return;
        if (tankInventory != null) {
            tankInventory.setCapacity(getCapacity());
            if (tankInventory.getSpace() < 0) tankInventory.drain(-tankInventory.getSpace(), IFluidHandler.FluidAction.EXECUTE);
        }

        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());

        if (!level.isClientSide) {
            setChanged();
            sendData();
        } else {
            if (fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(getFillState());
            fluidLevel.chase(getFillState(), 0.5f, LerpedFloat.Chaser.EXP);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        dropContents(level, worldPosition, itemInventory);
    }

    public static void dropContents(Level level, BlockPos pos, IItemHandler inv) {
        for (int slot = 0; slot < inv.getSlots(); slot++) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inv.getStackInSlot(slot));
    }

    protected void setLuminosity(int luminosity) { //TODO: ADD LUMINOSITY
        assert level != null;
        if (level.isClientSide) return;
        if (this.luminosity == luminosity) return;
        this.luminosity = luminosity;
        sendData();
    }

    public float getFillState() {
        return (float) tankInventory.getFluidAmount() / tankInventory.getCapacity();
    }

    private void setPreview(ItemStack preview) {
        ItemStack p = preview.isEmpty() ? ItemStack.EMPTY : preview.copy();
        boolean same = ItemStack.isSameItemSameComponents(resultItemStack, p) && resultItemStack.getCount() == p.getCount();
        if (same) return;
        resultItemStack = p;
        if (level != null && !level.isClientSide) { setChanged(); sendData(); }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        assert level != null;
        int prevLum = luminosity;
        luminosity = tag.getInt("Luminosity");
        itemInventory.load(tag, registries);

        tankInventory.setCapacity(getCapacity());
        tankInventory.readFromNBT(registries, tag.getCompound("TankContent"));
        if (tankInventory.getSpace() < 0) tankInventory.drain(-tankInventory.getSpace(), IFluidHandler.FluidAction.EXECUTE);

        if (tag.contains("ForceFluidLevel") || fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(getFillState());

        updateCapability = true;

        if (!clientPacket) return;

        resultItemStack = tag.contains("ResultPreview") ? ItemStack.parseOptional(registries, tag.getCompound("ResultPreview")) : ItemStack.EMPTY;
        coolingProgress = tag.getInt("CoolingProgress");
        coolingTarget = tag.getInt("CoolingTarget");

        float fillState = getFillState();
        if (tag.contains("ForceFluidLevel") || fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(fillState);
        fluidLevel.chase(fillState, 0.5f, LerpedFloat.Chaser.EXP);

        if (luminosity != prevLum && hasLevel()) level.getChunkSource().getLightEngine().checkBlock(worldPosition);

        if (tag.contains("LazySync")) fluidLevel.chase(fluidLevel.getChaseTarget(), 0.125f, LerpedFloat.Chaser.EXP);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (tankInventory != null) tankInventory.setCapacity(getCapacity());
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.put("TankContent", tankInventory.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Luminosity", luminosity);
        super.write(tag, registries, clientPacket);
        itemInventory.save(tag, registries);
        if (!clientPacket) return;
        if (!resultItemStack.isEmpty()) tag.put("ResultPreview", resultItemStack.save(registries, new CompoundTag()));
        tag.putInt("CoolingProgress", coolingProgress);
        tag.putInt("CoolingTarget", coolingTarget);

        if (forceFluidLevelUpdate) tag.putBoolean("ForceFluidLevel", true);
        if (queuedSync) tag.putBoolean("LazySync", true);
        forceFluidLevelUpdate = false;
    }

    @Override
    public void initialize() {
        super.initialize();
        sendData();
    }

    public void sendDataImmediately() {
        syncCooldown = 0;
        queuedSync = false;
        sendData();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        itemInventory.setChanged();
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

    @Override
    public void invalidate() {
        if (itemInventory != null || fluidCapability != null) invalidateCapabilities();
        super.invalidate();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public FluidStack getFluidStack() {
        var inv = getTankInventory();
        return inv == null ? FluidStack.EMPTY : inv.getFluid();
    }

    public ItemStack getStack() {
        var inv = getItemInventory();
        return inv == null ? ItemStack.EMPTY : inv.getFirstItem().copy();
    }

    public BasinInventory getItemInventory() {
        return itemInventory;
    }
    public BasinTank getTankInventory() {
        return tankInventory;
    }

    public LerpedFloat getFluidLevel() {
        return fluidLevel;
    }

    @SuppressWarnings("unused")
    public void setFluidLevel(LerpedFloat fluidLevel) {
        this.fluidLevel = fluidLevel;
    }

    public record BasinHandler(Fluid fluid, int capacity, ItemStack result) {}
}
