package org.jlortiz.playercollars.client;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.joml.Quaternionf;

public class PawRenderer implements TrinketRenderer {
    private static void renderForArm(ItemStack stack, MatrixStack matrices, PlayerEntityModel<? extends LivingEntity> model, World world, VertexConsumerProvider multiBufferSource, int light, boolean left) {
        matrices.push();
        (left ? model.leftArm : model.rightArm).rotate(matrices);
        matrices.multiply(new Quaternionf().rotateXYZ(0, (float) (left ? Math.PI : -Math.PI)/ 2, 0));
        matrices.translate(0, 0.5625, -0.015625);
        matrices.scale(0.75f / 2, 0.625f / 2, (model.thinArms ? 0.9375f : 1.125f) / 2);
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, multiBufferSource, world, 0);
        matrices.pop();
    }

    public static void renderOnFirstPerson(ItemStack stack, MatrixStack matrices, PlayerEntityModel<? extends LivingEntity> model, World world, VertexConsumerProvider multiBufferSource, int light, boolean left) {
        ItemStack is = stack.copy();
        is.remove(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        is.remove(DataComponentTypes.ENCHANTMENTS);

        matrices.push();
        (left ? model.leftArm : model.rightArm).rotate(matrices);
        matrices.translate(left ? 0.0625f : -0.046875f, 0.5625, -0.0625f);
        if (model.thinArms) matrices.translate(0.03125f * (left ? -1 : 1), 0, 0);
        matrices.scale((model.thinArms ? 0.59375f : 0.75f) / 2, 0.75f / 2, 1.03125f / 2);
        MinecraftClient.getInstance().getItemRenderer().renderItem(is, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, multiBufferSource, world, 0);
        matrices.pop();
    }

    @Override
    public void render(ItemStack itemStack, SlotReference slotReference, EntityModel<? extends LivingEntity> entityModel, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity livingEntity, float v, float v1, float v2, float v3, float v4, float v5) {
        if (!(entityModel instanceof PlayerEntityModel<? extends LivingEntity> model)) return;

        ItemStack is = itemStack.copy();
        is.remove(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        is.remove(DataComponentTypes.ENCHANTMENTS);
        renderForArm(is, matrixStack, model, livingEntity.getWorld(), vertexConsumerProvider, i, false);
        renderForArm(is, matrixStack, model, livingEntity.getWorld(), vertexConsumerProvider, i, true);
    }
}
