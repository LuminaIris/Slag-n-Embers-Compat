package dev.lopyluna.slag.content.items.old;

import dev.lopyluna.slag.content.items.modular.DataDynamicParts;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class ModularToolItem extends Item {
    public ModularToolItem(Properties properties) {
        super(properties.stacksTo(1).component(AllDataComponents.TOOL_PARTS, DataDynamicParts.EMPTY));
    }

    public DataDynamicParts getParts(ItemStack pStack) {
        return pStack.get(AllDataComponents.TOOL_PARTS);
    }
    public void setParts(ItemStack pStack, List<ItemStack> pStacks) {
        pStack.set(AllDataComponents.TOOL_PARTS, new DataDynamicParts(pStacks));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof Player player) || level.isClientSide) return;
        player.getInventory().setItem(slotId, transmuteTool(stack));

    }

    public ItemStack transmuteTool(ItemStack pStack) {
        var parts = getParts(pStack);
        var i = 0;
        for (var part : parts.items) {
            if (part.getItem() instanceof ModularToolPartItem item) part = item.transmuteStack(part);
            parts.items.set(i, part);
            i++;
        }
        var item = AllItems.MODULAR_ITEM.get();
        var newStack = item.getDefaultInstance();
        newStack.set(AllDataComponents.DYNAMIC_PARTS, parts);
        if (pStack.getItem() instanceof BakedModularToolItem) {
            var modular = item.getModularTypeFromParts(parts);
            if (modular != null) {
                var type = modular.id;
                newStack.set(AllDataComponents.BAKED, type);
                newStack.set(AllDataComponents.MODULAR_TYPE, type);
            }
        }
        return newStack;
    }
}
