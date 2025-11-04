package dev.lopyluna.slag.content.items.modular.actions;

import dev.lopyluna.slag.register.AllTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;

public class HarvestActions {
    public static final HarvestActions INSTANCE = new HarvestActions();

    public Object doAction(String action, List<Object> args) {
        if (action.equals("useOn")) return useOn((UseOnContext) args.getFirst());
        return null;
    }

    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var blockpos = context.getClickedPos();
        var player = context.getPlayer();
        var state = level.getBlockState(blockpos);
        if (state.is(AllTags.HARVESTABLE)) {
            var properties = new ArrayList<>(List.of(BlockStateProperties.AGE_1, BlockStateProperties.AGE_2, BlockStateProperties.AGE_3, BlockStateProperties.AGE_4, BlockStateProperties.AGE_5, BlockStateProperties.AGE_7, BlockStateProperties.AGE_15, BlockStateProperties.AGE_25));
            var result = InteractionResult.PASS;
            var hasProperty = false;
            for (var property : properties) if (state.hasProperty(property)) {
                if (state.getValue(property) == property.getPossibleValues().size() - 1) {
                    state = state.setValue(property, 0);
                    level.destroyBlock(blockpos, true, player);
                    level.setBlockAndUpdate(blockpos, state);
                    result = InteractionResult.SUCCESS;
                }
                hasProperty = true;
                break;
            }
            if (!hasProperty) {
                level.destroyBlock(blockpos, true, player);
                result = InteractionResult.SUCCESS;
            }
            if (result == InteractionResult.SUCCESS && player != null) context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            return result;
        }
        return InteractionResult.PASS;
    }
}
