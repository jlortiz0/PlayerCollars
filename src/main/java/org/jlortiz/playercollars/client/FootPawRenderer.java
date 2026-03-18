package org.jlortiz.playercollars.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;

import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class FootPawRenderer implements ICurioRenderer {

    private static void renderForLeg(ItemStack stack, PoseStack matrices,
                                      PlayerModel<? extends LivingEntity> model,
                                      Level world, MultiBufferSource buffers, int light, boolean left) {
        matrices.pushPose();
        (left ? model.leftLeg : model.rightLeg).translateAndRotate(matrices);
        matrices.mulPose(new Quaternionf().rotateXYZ((float) Math.PI / 2, 0, 0));
        matrices.translate(0, 0, -0.675);
        matrices.scale(0.75f / 2, 0.75f / 2, 0.75f / 2);
        Minecraft.getInstance().getItemRenderer()
                .renderStatic(stack, net.minecraft.world.item.ItemDisplayContext.FIXED, light,
                        OverlayTexture.NO_OVERLAY, matrices, buffers, world, 0);
        matrices.popPose();
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack matrices,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffers,
            int light, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(renderLayerParent.getModel() instanceof PlayerModel<? extends LivingEntity> model)) return;
        renderForLeg(stack, matrices, model, slotContext.entity().level(), buffers, light, false);
        renderForLeg(stack, matrices, model, slotContext.entity().level(), buffers, light, true);
    }
}
