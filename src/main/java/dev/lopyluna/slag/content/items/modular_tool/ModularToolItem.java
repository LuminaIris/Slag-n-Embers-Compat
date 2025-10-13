package dev.lopyluna.slag.content.items.modular_tool;

import com.tterrag.registrate.util.RegistrateDistExecutor;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.ClientTooltips;
import dev.lopyluna.slag.client.render.SimpleCustomRenderer;
import dev.lopyluna.slag.register.AllDataComponents;
import dev.lopyluna.slag.register.AllItems;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

@ParametersAreNonnullByDefault
public class ModularToolItem extends Item {
    public ModularToolItem(Properties properties) {
        super(properties.stacksTo(1).component(AllDataComponents.TOOL_PARTS, DataToolParts.EMPTY));
    }

    public List<IToolPart> getToolParts(ItemStack pStack) {
        List<IToolPart> toolParts = new ArrayList<>();
        var pParts = getParts(pStack);
        if (pParts == null) return toolParts;
        var parts = pParts.itemsCopy();
        for (var stack : parts) if (stack.getItem() instanceof IToolPart part) toolParts.add(part);
        return toolParts;
    }
    public DataToolParts getParts(ItemStack pStack) {
        return pStack.get(AllDataComponents.TOOL_PARTS);
    }
    public void setParts(ItemStack pStack, List<ItemStack> pStacks) {
        pStack.set(AllDataComponents.TOOL_PARTS, new DataToolParts(pStacks));
    }

    @Override
    public @NotNull String getDescriptionId(ItemStack stack) {
        var mixture = getToolMixture(stack);
        var material = "";
        var pParts = getParts(stack);
        if (pParts == null) return super.getDescriptionId(stack);
        var parts = pParts.itemsCopy();
        for (var partStack : parts) if (partStack.getItem() instanceof IToolPart part) {
            material = part.getMaterialType().id;
            break;
        }

        return mixture.isEmpty() || material.isEmpty() ? super.getDescriptionId(stack) :
                Util.makeDescriptionId("item", SlagEmbers.loc(BuiltInRegistries.ITEM.getKey(this).getNamespace(), material + "_" + mixture));
    }

    public String getToolMixture(ItemStack pStack) {
        if (pStack.isEmpty()) return "";
        var parts = getParts(pStack);
        if (parts == null) return "";
        if (parts.hasAllPartSegments("pickaxe_head")) return "pickaxe";
        if (parts.hasAllPartSegments("axe_head")) return "axe";
        if (parts.hasAllPartSegments("shovel_head")) return "shovel";
        if (parts.hasAllPartSegments("hoe_head")) return "hoe";
        if (parts.hasAllPartSegments("sword_blade", "guard")) return "sword";

        if (parts.hasAllPartSegments("axe_head", "hoe_head")) return "mattock";
        if (parts.hasAllPartSegments("pickaxe_head", "shovel_head")) return "prybar";
        if (parts.hasAllPartSegments("shovel_head", "hoe_head")) return "graip";
        if (parts.hasAllPartSegments("pickaxe_head", "axe_head")) return "mallet";

        if (parts.hasAllPartSegments("pickaxe_head", "axe_head", "shovel_head")) return "hammer";
        if (parts.hasAllPartSegments("hoe_head", "sword_blade", "guard")) return "scythe";
        if (parts.hasAllPartSegments("pickaxe_head", "axe_head", "sword_blade")) return "maul";

        if (parts.hasAllPartSegments("pickaxe_head", "axe_head", "shovel_head", "hoe_head", "sword_blade")) return "paxel";
        return "";
    }

