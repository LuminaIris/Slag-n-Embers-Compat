package dev.lopyluna.slag.content.items.dynamic_mold;

import dev.lopyluna.slag.client.render.SimpleCustomRenderer;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllLangs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class DynamicMoldItem extends Item {
    public DynamicMoldItem(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        var hasType = stack.has(AllDataComponents.CAST_TYPE);
        var empty = other.isEmpty();
        if (action != ClickAction.SECONDARY || !slot.allowModification(player) || other.getItem() instanceof DynamicMoldItem) return false;
        if (!hasType && empty) return false;
        var level = player.level();
        var random = level.getRandom();
        var b = true;
        if (empty) {
            stack.remove(AllDataComponents.CAST_TYPE);
            player.playSound(SoundEvents.DECORATED_POT_INSERT_FAIL, 0.8F, 0.8F + random.nextFloat() * 0.4F);
            player.playSound(SoundEvents.CRAFTER_CRAFT, 0.8F, 0.8F + random.nextFloat() * 0.4F);
            b = false;
        } else {
            var type = stack.get(AllDataComponents.CAST_TYPE);
            for (var tag : other.getItem().builtInRegistryHolder().tags().toList()) if (tag.location().getPath().split("/")[0].equals("cast")) {
                if (type != null && type.equals(tag)) continue;
                stack.set(AllDataComponents.CAST_TYPE, tag);
                player.playSound(SoundEvents.DECORATED_POT_INSERT_FAIL, 0.8F, 0.8F + random.nextFloat() * 0.4F);
                player.playSound(SoundEvents.CRAFTER_CRAFT, 0.8F, 0.8F + random.nextFloat() * 0.4F);
                b = false;
                break;
            }
        }
        if (b) player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.8F, 0.8F + random.nextFloat() * 0.4F);
        return true;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var use = super.use(level, player, usedHand);
        if (!player.isShiftKeyDown()) return use;
        var stack = player.getItemInHand(usedHand);
        if (!stack.has(AllDataComponents.CAST_TYPE)) return use;
        stack.remove(AllDataComponents.CAST_TYPE);
        var random = level.getRandom();
        player.playSound(SoundEvents.DECORATED_POT_INSERT_FAIL, 0.8F, 0.8F + random.nextFloat() * 0.4F);
        player.playSound(SoundEvents.CRAFTER_CRAFT, 0.8F, 0.8F + random.nextFloat() * 0.4F);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, tooltip, flag);
        if (stack.has(AllDataComponents.CAST_TYPE)) tooltip.add(AllLangs.tr("clear_imprint").withStyle(ChatFormatting.GRAY));
        else tooltip.add(AllLangs.tr("imprint").withStyle(ChatFormatting.GRAY));
    }

    @SuppressWarnings("removal")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new DynamicMoldRenderer()));
    }
}
