package dev.lopyluna.slag.content.blocks.table;

import dev.lopyluna.slag.content.blocks.DirtyInventory;
import dev.lopyluna.slag.content.items.dynamic_mold.DynamicMoldItem;
import dev.lopyluna.slag.register.AllDataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TableInventory extends DirtyInventory<TableBE> {
    TableBE be;

    public TableInventory(int slots, TableBE be) {
        super(slots, be, 1, false);
        this.be = be;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!(stack.getItem() instanceof DynamicMoldItem) || !stack.has(AllDataComponents.CAST_TYPE) || !getFirstMoldItem().isEmpty()) return stack;
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        validateSlotIndex(slot);
        var existing = this.stacks.get(slot);
        if (existing.getItem() instanceof DynamicMoldItem) return ItemStack.EMPTY;
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        var i = slot;
        if (getItem(slot).getItem() instanceof DynamicMoldItem) i = 1;
        stacks.set(i, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
        onContentsChanged(i);
    }

    public ItemStack getFirstMoldItem() {
        for (var s : stacks) if (!s.isEmpty() && s.getItem() instanceof DynamicMoldItem) return s;
        return ItemStack.EMPTY;
    }
    @Override
    public ItemStack getFirstItem() {
        for (var s : stacks) if (!s.isEmpty() && !(s.getItem() instanceof DynamicMoldItem)) return s;
        return ItemStack.EMPTY;
    }

    @Override
    public void setChanged() {
        be.sendDataImmediately();
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        be.sendDataImmediately();
    }
    @Override public boolean stillValid(@NotNull Player player) {
        return Container.stillValidBlockEntity(be, player);
    }
}