    public String getPureMixture(ItemStack pStack) {
        if (pStack.isEmpty()) return "";
        var parts = getParts(pStack);
        if (parts == null) return "";
        if (parts.hasAllPartSegments("axe_head", "hoe_head")) return "mattock";
        if (parts.hasAllPartSegments("pickaxe_head", "shovel_head")) return "prybar";
        if (parts.hasAllPartSegments("shovel_head", "hoe_head")) return "graip";
        if (parts.hasAllPartSegments("pickaxe_head", "axe_head")) return "mallet";

        if (parts.hasAllPartSegments("pickaxe_head", "axe_head", "shovel_head")) return "hammer";
        if (parts.hasAllPartSegments("hoe_head", "sword_blade", "guard")) return "scythe";
        if (parts.hasAllPartSegments("pickaxe_head", "axe_head", "sword_blade")) return "maul";

        if (parts.hasAllPartSegments("pickaxe_head", "axe_head", "shovel_head", "hoe_head", "sword_blade")) return "paxel";
        return "";
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (stack.getCount() != 1) return false;
        if (action == ClickAction.PRIMARY && slot.allowModification(player)) {
            var parts = getParts(stack);
            if (parts == null) return false;
            var toolParts = parts.itemsCopy();
            if (other.isEmpty()) {
                if (toolParts.isEmpty()) return false;
                var itemstack = !parts.contains(Items.STICK) ? toolParts.getFirst() : toolParts.getLast();
                playRemoveOneSound(player);
                access.set(itemstack);
                if (!parts.contains(Items.STICK)) toolParts.removeFirst();
                else toolParts.removeLast();
                setParts(stack, toolParts);
                return true;
            } else if (other.getItem() instanceof IToolPart part) {
                if (toolParts.size() > 5 || parts.containsPartSegment(part) || parts.contains(other) || parts.contains(Items.STICK)) plaFailSound(player);
                else {
                    playInsertSound(player);
                    var singleItem = other.copyWithCount(1);
                    toolParts.add(singleItem);
                    other.shrink(1);
                    setParts(stack, toolParts);
                }
                return true;
            } else if (other.is(Items.STICK)) {
                if (toolParts.isEmpty() || toolParts.size() > 6 || parts.contains(other) || other.getCount() > 3) plaFailSound(player);
                else {
                    playInsertSound(player);
                    toolParts.add(other.copy());
                    access.set(ItemStack.EMPTY);
                    setParts(stack, toolParts);
                }
                return true;
            } else if (testIsHammer(other)) {
                if (!parts.contains(Items.STICK) || testParts(parts) || !test(parts, player) || !testHammer(stack, other)) plaFailSound(player);
                else {
                    playBuildSound(player, testStation(player));
                    slot.set(stack.transmuteCopy(AllItems.BAKED_TOOL));
                    if (other.getItem() instanceof BakedModularToolItem) hurtAndBreak(other, 4, player.level(), player);
                    else other.shrink(1);
                }
                return true;
            }
        }
        return false;
    }

    public static void hurtAndBreak(ItemStack stack, int amount, Level level, @Nullable LivingEntity livingEntity) {
        if (level instanceof ServerLevel server && stack.isDamageableItem()) {
            Consumer<Item> onBreak = item -> {
                if (!stack.isEmpty() && livingEntity != null && !livingEntity.isSilent()) level.playLocalSound(
                        livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                        stack.getBreakingSound(), livingEntity.getSoundSource(),
                        0.8F, 0.8F + level.random.nextFloat() * 0.4F, false
                );
            };
            amount = stack.getItem().damageItem(stack, amount, livingEntity, onBreak);
            if (livingEntity == null || !livingEntity.hasInfiniteMaterials()) {
                if (amount > 0) {
                    amount = EnchantmentHelper.processDurabilityChange(server, stack, amount);
                    if (amount <= 0) return;
                }

                if (livingEntity instanceof ServerPlayer sp) if (amount != 0) CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(sp, stack, stack.getDamageValue() + amount);

                int i = stack.getDamageValue() + amount;
                stack.setDamageValue(i);
                if (i >= stack.getMaxDamage()) {
                    stack.shrink(1);
                    onBreak.accept(stack.getItem());
                }
            }
        }

    }

    public static boolean testParts(DataToolParts parts) {
        if (parts.hasAllPartSegments("pickaxe_head")) return false;
        if (parts.hasAllPartSegments("axe_head")) return false;
        if (parts.hasAllPartSegments("shovel_head")) return false;
        if (parts.hasAllPartSegments("hoe_head")) return false;
        if (parts.hasAllPartSegments("sword_blade", "guard")) return false;

        if (parts.hasAllPartSegments("axe_head", "hoe_head")) return false;
        if (parts.hasAllPartSegments("pickaxe_head", "shovel_head")) return false;
        if (parts.hasAllPartSegments("shovel_head", "hoe_head")) return false;
        if (parts.hasAllPartSegments("pickaxe_head", "axe_head")) return false;

        if (parts.hasAllPartSegments("pickaxe_head", "axe_head", "shovel_head")) return false;
        if (parts.hasAllPartSegments("hoe_head", "sword_blade", "guard")) return false;

        if (parts.hasAllPartSegments("pickaxe_head", "axe_head", "shovel_head", "hoe_head", "sword_blade")) return false;
        return !parts.hasAllPartSegments("pickaxe_head", "axe_head", "sword_blade");
    }

