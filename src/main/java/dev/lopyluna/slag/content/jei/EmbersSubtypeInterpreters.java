package dev.lopyluna.slag.content.jei;

import dev.lopyluna.slag.register.AllDataComponents;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("all")
public class EmbersSubtypeInterpreters {
    public static final DynamicPartSubtype PART_INSTANCE = new DynamicPartSubtype();
    public static final ModularItemSubtype MODULAR_INSTANCE = new ModularItemSubtype();

    public static class DynamicPartSubtype extends SimpleInterpreterAdapter<ItemStack> {
        private DynamicPartSubtype() {
            super((stack, ctx) -> {
                var materialId = stack.get(AllDataComponents.MATERIAL_TYPE);
                var partId = stack.get(AllDataComponents.PART_TYPE);
                var builtId = stack.get(AllDataComponents.BUILT);

                if (materialId == null || partId == null) return "";

                var sb = new StringBuilder();
                sb.append("material:").append(materialId).append(';');
                sb.append("part:").append(partId).append(';');
                if (builtId != null) sb.append("built:").append(builtId).append(';');

                return sb.toString();
            });
        }
    }
    public static class ModularItemSubtype extends SimpleInterpreterAdapter<ItemStack> {
        private ModularItemSubtype() {
            super((stack, ctx) -> {
                var data = stack.get(AllDataComponents.DYNAMIC_PARTS);
                var bakedId = stack.get(AllDataComponents.BAKED);

                if (data == null || data.isEmpty()) return "";

                var sb = new StringBuilder();
                if (bakedId != null) sb.append("baked:").append(bakedId).append(';');

                for (var s : data.itemsCopy()) {
                    if (s.isEmpty()) continue;
                    var id = BuiltInRegistries.ITEM.getKey(s.getItem());
                    sb.append(id);

                    var modularId = s.get(AllDataComponents.BUILT);
                    var materialId = s.get(AllDataComponents.MATERIAL_TYPE);
                    var partId = s.get(AllDataComponents.PART_TYPE);
                    if (modularId != null) sb.append("[modular:").append(modularId).append("]");
                    if (materialId != null) sb.append("[mat:").append(materialId).append("]");
                    if (partId != null) sb.append("[part:").append(partId).append("]");

                    sb.append('#').append(s.getCount()).append(';');
                }
                return sb.toString();
            });
        }
    }

    @ParametersAreNonnullByDefault
    private static class SimpleInterpreterAdapter<T> implements ISubtypeInterpreter<T> {
        private final IInterpreter<T> interpreter;
        public SimpleInterpreterAdapter(IInterpreter<T> interpreter) { this.interpreter = interpreter; }
        @Override
        public Object getSubtypeData(T stack, UidContext ctx) {
            var result = interpreter.apply(stack, ctx);
            if (result.isEmpty()) return null;
            return result;
        }
        @Override public @NotNull String getLegacyStringSubtypeInfo(T stack, UidContext ctx) { return interpreter.apply(stack, ctx); }
    }
    public interface IInterpreter<T> {
        String apply(T ingredient, UidContext context);
    }
}
