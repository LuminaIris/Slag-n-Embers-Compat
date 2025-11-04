package dev.lopyluna.slag.content.blocks.crucible;

import dev.lopyluna.slag.content.utils.NBTHelper;
import dev.lopyluna.slag.register.AllRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class CrucibleTank extends FluidTank {
    public List<FluidStack> fluids = new ArrayList<>();
    private final Consumer<List<FluidStack>> updateCallback;

    public CrucibleTank(int capacity, Consumer<List<FluidStack>> updateCallback) {
        super(capacity);
        this.updateCallback = updateCallback;
        fluids.add(FluidStack.EMPTY);
    }

    @Override
    protected void onContentsChanged() {
        updateCallback.accept(getFluids());
    }

    public List<FluidStack> getFluids() {
        compress();
        return fluids;
    }

    public List<FluidStack> getFluidCopy() {
        if (getFluids() == null || fluids.isEmpty()) return new ArrayList<>();
        return getFluids().stream().filter(s -> !s.isEmpty()).map(FluidStack::copy).toList();
    }

    public void clear() {
        fluids.clear();
        onContentsChanged();
    }

    public boolean containsLiquid(Fluid fluid) {
        if (fluids.isEmpty()) return false;
        for (var target : fluids) if (target.is(fluid)) return true;
        return false;
    }

    public boolean containsHotLiquid() {
        if (fluids.isEmpty()) return false;
        for (var fluid : fluids) if (fluid.getFluidType().getTemperature() >= 1000) return true;
        return false;
    }

    public boolean canExtinguishEntity(Entity entity) {
        if (containsHotLiquid()) return false;
        for (var fluid : fluids) if (fluid.getFluidType().canExtinguish(entity)) return true;
        return false;
    }

    public void noCoverTick() {
        List<FluidStack> list = this.fluids;
        if (list == null || list.isEmpty()) return;

        for (var fluid : fluids) {
            if (fluid.isEmpty()) continue;
            if (!fluid.getFluidType().isLighterThanAir()) continue;
            fluid.shrink(25);
        }
        onContentsChanged();
        compress();
    }

    public boolean moveFluidToFront(int targetIdx) {
        List<FluidStack> list = this.fluids;
        if (list == null || list.isEmpty()) return false;
        if (targetIdx == 0) {
            var f = list.removeFirst();
            list.addLast(f);
            onContentsChanged();
            compress();
            return true;
        }
        if (targetIdx <= 0 || targetIdx >= list.size()) return false;

        var sel = list.remove(targetIdx);
        list.addFirst(sel);
        onContentsChanged();
        compress();
        return true;
    }

    public @NotNull FluidStack getFluidFiltered(Fluid fluid) {
        if (fluids.isEmpty()) return FluidStack.EMPTY;
        for (var target : fluids) if (target.is(fluid)) return target;
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack getFluid() {
        return fluids.isEmpty() ? FluidStack.EMPTY : fluids.getFirst();
    }

    public int getFluidAmount() {
        return getFluid().getAmount();
    }

    public int getTotalFluidAmount() {
        var i = 0;
        for (var fluid : fluids) if (!fluid.isEmpty()) i += fluid.getAmount();
        return i;
    }

    @Override
    public @NotNull FluidTank readFromNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        fluids.clear();
        fluids = NBTHelper.readFluidList(nbt.getList("Fluids", Tag.TAG_COMPOUND), provider);
        return this;
    }

    @Override
    public @NotNull CompoundTag writeToNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        nbt.put("Fluids", NBTHelper.writeFluidList(fluids, provider));
        return nbt;
    }

    @SuppressWarnings("all")
    @Override
    public boolean isFluidValid(FluidStack stack) {
        return this.validator != null && super.isFluidValid(stack);
    }

    public int fill(List<FluidStack> resources, IFluidHandler.FluidAction action) {
        if (resources.isEmpty()) return 0;
        var i = 0;
        for (var f : resources) i += fill(f, action);
        return i;
    }

    @SuppressWarnings("all")
    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource == null || resource.isEmpty() || validator == null || !isFluidValid(resource) || action == null) return 0;

        int match = findFirstMatch(resource);
        int spaceTotal = getSpace();

        if (match >= 0) {
            int fillable = Math.min(spaceTotal, resource.getAmount());
            if (fillable <= 0) return 0;
            if (action.simulate()) return fillable;

            fluids.get(match).grow(fillable);
            onContentsChanged();
            return fillable;
        }
        int fillable = Math.min(spaceTotal, resource.getAmount());
        if (fillable <= 0) return 0;
        if (action.simulate()) return fillable;

        fluids.add(resource.copyWithAmount(fillable));
        if (fluids.getFirst().isEmpty() && fluids.size() > 1) moveFirstNonEmptyToFront();
        onContentsChanged();
        return fillable;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty()) return FluidStack.EMPTY;
        compress();

        int matchIndex = findFirstMatch(resource);
        if (matchIndex < 0) return FluidStack.EMPTY;

        FluidStack matched = fluids.get(matchIndex);
        if (matched.isEmpty()) return FluidStack.EMPTY;

        int drained = Math.min(matched.getAmount(), resource.getAmount());
        FluidStack out = matched.copyWithAmount(drained);

        if (action.execute() && drained > 0) {
            matched.shrink(drained);
            onContentsChanged();
        }

        return out;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        compress();
        if (maxDrain <= 0 || fluids.isEmpty()) return FluidStack.EMPTY;

        var top = fluids.getFirst();
        if (top.isEmpty()) return FluidStack.EMPTY;

        int drained = Math.min(top.getAmount(), maxDrain);
        FluidStack out = top.copyWithAmount(drained);

        if (action.execute() && drained > 0) {
            top.shrink(drained);
            onContentsChanged();
        }
        return out;
    }

    @Override
    public void setFluid(@NotNull FluidStack stack) {
        if (fluids.isEmpty()) fluids.add(stack);
        else fluids.set(0, stack);
        compress();
        updateCallback.accept(fluids);
    }

    @Override
    public int getTanks() {
        return fluids.size();
    }

    @Override
    public boolean isEmpty() {
        return getTotalFluidAmount() == 0;
    }

    public int getSpace() {
        return Math.max(0, this.capacity - getTotalFluidAmount());
    }

    private int findFirstMatch(FluidStack target) {
        for (int i = 0; i < fluids.size(); i++) {
            FluidStack f = fluids.get(i);
            if (!f.isEmpty() && FluidStack.isSameFluidSameComponents(f, target)) return i;
        }
        return -1;
    }

    private void compress() {
        fluids.removeIf(FluidStack::isEmpty);
        for (int i = 0; i + 1 < fluids.size(); ) {
            FluidStack a = fluids.get(i);
            FluidStack b = fluids.get(i + 1);
            if (FluidStack.isSameFluidSameComponents(a, b)) {
                a.grow(b.getAmount());
                fluids.remove(i + 1);
            } else i++;
        }
        if (fluids.isEmpty()) fluids.add(FluidStack.EMPTY);
        int overflow = Math.max(0, getTotalFluidAmount() - this.capacity);
        if (overflow > 0) trimFromEnd(overflow);
    }

    private void moveFirstNonEmptyToFront() {
        for (int i = 1; i < fluids.size(); i++) if (!fluids.get(i).isEmpty()) {
            FluidStack f = fluids.remove(i);
            fluids.set(0, f);
            return;
        }
    }

    private void trimFromEnd(int toRemove) {
        for (int i = fluids.size() - 1; i >= 0 && toRemove > 0; i--) {
            FluidStack f = fluids.get(i);
            int take = Math.min(f.getAmount(), toRemove);
            f.shrink(take);
            toRemove -= take;
            if (f.isEmpty()) fluids.remove(i);
        }
        if (fluids.isEmpty()) fluids.add(FluidStack.EMPTY);
    }

    public boolean tryAlloy(Level level) { return tryAlloy(level, 1); }

    public boolean tryAlloy(Level level, int maxCraftsPerCall) {
        if (level.isClientSide) return false;

        boolean changed = false;
        int crafts = 0;

        fluids.removeIf(FluidStack::isEmpty);
        if (fluids.isEmpty()) return false;

        RecipeManager rm = level.getRecipeManager();
        var holders = rm.getAllRecipesFor(AllRecipes.ALLOYING.get());
        if (holders.isEmpty()) return false;
        var recipes = holders.stream().map(RecipeHolder::value).toList();
        if (recipes.isEmpty()) return false;

        while (crafts < maxCraftsPerCall) {
            AlloyingRecipe r = findCraftable(recipes);
            if (r == null) break;

            FluidStack out = r.getOutput();
            if (!canAccept(out)) break;

            for (FluidStack needed : r.getInputs()) subtractAcross(needed, needed.getAmount());
            mergeIn(out);
            crafts++;
            changed = true;
        }

        coalesce();
        if (fluids.isEmpty()) fluids.add(FluidStack.EMPTY);
        return changed;
    }

    private AlloyingRecipe findCraftable(List<AlloyingRecipe> recipes) {
        for (var r : recipes) if (canCraft(r)) return r;
        return null;
    }

    private boolean canCraft(AlloyingRecipe r) {
        var req = r.getInputs();
        if (req == null || req.isEmpty()) return false;
        for (var needed : req) {
            if (needed.isEmpty() || needed.getAmount() <= 0) return false;
            if (countOf(needed) < needed.getAmount()) return false;
        }
        return true;
    }
    private boolean canAccept(FluidStack add) {
        return !add.isEmpty();
    }

    private static boolean sameKind(FluidStack a, FluidStack b) {
        return FluidStack.isSameFluidSameComponents(a, b);
    }

    private int countOf(FluidStack kind) {
        int total = 0;
        for (var f : fluids) if (!f.isEmpty() && sameKind(f, kind)) total += f.getAmount();
        return total;
    }

    private void subtractAcross(FluidStack kind, int amount) {
        if (amount <= 0) return;
        for (int i = 0; i < fluids.size() && amount > 0; i++) {
            FluidStack f = fluids.get(i);
            if (f.isEmpty() || !sameKind(f, kind)) continue;
            int take = Math.min(f.getAmount(), amount);
            f.shrink(take);
            amount -= take;
            if (f.isEmpty()) { fluids.remove(i); i--; }
        }
    }

    private void mergeIn(FluidStack add) {
        if (add.isEmpty()) return;
        for (var f : fluids) if (!f.isEmpty() && sameKind(f, add)) {
            f.grow(add.getAmount());
            return;
        }
        fluids.add(add.copy());
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (tank >= fluids.size() || 0 >= tank) return super.getFluidInTank(tank);
        return fluids.get(tank);
    }

    private void coalesce() {
        if (fluids.isEmpty()) return;
        List<FluidStack> out = new ArrayList<>(fluids.size());
        for (var f : fluids) {
            if (f.isEmpty()) continue;
            boolean merged = false;
            for (var g : out) if (sameKind(f, g)) {
                g.grow(f.getAmount());
                merged = true;
                break;
            }
            if (!merged) out.add(f.copy());
        }
        fluids.clear();
        fluids.addAll(out);
    }
}
