package dev.lopyluna.slag.content.blocks.smart;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class SmartBlockEntity extends CachedRenderBBBlockEntity {

    private final Map<BehaviourType<?>, BlockEntityBehaviour> behaviours = new Reference2ObjectArrayMap<>();
    private boolean initialized = false;
    private boolean firstNbtRead = true;
    protected int lazyTickRate;
    protected int lazyTickCounter;
    private boolean chunkUnloaded;

    public SmartBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        setLazyTickRate(10);

        ArrayList<BlockEntityBehaviour> list = new ArrayList<>();
        addBehaviours(list);
        list.forEach(b -> behaviours.put(b.getType(), b));
    }

    public abstract void addBehaviours(List<BlockEntityBehaviour> behaviours);

    /**
     * Gets called just before reading block entity data for behaviours. Register
     * anything here that depends on your custom BE data.
     */
    @SuppressWarnings("unused")
    public void addBehavioursDeferred(List<BlockEntityBehaviour> behaviours) {}

    public void initialize() {
        if (firstNbtRead) {
            firstNbtRead = false;
            //NeoForge.EVENT_BUS.post(new BlockEntityBehaviourEvent(this, behaviours));
        }

        forEachBehaviour(BlockEntityBehaviour::initialize);
        lazyTick();
    }

    public void tick() {
        if (!initialized && hasLevel()) {
            initialize();
            initialized = true;
        }

        if (lazyTickCounter-- <= 0) {
            lazyTickCounter = lazyTickRate;
            lazyTick();
        }

        forEachBehaviour(BlockEntityBehaviour::tick);
    }

    public void lazyTick() {}

    /**
     * Hook only these in future subclasses of STE
     */
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.saveAdditional(tag, registries);
        forEachBehaviour(tb -> tb.write(tag, registries, clientPacket));
    }

    /**
     * Hook only these in future subclasses of STE
     */
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        if (firstNbtRead) {
            firstNbtRead = false;
            ArrayList<BlockEntityBehaviour> list = new ArrayList<>();
            addBehavioursDeferred(list);
            list.forEach(b -> behaviours.put(b.getType(), b));
            //NeoForge.EVENT_BUS.post(new BlockEntityBehaviourEvent(this, behaviours));
        }
        super.loadAdditional(tag, registries);
        forEachBehaviour(tb -> tb.read(tag, registries, clientPacket));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        read(tag, registries, false);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        chunkUnloaded = true;
    }

    @Override
    public final void setRemoved() {
        super.setRemoved();
        if (!chunkUnloaded) remove();
        invalidate();
    }

    /**
     * Block destroyed or Chunk unloaded. Usually invalidates capabilities
     */
    public void invalidate() {
        forEachBehaviour(BlockEntityBehaviour::unload);
    }

    /**
     * Block destroyed or picked up by a contraption. Usually detaches kinetics
     */
    public void remove() {}

    /**
     * Block destroyed or replaced. Requires Block to call IBE::onRemove
     */
    public void destroy() {
        forEachBehaviour(BlockEntityBehaviour::destroy);
    }

    @Override
    public final void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        write(tag, registries, false);
    }

    @Override
    public final void readClient(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        read(tag, registries, true);
    }

    @Override
    public final CompoundTag writeClient(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        write(tag, registries, true);
        return tag;
    }

    @SuppressWarnings("unchecked")
    public <T extends BlockEntityBehaviour> T getBehaviour(BehaviourType<T> type) {
        return (T) behaviours.get(type);
    }

    public void forEachBehaviour(Consumer<BlockEntityBehaviour> action) {
        getAllBehaviours().forEach(action);
    }

    public Collection<BlockEntityBehaviour> getAllBehaviours() {
        return behaviours.values();
    }

    public void setLazyTickRate(int slowTickRate) {
        this.lazyTickRate = slowTickRate;
        this.lazyTickCounter = slowTickRate;
    }
}
