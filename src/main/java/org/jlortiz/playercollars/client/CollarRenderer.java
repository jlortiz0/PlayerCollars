package org.jlortiz.playercollars.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class CollarRenderer implements ICurioRenderer {
    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack matrixStack,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource bufferSource,
            int light, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> biped)) return;
        try {
            ModelPart body = biped.body;
            boolean hasChestplate = slotContext.entity().getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorItem;
            matrixStack.pushPose();
            matrixStack.translate(body.x * 0.0625f, body.y * 0.0625f, body.z * 0.0625f);
            matrixStack.mulPose(new Quaternionf().rotateXYZ(body.xRot, body.yRot, body.zRot + (float) Math.PI));
            matrixStack.scale(
                    (hasChestplate ? 0.7f : 0.85f) * body.xScale,
                    0.85f * body.yScale,
                    (hasChestplate ? 1.1f : 0.85f) * body.zScale);
            matrixStack.translate(0, hasChestplate ? 0.475 : 0.4125, -0.005);
            net.minecraft.client.Minecraft.getInstance().getItemRenderer()
                    .renderStatic(stack, net.minecraft.world.item.ItemDisplayContext.HEAD, light,
                            OverlayTexture.NO_OVERLAY, matrixStack, bufferSource,
                            slotContext.entity().level(), 0);
            matrixStack.popPose();
        } catch (ClassCastException ignored) {}
    }
}
