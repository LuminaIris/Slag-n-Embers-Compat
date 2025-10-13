package dev.lopyluna.slag.content.blocks.basin;

import dev.lopyluna.slag.content.blocks.DirtyInventory;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class BasinInventory extends DirtyInventory<BasinBE> {
    BasinBE be;

    public BasinInventory(int slots, BasinBE be) {
        super(slots, be, 64, true);
        this.be = be;
        insertionAllowed = false;
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

    @Override
    public boolean stillValid(@NotNull Player player) {
        return Container.stillValidBlockEntity(be, player);
    }
}
