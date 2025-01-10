package org.jlortiz.playercollars.client;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.registry.tag.ItemTags;
import org.joml.Quaternionf;

public class CollarRenderer implements TrinketRenderer {

    @Override
    public void render(ItemStack itemStack, SlotReference slotReference, EntityModel<? extends EntityRenderState> entityModel, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity livingEntity, float v, float v1, float v2, float v3, float v4, float v5) {
        try {
            ModelPart body = ((PlayerEntityModel) entityModel).body;
            boolean hasChestplate = false;
            for (ItemStack is : slotReference.inventory().getComponent().getEntity().getArmorItems()) {
                if (is.isIn(ItemTags.CHEST_ARMOR)) {
                    hasChestplate = true;
                    break;
                }
            }
            matrixStack.translate(body.pivotX * 0.0625f, body.pivotY * 0.0625f, body.pivotZ * 0.0625f);
            matrixStack.multiply(new Quaternionf().rotateXYZ(body.pitch, body.yaw, body.roll + (float) Math.PI));
            matrixStack.scale((hasChestplate ? 0.7f : 0.85f) * body.xScale, 0.85f * body.yScale, (hasChestplate ? 1.1f : 0.85f) * body.zScale);
            matrixStack.translate(0, hasChestplate ? 0.475 : 0.4125, -0.005);
            MinecraftClient.getInstance().getItemRenderer().renderItem(itemStack, ModelTransformationMode.HEAD, i, OverlayTexture.DEFAULT_UV, matrixStack, vertexConsumerProvider, livingEntity.getWorld(), 0);
        } catch (ClassCastException ignored) {}
    }
}
