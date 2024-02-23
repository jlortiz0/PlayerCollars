package org.jlortiz.playercollars.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class CollarRenderer implements ICurioRenderer {

    private final BakedModel model;
    public CollarRenderer(BakedModel m) {
        this.model = m;
    }

    public static double y = 0.45;
    public static float scale = 0.8f;

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int i, float v, float v1, float v2, float v3, float v4, float v5) {
        poseStack.pushPose();
        poseStack.mulPose(new Quaternion(0, 0, 180, true));
        ModelPart body = ((HumanoidModel<T>) renderLayerParent.getModel()).body;
        poseStack.mulPose(new Quaternion(body.xRot, body.yRot, body.zRot, false));
        poseStack.translate(0, y, 0);
        poseStack.scale(scale, scale, scale);
        poseStack.scale(body.xScale, body.yScale, body.zScale);
        Minecraft.getInstance().getItemRenderer().render(itemStack, ItemTransforms.TransformType.HEAD, false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, model);
        poseStack.popPose();
    }
}
