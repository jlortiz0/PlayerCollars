package org.jlortiz.playercollars.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class CollarRenderer implements ICurioRenderer {

    private final BakedModel model;
    public CollarRenderer(BakedModel m) {
        this.model = m;
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int i, float v, float v1, float v2, float v3, float v4, float v5) {
        poseStack.pushPose();
        try {
            ModelPart body = ((HumanoidModel<T>) renderLayerParent.getModel()).body;
            poseStack.translate(body.x * 0.0625f, body.y * 0.0625f, body.z * 0.0625f);
            poseStack.mulPose(new Quaternion(body.xRot, body.yRot, body.zRot + (float) Math.PI, false));
            poseStack.scale(0.8f * body.xScale, 0.8f * body.yScale, 0.8f * body.zScale);
            poseStack.translate(0, 0.4, 0);
            Minecraft.getInstance().getItemRenderer().render(itemStack, ItemTransforms.TransformType.HEAD, false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, model);
        } catch (ClassCastException ignored) {}
        poseStack.popPose();
    }
}
