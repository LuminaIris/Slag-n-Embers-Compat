package dev.lopyluna.slag.content.items.dynamic_part;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lopyluna.slag.SlagEmbers;
import dev.lopyluna.slag.client.render.CustomRenderedItemModel;
import dev.lopyluna.slag.client.render.CustomRenderedItemModelRenderer;
import dev.lopyluna.slag.client.render.PartialItemModelRenderer;
import dev.lopyluna.slag.register.AllDataComponents;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DynamicPartRenderer extends CustomRenderedItemModelRenderer {
    @Override protected void render(ItemStack stack, ItemRenderer itemRenderer, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
                                    ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buf, int light, int overlay) {
        var shaper = itemRenderer.getItemModelShaper();
        var manager = shaper.getModelManager();
        renderer.render(renderFallBack(stack, manager), light);
    }

    public static BakedModel renderFallBack(ItemStack stack, ModelManager manager) {
        var missing = manager.getMissingModel();
        if (!(stack.getItem() instanceof IDynamicPart dynamicPart)) return missing;
        var hasMat = stack.has(AllDataComponents.MATERIAL_TYPE);
        if (!hasMat) return missing;
        var hasPart = stack.has(AllDataComponents.PART_TYPE);
        if (!hasPart) return missing;
        var mat = dynamicPart.getMaterialType(stack).orElse(null);
        if (mat == null) return missing;
        var part = dynamicPart.getPartType(stack).orElse(null);
        if (part == null) return missing;
        var matLoc = mat.id;
        var matID = matLoc.getPath();
        var partID = part.id.getPath();
        var targetMod = matLoc.getNamespace();

        var target = "item/dynamic_parts/" + matID + "/" + partID;
        var built = stack.get(AllDataComponents.BUILT);
        if (stack.has(AllDataComponents.BUILT) && built != null) target = "item/modular/" + built.getPath() + "/" + matID + "/" + partID;

        var modelLoc = ModelResourceLocation.standalone(SlagEmbers.loc(targetMod, target));
        return manager.getModel(modelLoc);
    }
}
