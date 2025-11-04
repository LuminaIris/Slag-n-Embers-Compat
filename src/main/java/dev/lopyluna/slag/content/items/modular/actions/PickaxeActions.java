package dev.lopyluna.slag.content.items.modular.actions;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.List;

@SuppressWarnings("unused")
public class PickaxeActions {
    public static final PickaxeActions INSTANCE = new PickaxeActions();

    public Object doAction(String action, List<Object> args) {
        return switch (action) {
            case "isTool" -> true;
            case "useOn" -> useOn((UseOnContext) args.getFirst());
            case "canPerformAction" -> canPerformAction((ItemStack) args.getFirst(), (ItemAbility) args.getLast());
            default -> null;
        };
    }

    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(itemAbility);
    }
}
