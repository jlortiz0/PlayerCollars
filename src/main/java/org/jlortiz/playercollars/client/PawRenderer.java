package org.jlortiz.playercollars.client;

import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.client.Side;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.world.World;
import org.joml.Quaternionf;

public class PawRenderer implements AccessoryRenderer {

    private static void renderForArm(ItemStack stack, MatrixStack matrices, PlayerEntityModel model, World world, VertexConsumerProvider multiBufferSource, int light, boolean left) {
        matrices.push();
        AccessoryRenderer.transformToFace(matrices, left ? model.leftArm : model.rightArm, Side.BOTTOM);
        matrices.multiply(new Quaternionf().rotateXYZ((float) Math.PI, (float) (left ? Math.PI : -Math.PI)/ 2, 0));
        matrices.translate(0, -0.1875, -0.125);
        matrices.scale(0.75f, 0.625f, model.thinArms ? 0.875f : 1.03125f);
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ItemDisplayContext.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, multiBufferSource, world, 0);
        matrices.pop();
    }

    @Override
    public <S extends LivingEntityRenderState> void renderOnFirstPerson(Arm arm, ItemStack stack, SlotReference reference, MatrixStack matrices, EntityModel<S> entityModel, S renderState, VertexConsumerProvider multiBufferSource, int light, float partialTicks) {
        if (!(entityModel instanceof PlayerEntityModel model)) return;
        boolean left = arm == Arm.LEFT;
        matrices.push();
        AccessoryRenderer.transformToFace(matrices, left ? model.leftArm : model.rightArm, Side.BOTTOM);
        matrices.multiply(new Quaternionf().rotateXYZ((float) Math.PI, 0, 0));
        matrices.translate(left ? 0 : 0.015625, -0.1875, left ? -0.135 : -0.14);
        matrices.scale(model.thinArms ? 0.59375f : 0.75f, 0.75f, 1.03125f);
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ItemDisplayContext.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, multiBufferSource, reference.entity().getEntityWorld(), 0);
        matrices.pop();
    }

    @Override
    public <S extends LivingEntityRenderState> void render(ItemStack stack, SlotReference reference, MatrixStack matrices, EntityModel<S> entityModel, S renderState, VertexConsumerProvider multiBufferSource, int light, float partialTicks) {
        if (!(entityModel instanceof PlayerEntityModel model)) return;

        ItemStack is = stack.copy();
        is.remove(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        is.remove(DataComponentTypes.ENCHANTMENTS);
        renderForArm(is, matrices, model, reference.entity().getEntityWorld(), multiBufferSource, light, false);
        renderForArm(is, matrices, model, reference.entity().getEntityWorld(), multiBufferSource, light, true);
    }
}
