package dev.lopyluna.slag.mixin;

import dev.lopyluna.slag.content.items.modular_tool.BakedModularToolItem;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.lopyluna.slag.content.AllUtils.matchesToolTag;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow public abstract Item getItem();

    // ItemStack#is(TagKey<Item>)
    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"), cancellable = true)
    private void isTag(TagKey<Item> tag, CallbackInfoReturnable<Boolean> cir) {
        var self = (ItemStack)(Object)this;
        if (self.getItem() instanceof BakedModularToolItem tool) {
            var value = matchesToolTag(tool, self, tag);
            if (value != null) cir.setReturnValue(value);
        }
    }

    // ItemStack#is(HolderSet<Item>)
    @Inject(method = "is(Lnet/minecraft/core/HolderSet;)Z", at = @At("HEAD"), cancellable = true)
    private void isHolder(HolderSet<Item> set, CallbackInfoReturnable<Boolean> cir) {
        var self = (ItemStack)(Object)this;
        if (!(self.getItem() instanceof BakedModularToolItem tool)) return;
        if (set instanceof HolderSet.Named<Item> named) {
            var value = matchesToolTag(tool, self, named.key());
            if (value != null) cir.setReturnValue(value);
        }
    }
}
