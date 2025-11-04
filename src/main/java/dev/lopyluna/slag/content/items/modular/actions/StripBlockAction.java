package dev.lopyluna.slag.content.items.modular.actions;

import dev.lopyluna.slag.mixin.AxeItemAccessor;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public class StripBlockAction {
    public static final StripBlockAction INSTANCE = new StripBlockAction();

    public Object doAction(String action, List<Object> args) {
        return switch (action) {
            case "canPerformAction" -> canPerformAction((ItemStack) args.getFirst(), (ItemAbility) args.getLast());
            case "useOn" -> useOn((UseOnContext) args.getFirst());
            default -> null;
        };
    }


    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Player player = context.getPlayer();
        if (AxeItemAccessor.playerHasShieldUseIntent(context)) return InteractionResult.PASS;
        else {
            Optional<BlockState> optional = AxeActions.evaluateNewBlockState(level, blockpos, player, level.getBlockState(blockpos), context);
            if (optional.isEmpty()) return InteractionResult.PASS;
            else {
                ItemStack itemstack = context.getItemInHand();
                if (player instanceof ServerPlayer) CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockpos, itemstack);

                level.setBlock(blockpos, optional.get(), 11);
                level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(player, optional.get()));
                if (player != null) itemstack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
    }

    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return itemAbility == ItemAbilities.AXE_STRIP || itemAbility == ItemAbilities.AXE_SCRAPE || itemAbility == ItemAbilities.AXE_WAX_OFF;
    }
}
