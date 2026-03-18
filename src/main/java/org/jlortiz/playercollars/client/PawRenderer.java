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

public class PawRenderer implements ICurioRenderer {

    private static void renderForArm(ItemStack stack, PoseStack matrices,
                                      PlayerModel<? extends LivingEntity> model,
                                      Level world, MultiBufferSource buffers, int light, boolean left) {
        matrices.pushPose();
        (left ? model.leftArm : model.rightArm).translateAndRotate(matrices);
        matrices.mulPose(new Quaternionf().rotateXYZ(0, (float)(left ? Math.PI : -Math.PI) / 2, 0));
        matrices.translate(0, 0.5625, -0.015625);
        boolean slim = isSlimModel(model);
        matrices.scale(0.75f / 2, 0.625f / 2, (slim ? 0.9375f : 1.125f) / 2);
        Minecraft.getInstance().getItemRenderer()
                .renderStatic(stack, net.minecraft.world.item.ItemDisplayContext.FIXED, light,
                        OverlayTexture.NO_OVERLAY, matrices, buffers, world, 0);
        matrices.popPose();
    }

    public static void renderOnFirstPerson(ItemStack stack, PoseStack matrices,
                                            PlayerModel<? extends LivingEntity> model,
                                            Level world, MultiBufferSource buffers, int light, boolean left) {
        matrices.pushPose();
        (left ? model.leftArm : model.rightArm).translateAndRotate(matrices);
        boolean slim = isSlimModel(model);
        matrices.translate(left ? 0.0625f : -0.046875f, 0.5625, -0.0625f);
        if (slim) matrices.translate(0.03125f * (left ? -1 : 1), 0, 0);
        matrices.scale((slim ? 0.59375f : 0.75f) / 2, 0.75f / 2, 1.03125f / 2);
        Minecraft.getInstance().getItemRenderer()
                .renderStatic(stack, net.minecraft.world.item.ItemDisplayContext.FIXED, light,
                        OverlayTexture.NO_OVERLAY, matrices, buffers, world, 0);
        matrices.popPose();
    }


    private static boolean isSlimModel(net.minecraft.client.model.PlayerModel<?> model) {
        try {
            java.lang.reflect.Field f = net.minecraft.client.model.PlayerModel.class.getDeclaredField("slim");
            f.setAccessible(true);
            return (boolean) f.get(model);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack matrices,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffers,
            int light, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(renderLayerParent.getModel() instanceof PlayerModel<? extends LivingEntity> model)) return;
        renderForArm(stack, matrices, model, slotContext.entity().level(), buffers, light, false);
        renderForArm(stack, matrices, model, slotContext.entity().level(), buffers, light, true);
    }
}
