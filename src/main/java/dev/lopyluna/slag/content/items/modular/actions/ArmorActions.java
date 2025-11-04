package dev.lopyluna.slag.content.items.modular.actions;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;

@SuppressWarnings("unused")
public class ArmorActions {
    public static final ArmorActions INSTANCE = new ArmorActions();

    public Object doAction(String action, EquipmentSlot slot, List<Object> args) {
        if (action.equals("isEquipable")) return slot;
        if (action.equals("getEquipmentSlot")) return slot;
        return null;
    }

}
