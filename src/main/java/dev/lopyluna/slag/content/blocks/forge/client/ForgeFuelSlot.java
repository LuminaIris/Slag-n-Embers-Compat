package dev.lopyluna.slag.content.blocks.forge.client;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.world.inventory.FurnaceFuelSlot.isBucket;

@ParametersAreNonnullByDefault
public class ForgeFuelSlot extends Slot {
    private final ForgeMenu menu;
    public ForgeFuelSlot(ForgeMenu menu, Container container, int i, int j, int k) {
        super(container, i, j, k);
        this.menu = menu;
    }

    public boolean mayPlace(ItemStack stack) {
        return this.menu.isFuel(stack) || isBucket(stack);
    }

    public int getMaxStackSize(ItemStack stack) {
        return isBucket(stack) ? 1 : super.getMaxStackSize(stack);
    }
}
