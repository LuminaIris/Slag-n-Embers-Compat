package dev.lopyluna.slag.content.blocks.table;

import com.mojang.datafixers.util.Pair;
import dev.lopyluna.slag.content.blocks.multiblock.LerpedFloat;
import dev.lopyluna.slag.content.blocks.smart.BlockEntityBehaviour;
import dev.lopyluna.slag.content.blocks.smart.SmartBlockEntity;
import dev.lopyluna.slag.register.AllBETypes;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
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

public class TableBE extends SmartBlockEntity {
    public static final Map<Pair<Fluid, TagKey<Item>>, TableBE.TableHandler> tableHandlers = new HashMap<>();

    protected IItemHandler itemCapability;
    protected IFluidHandler fluidCapability;
    protected boolean forceFluidLevelUpdate;
    protected TableInventory itemInventory;
    protected TableTank tankInventory;
    protected boolean updateCapability;
    protected int luminosity;

    private static final int SYNC_RATE = 8;
    protected int syncCooldown;
    protected boolean queuedSync;

    public int coolingTarget;
    public int coolingProgress;

    public ItemStack resultItemStack = ItemStack.EMPTY;
    public ItemStack moldItemStack = ItemStack.EMPTY;

    // For rendering purposes only
    private LerpedFloat fluidLevel;

    public TableBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        itemInventory = new TableInventory(2, this);
        tankInventory = createInventory(getCapacity());
        forceFluidLevelUpdate = true;

