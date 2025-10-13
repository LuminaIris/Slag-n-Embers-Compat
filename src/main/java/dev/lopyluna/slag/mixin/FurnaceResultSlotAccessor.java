package dev.lopyluna.slag.mixin;

import net.minecraft.world.inventory.FurnaceResultSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FurnaceResultSlot.class)
public interface FurnaceResultSlotAccessor {
    @Accessor("removeCount")
    int getRemoveCount();
    @Accessor("removeCount")
    void setRemoveCount(int value);
}
