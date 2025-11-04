package dev.lopyluna.slag.content.items.modular.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

@SuppressWarnings("unused")
public class ShovelActions {
    public static final ShovelActions INSTANCE = new ShovelActions();

    public Object doAction(String action, List<Object> args) {
        return switch (action) {
            case "isTool" -> true;
            case "useOn" -> useOn((UseOnContext) args.getFirst());
            case "canPerformAction" -> canPerformAction((ItemStack) args.getFirst(), (ItemAbility) args.getLast());
            default -> null;
        };
    }

    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (context.getClickedFace() == Direction.DOWN) return InteractionResult.PASS;
        else { Player player = context.getPlayer();
            BlockState flatten = blockstate.getToolModifiedState(context, net.neoforged.neoforge.common.ItemAbilities.SHOVEL_FLATTEN, false);
            BlockState state;
            if (flatten != null && level.getBlockState(blockpos.above()).isAir()) {
                level.playSound(player, blockpos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                state = flatten;
            } else if ((state = blockstate.getToolModifiedState(context, net.neoforged.neoforge.common.ItemAbilities.SHOVEL_DOUSE, false)) != null) {
                if (!level.isClientSide()) level.levelEvent(null, 1009, blockpos, 0);
            } if (state != null) {
                if (!level.isClientSide) {
                    level.setBlock(blockpos, state, 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(player, state));
                    if (player != null) context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else return InteractionResult.PASS;
        }
    }

    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(itemAbility);
    }
}
