package dev.lopyluna.slag.client;

import dev.lopyluna.slag.content.items.modular.ModularItem;
import dev.lopyluna.slag.register.AllLangs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

@SuppressWarnings("unused")
public class ClientTooltips {

    public static void appendHoverTextModularTool(ModularItem item, ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (context.level() instanceof ClientLevel level) {
            var parts = item.getParts(stack);
            if (parts != null && !parts.isEmpty()) {
                var copyParts = parts.itemsCopy();
                if (copyParts.isEmpty()) {
                    tooltip.add(AllLangs.tr("modular_tool_waiting").withStyle(ChatFormatting.GRAY));
                    return;
                }
                //if (!item.hasModularType(stack)) {
                //    var player = Minecraft.getInstance().player;
                //    var tier = item.getTier(stack);

                //    //if (3.5f >= tier) tooltip.add(AllLangs.tr("modular_tool_crafting_hammer_weak").withStyle(ChatFormatting.RED));
                //    //else tooltip.add(AllLangs.trArgs("modular_tool_crafting_hammer", String.valueOf(tier)).withStyle(ChatFormatting.RED));
                //}

                if (!item.hasModularType(stack)) AllLangs.modularToolStats(tooltip, parts, stack, item);
                else if (isTool(item, stack)) AllLangs.modularToolStats(tooltip, parts, stack, item);
                AllLangs.modularParts(tooltip, copyParts);
            } else tooltip.add(AllLangs.tr("modular_tool_waiting").withStyle(ChatFormatting.GRAY));
        }
    }


    public static boolean isTool(ModularItem item, ItemStack stack) {
        var modularType = item.getModularType(stack);
        return modularType != null && modularType.actions.contains("isTool");
    }
}