        updateCapability = false;
        refreshCapability();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, AllBETypes.TABLE.get(), (be, context) -> {
            var mold = be.getMold();
            if (mold.isEmpty() || !mold.has(AllDataComponents.CAST_TYPE) || !be.getStack().isEmpty()) return null;
            if (be.fluidCapability == null) be.refreshCapability();
            return be.fluidCapability;
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AllBETypes.TABLE.get(), (be, context) -> {
            if (be.itemCapability == null) be.refreshCapability();
            return be.itemCapability;
        });
    }

    @Override
    public void tick() {
        super.tick();
        if (syncCooldown > 0) {
            syncCooldown--;
            if (syncCooldown == 0 && queuedSync) sendData();
        }

        if (updateCapability) {
            updateCapability = false;
            refreshCapability();
        }
        if (fluidLevel != null) fluidLevel.tickChaser();
        if (level == null || level.isClientSide || tableHandlers.isEmpty()) return;

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
        var moldStack = getMold();
        if (moldStack.isEmpty()) {
            setMoldPreview(ItemStack.EMPTY);
            return false;
        } else if (!ItemStack.isSameItemSameComponents(moldStack, moldItemStack)) setMoldPreview(moldStack);
        var moldType = moldStack.get(AllDataComponents.CAST_TYPE);
        if (moldType == null) return false;

        var fluidStack = getFluidStack();
        if (fluidStack.isEmpty()) return false;

        var handler = tableHandlers.get(Pair.of(fluidStack.getFluid(), moldType));
        if (handler == null) return false;

        var tank = getTankInventory();
        if (tank == null) return false;
        var item = getItemInventory();
        if (item == null) return false;

        var result = getResultStack(handler);
        setPreview(result);

        var capacity = tank.getCapacity();
        if (capacity != tank.getFluidAmount()) return true;

        coolingTarget = Mth.clamp((int) ((float) capacity * 0.25f), 4, 256);
        if (coolingTarget > coolingProgress) ++coolingProgress;
        else {
            coolingProgress = 0;
            item.setItem(0, result);
            var mold = item.getFirstMoldItem();
            if (mold.is(AllTags.MOLDS_SINGLE)) mold.setCount(0);
            tank.drain(capacity, IFluidHandler.FluidAction.EXECUTE);
            level.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1F);
        }
        if (!level.isClientSide) { setChanged(); sendData(); }
        return true;
    }

    protected TableTank createInventory(int capacity) {
        return new TableTank(this, capacity, this::onFluidStackChanged);
    }

    public void refreshCapability() {
        fluidCapability = handlerForCapability();
        itemCapability = handlerForCapabilityItem();
        invalidateCapabilities();
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
            fluidLevel.chase(getFillState(), 0.75f, LerpedFloat.Chaser.EXP);
        }
    }
    public float getFillState() { return (float) tankInventory.getFluidAmount() / tankInventory.getCapacity(); }

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

    private void setPreview(ItemStack preview) {
        ItemStack p = preview.isEmpty() ? getStack() : preview.copy();
        boolean same = ItemStack.isSameItemSameComponents(resultItemStack, p);
        if (same) return;
        resultItemStack = p;
        if (level != null && !level.isClientSide) { setChanged(); sendData(); }
    }
    private void setMoldPreview(ItemStack preview) {
        ItemStack p = preview.isEmpty() ? getMold() : preview.copy();
        boolean same = ItemStack.isSameItemSameComponents(moldItemStack, p);
        if (same) return;
        moldItemStack = p;
        if (level != null && !level.isClientSide) { setChanged(); sendData(); }
    }

    public int getCapacity() {
        var moldStack = getMold();
        if (moldStack.isEmpty()) return 250;
        var moldType = moldStack.get(AllDataComponents.CAST_TYPE);
        if (moldType == null) return 250;
        var fluid = getFluidStack();
        if (fluid.isEmpty()) return 250;
        var handler = tableHandlers.get(Pair.of(fluid.getFluid(), moldType));
        return handler == null ? 250 : handler.capacity();
    }

    public ItemStack getResultStack(TableBE.TableHandler handler) {
        return handler == null ? ItemStack.EMPTY : handler.result().copy();
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
        moldItemStack = tag.contains("MoldPreview") ? ItemStack.parseOptional(registries, tag.getCompound("MoldPreview")) : ItemStack.EMPTY;
        coolingProgress = tag.getInt("CoolingProgress");
        coolingTarget = tag.getInt("CoolingTarget");

        float fillState = getFillState();
        if (tag.contains("ForceFluidLevel") || fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(fillState);
        fluidLevel.chase(fillState, 0.75f, LerpedFloat.Chaser.EXP);

        if (luminosity != prevLum && hasLevel()) level.getChunkSource().getLightEngine().checkBlock(worldPosition);

        if (tag.contains("LazySync")) fluidLevel.chase(fluidLevel.getChaseTarget(), 0.25f, LerpedFloat.Chaser.EXP);
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
        if (!moldItemStack.isEmpty()) tag.put("MoldPreview", moldItemStack.save(registries, new CompoundTag()));
        tag.putInt("CoolingProgress", coolingProgress);
        tag.putInt("CoolingTarget", coolingTarget);

        if (forceFluidLevelUpdate) tag.putBoolean("ForceFluidLevel", true);
        if (queuedSync) tag.putBoolean("LazySync", true);
        forceFluidLevelUpdate = false;
    }

    public FluidStack getFluidStack() {
        var inv = getTankInventory();
        return inv == null ? FluidStack.EMPTY : inv.getFluid();
    }
    public ItemStack getMold() {
        var inv = getItemInventory();
        return inv == null ? ItemStack.EMPTY : inv.getFirstMoldItem().copy();
    }
    public ItemStack getStack() {
        var inv = getItemInventory();
        return inv == null ? ItemStack.EMPTY : inv.getFirstItem().copy();
    }

    private IItemHandler handlerForCapabilityItem() { return itemInventory; }
    private IFluidHandler handlerForCapability() { return tankInventory; }
    public int getLuminosity() { return luminosity; }
    @Override public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}
    public TableInventory getItemInventory() { return itemInventory; }
    public TableTank getTankInventory() { return tankInventory; }
    public LerpedFloat getFluidLevel() { return fluidLevel; }
    @SuppressWarnings("unused") public void setFluidLevel(LerpedFloat fluidLevel) { this.fluidLevel = fluidLevel; }
    public record TableHandler(Fluid fluid, int capacity, TagKey<Item> cast, ItemStack result) {}
}