    public static int testRodCount(DataToolParts parts) {
        if (parts.hasAllPartSegments("pickaxe_head")) return 2;
        if (parts.hasAllPartSegments("axe_head")) return 2;
        if (parts.hasAllPartSegments("shovel_head")) return 2;
        if (parts.hasAllPartSegments("hoe_head")) return 2;
        if (parts.hasAllPartSegments("sword_blade", "guard")) return 1;

        if (parts.hasAllPartSegments("axe_head", "hoe_head")) return 2;
        if (parts.hasAllPartSegments("pickaxe_head", "shovel_head")) return 2;
        if (parts.hasAllPartSegments("shovel_head", "hoe_head")) return 2;
        if (parts.hasAllPartSegments("pickaxe_head", "axe_head")) return 2;

        if (parts.hasAllPartSegments("pickaxe_head", "axe_head", "shovel_head")) return 3;
        if (parts.hasAllPartSegments("hoe_head", "sword_blade", "guard")) return 3;
        if (parts.hasAllPartSegments("pickaxe_head", "axe_head", "sword_blade")) return 3;

        if (parts.hasAllPartSegments("pickaxe_head", "axe_head", "shovel_head", "hoe_head", "sword_blade")) return 3;
        return 0;
    }

    private void plaFailSound(Entity entity) {
        entity.playSound(SoundEvents.CRAFTER_FAIL, 1.25F, 0.5F + entity.level().getRandom().nextFloat() * 0.4F);
        entity.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5F, 0.5F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.DECORATED_POT_INSERT, 0.8F, 0.5F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.DECORATED_POT_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playBuildSound(Entity entity, @Nullable SoundEvent event) {
        entity.playSound(event != null ? event : SoundEvents.ANVIL_USE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    public boolean testIsHammer(ItemStack other) {
        if (other.is(ItemTags.STONE_TOOL_MATERIALS) || other.is(ItemTags.LOGS)) return true;
        else if (other.getItem() instanceof BakedModularToolItem item) {
            var otherParts = item.getParts(other);
            return otherParts.hasAllPartSegments("pickaxe_head", "axe_head", "shovel_head") || otherParts.hasAllPartSegments("pickaxe_head", "axe_head");
        }
        return false;
    }

    public boolean testHammer(ItemStack stack, ItemStack other) {
        var tier = averageMod(stack, IToolPart::getTough);
        if (other.is(ItemTags.STONE_TOOL_MATERIALS) || other.is(ItemTags.LOGS)) {
            return 3.5f >= tier;
        } else if (other.getItem() instanceof BakedModularToolItem item) {
            var otherParts = item.getParts(other);
            var otherTier = averageMod(other, IToolPart::getTough);
            if (Math.max(otherTier + 1.5f, 4f) >= tier && otherParts.hasAllPartSegments("pickaxe_head", "axe_head", "shovel_head")) return true;
            return Math.max(otherTier + 1f, 3.5f) >= tier && otherParts.hasAllPartSegments("pickaxe_head", "axe_head");
        }
        return false;
    }

    public SoundEvent testStation(Player player) {
        var level = player.level();
        var state = level.getBlockState(Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE).getBlockPos());
        if (state.is(BlockTags.ANVIL)) return SoundEvents.ANVIL_USE;
        if (state.is(Blocks.SMITHING_TABLE)) return SoundEvents.SMITHING_TABLE_USE;
        if (state.is(Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES)) return SoundEvents.CRAFTER_CRAFT;
        return null;
    }

    public float hammerTier(DataToolParts parts, ItemStack stack) {
        if (parts.hasAllPartSegments("pickaxe_head", "axe_head", "shovel_head")) return Math.max(averageMod(stack, IToolPart::getTough) + 1.5f, 4f);
        if (parts.hasAllPartSegments("pickaxe_head", "axe_head")) return Math.max(averageMod(stack, IToolPart::getTough) + 1f, 3.5f);
        return 0;
    }

    public boolean test(DataToolParts parts, Player player) {
        var level = player.level();
        var rodCount = testRodCount(parts);

        if (!parts.contains(Items.STICK)) {
            return false;
        } else if (rodCount != parts.getItem(Items.STICK).getCount()) {
           return false;
        } else {
            var lookState = level.getBlockState(Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE).getBlockPos());
            var size = parts.size();
            if (size > 4) return lookState.is(BlockTags.ANVIL);
            else if (size > 3) return lookState.is(Blocks.SMITHING_TABLE) || lookState.is(BlockTags.ANVIL);
            else return lookState.is(Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES) || lookState.is(Blocks.SMITHING_TABLE) || lookState.is(BlockTags.ANVIL);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        Level level = ctx.level();
        if (level == null) return;
        if (level.isClientSide()) RegistrateDistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientTooltips.appendHoverTextModularTool(this, stack, ctx, tooltip, flag));
    }

    public float averageMod(ItemStack stack, ToDoubleFunction<IToolPart> getter) {
        var parts = getToolParts(stack);
        if (parts.isEmpty()) return 0f;
        double sum = 0;
        for (var p : parts) sum += getter.applyAsDouble(p);
        return round2((float)(sum / parts.size()));
    }

    static float round2(float v) { return ((int) (v * 100f) / 100f); }

    @SuppressWarnings("removal")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new ModularToolRenderer()));
    }
}
