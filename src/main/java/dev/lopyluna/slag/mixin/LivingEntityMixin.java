package dev.lopyluna.slag.mixin;

import dev.lopyluna.slag.content.items.modular.ModularEquipablesItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {super(entityType, level);}

    // LivingEntity#onEquipItem(EquipmentSlot, ItemStack, ItemStack)
    @Inject(method = "onEquipItem(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"))
    private void equippedItemSound(EquipmentSlot slot, ItemStack oldItem, ItemStack newItem, CallbackInfo ci) {
        var self = (LivingEntity)(Object)this;
        boolean flag = newItem.isEmpty() && oldItem.isEmpty();
        if (!flag && !ItemStack.isSameItemSameComponents(oldItem, newItem) && !firstTick) {
            if (!level().isClientSide() && !isSpectator()) {
                if (!isSilent() && newItem.getItem() instanceof ModularEquipablesItem item && item.getEquipmentSlot(newItem) == slot) level().playSeededSound(null, self, item.getEquipSound(newItem), getSoundSource(), 1.0F, 1.0F, random.nextLong());
            }
        }
    }
}
