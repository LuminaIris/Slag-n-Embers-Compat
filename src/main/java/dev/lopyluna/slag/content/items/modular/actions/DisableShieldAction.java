package dev.lopyluna.slag.content.items.modular.actions;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.List;

@SuppressWarnings("unused")
public class DisableShieldAction {
    public static final DisableShieldAction INSTANCE = new DisableShieldAction();

    public Object doAction(String action, List<Object> args) {
        return switch (action) {
            case "canPerformAction" -> canPerformAction((ItemStack) args.getFirst(), (ItemAbility) args.getLast());
            case "canDisableShield" -> true;
            default -> null;
        };
    }

    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return false;
    }
}
