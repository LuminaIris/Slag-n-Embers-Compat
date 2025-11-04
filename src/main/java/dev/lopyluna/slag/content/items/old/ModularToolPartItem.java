package dev.lopyluna.slag.content.items.old;

import dev.lopyluna.slag.content.types.MaterialType;
import dev.lopyluna.slag.content.types.PartType;
import dev.lopyluna.slag.register.AllItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ModularToolPartItem extends Item {
    MaterialType type;
    PartType part;
    public ModularToolPartItem(MaterialType type, PartType part, Properties properties) {
        super(properties);
        this.type = type;
        this.part = part;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof Player player) || level.isClientSide) return;
        player.getInventory().setItem(slotId, transmuteStack(stack));
    }

    public ItemStack transmuteStack(ItemStack stack) {
        var part = AllItems.DYNAMIC_PART.get();
        var newStack = stack.transmuteCopy(part);
        if (this.part != null && this.type != null) {
            part.setMaterialType(newStack, this.type);
            part.setPartType(newStack, this.part);
        }
        return newStack;
    }

}
