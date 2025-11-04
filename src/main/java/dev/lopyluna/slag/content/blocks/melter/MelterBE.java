package dev.lopyluna.slag.content.blocks.melter;

import dev.lopyluna.slag.content.blocks.melter.client.MelterMenu;
import dev.lopyluna.slag.content.blocks.multiblock.LerpedFloat;
import dev.lopyluna.slag.content.blocks.smart.BlockEntityBehaviour;
import dev.lopyluna.slag.content.blocks.smart.SmartBlockEntity;
import dev.lopyluna.slag.content.blocks.smart.SmartFluidTank;
import dev.lopyluna.slag.register.AllBETypes;
import dev.lopyluna.slag.register.AllRecipes;
import dev.lopyluna.slag.register.AllTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class MelterBE extends SmartBlockEntity implements MenuProvider {
    public static final int BLOCK_SIZE = 648; //BLOCKS | 1:1
    public static final int SMALL_BLOCK_SIZE = 288; //SMALL/CRYSTAL BLOCKS | 1:2.25
    public static final int CRYSTAL_SIZE = 162; //PEARL/BALL/CRYSTALS | 1:4
    public static final int INGOT_SIZE = 72; //INGOT/GEM | 1:9
    public static final int SHARD_SIZE = 18; //GEM NUGGET | 1:36
    public static final int NUGGET_SIZE = 8; //INGOT NUGGET | 1:81

    private final RecipeManager.CachedCheck<SingleRecipeInput, MeltingRecipe> quickCheck;

    protected IItemHandler itemCapability;
    protected IFluidHandler fluidCapability;
    protected boolean forceFluidLevelUpdate;
    protected MelterInventory itemInventory;
    protected SmartFluidTank tankInventory;
    protected boolean updateCapability;
    protected int luminosity;

    private static final int SYNC_RATE = 8;
    protected int syncCooldown;
    protected boolean queuedSync;

    // For rendering purposes only
    private LerpedFloat fluidLevel;

    public int meltingTarget;
    public int meltingProgress;
    public boolean melting;

    public MelterBE(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.quickCheck = RecipeManager.createCheck(AllRecipes.MELTING.get());
        itemInventory = new MelterInventory(1, this);
        tankInventory = createInventory();
        forceFluidLevelUpdate = true;

        updateCapability = false;
        refreshCapability();
    }

    public int getLuminosity() {
        return luminosity;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, AllBETypes.MELTER.get(), (be, context) -> {
            if (be.fluidCapability == null) be.refreshCapability();
            return be.fluidCapability;
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, AllBETypes.MELTER.get(), (be, context) -> {
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

        if (level == null) return;

        melting = tickRecipe(level);

        if (level.isClientSide) return;

        var facing = getBlockState().getValue(MelterBlock.FACING);
        var outputInv = getFluidHandlerDir(worldPosition.relative(facing.getOpposite()).below());
        if (outputInv == null) return;
        var outputFluid = outputInv.getFluidInTank(0);
        var inputInv = getTankInventory();
        if (inputInv == null) return;
        var inputFluid = inputInv.getFluidInTank(0);
        if (inputFluid.isEmpty()) return;
        var outputAmount = outputFluid.getAmount();
        var outputCapacity = outputInv.getTankCapacity(0);
        if (outputAmount >= outputCapacity) return;
        var targetDrain = Mth.clamp(outputFluid.isEmpty() ? 1 : 10, 0, outputCapacity - outputAmount);
        if (targetDrain == 0) return;

        var drained = inputInv.drain(targetDrain, IFluidHandler.FluidAction.SIMULATE);
        if (drained.getAmount() != outputInv.fill(drained, IFluidHandler.FluidAction.SIMULATE)) return;
        inputInv.drain(targetDrain, IFluidHandler.FluidAction.EXECUTE);
        outputInv.fill(drained, IFluidHandler.FluidAction.EXECUTE);
    }

    public boolean tickRecipe(Level level) {
        var stack = getStack();
        if (stack.isEmpty()) {
            meltingTarget = 0;
            meltingProgress = 0;
            return false;
        }
        var belowState = getBelowState(level);
        if (!belowState.is(AllTags.MELTER_HEATER)) {
            notMelting();
            return false;
        }

        var input = new SingleRecipeInput(stack);
        var recipeholder = quickCheck.getRecipeFor(input, level).orElse(null);
        var tank = getTankInventory();
        if (tank != null && recipeholder != null && recipeholder.value() instanceof MeltingRecipe recipe) {
            var multiplier = 1f;
            if (stack.isDamaged()) multiplier = 1f - ((float) stack.getDamageValue() / (float) stack.getMaxDamage());

            var access = level.registryAccess();
            var fluids = recipe.getResultFluids(access);
            var total = getTotalAmount(fluids);
            meltingTarget = Mth.clamp((int) ((float) total * 0.5f), 4, 256);
            var toFill = new ArrayList<FluidStack>();
            for (var fluid : fluids) toFill.add(new FluidStack(fluid.getFluidHolder(), Math.round((float) fluid.getAmount() * multiplier)));
            if (meltingTarget > meltingProgress) ++meltingProgress;
            else {
                if (total + tank.getFluidAmount() > tank.getCapacity()) return true;
                meltingProgress = 0;
                stack.shrink(1);
                for (var fluid : toFill) tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
            }
            return true;
        }
        notMelting();
        return false;
    }

    public static int getTotalAmount(List<FluidStack> fluids) {
        return fluids.stream().mapToInt(FluidStack::getAmount).sum();
    }

    public void notMelting() {
        if (meltingProgress > 0) --meltingProgress;
    }

    public BlockState getBelowState(Level level) {
        return level.getBlockState(worldPosition.below());
    }

    private IFluidHandler getFluidHandlerDir(BlockPos pos) {
        assert level != null;
        if (!level.isLoaded(pos)) return null;
        var be = level.getBlockEntity(pos);
        if (be == null) return null;
        return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.DOWN);
    }

    protected SmartFluidTank createInventory() {
        return new SmartFluidTank(4000, this::onFluidStackChanged);
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

        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());

        if (!level.isClientSide) {
            setChanged();
            sendData();
        } else {
            if (fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(getFillState());
            fluidLevel.chase(getFillState(), 0.5f, LerpedFloat.Chaser.EXP);
        }
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

    @Override
    public void destroy() {
        super.destroy();
        dropContents(level, worldPosition, itemInventory);
    }

    public static void dropContents(Level level, BlockPos pos, IItemHandler inv) {
        for (int slot = 0; slot < inv.getSlots(); slot++) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inv.getStackInSlot(slot));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        meltingProgress = tag.getInt("MeltingProgress");
        melting = tag.getBoolean("Melting");
        itemInventory.load(tag, registries);
        assert level != null;
        int prevLum = luminosity;
        luminosity = tag.getInt("Luminosity");

        tankInventory.setCapacity(4000);

        tankInventory.readFromNBT(registries, tag.getCompound("TankContent"));
        if (tankInventory.getSpace() < 0) tankInventory.drain(-tankInventory.getSpace(), IFluidHandler.FluidAction.EXECUTE);

        if (tag.contains("ForceFluidLevel") || fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(getFillState());

        updateCapability = true;

        if (!clientPacket) return;

        float fillState = getFillState();
        if (tag.contains("ForceFluidLevel") || fluidLevel == null) fluidLevel = LerpedFloat.linear().startWithValue(fillState);
        fluidLevel.chase(fillState, 0.5f, LerpedFloat.Chaser.EXP);

        if (luminosity != prevLum && hasLevel()) level.getChunkSource().getLightEngine().checkBlock(worldPosition);

        if (tag.contains("LazySync")) fluidLevel.chase(fluidLevel.getChaseTarget(), 0.125f, LerpedFloat.Chaser.EXP);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.put("TankContent", tankInventory.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Luminosity", luminosity);
        super.write(tag, registries, clientPacket);
        tag.putInt("MeltingProgress", meltingProgress);
        tag.putBoolean("Melting", melting);
        itemInventory.save(tag, registries);
        if (!clientPacket) return;
        if (forceFluidLevelUpdate) tag.putBoolean("ForceFluidLevel", true);
        if (queuedSync) tag.putBoolean("LazySync", true);
        forceFluidLevelUpdate = false;
    }

    @Override
    public void initialize() {
        super.initialize();
        sendData();
    }

    @Override
    public void invalidate() {
        if (itemInventory != null || fluidCapability != null) invalidateCapabilities();
        super.invalidate();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        itemInventory.setChanged();
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

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public ItemStack getStack() {
        return getItemInventory().getFirstItem();
    }

    public MelterInventory getItemInventory() {
        return itemInventory;
    }

    public SmartFluidTank getTankInventory() {
        return tankInventory;
    }

    public LerpedFloat getFluidLevel() {
        return fluidLevel;
    }

    @SuppressWarnings("unused")
    public void setFluidLevel(LerpedFloat fluidLevel) {
        this.fluidLevel = fluidLevel;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        if (itemInventory == null) itemInventory = new MelterInventory(1, this);
        return new MelterMenu(i, inventory, itemInventory, worldPosition);
    }

    @Override
    public void writeClientSideData(@NotNull AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(worldPosition);
    }
}
