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
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.world.World;
import org.joml.Quaternionf;

public class FootPawRenderer implements AccessoryRenderer {
    private static void renderForLeg(ItemStack stack, MatrixStack matrices, PlayerEntityModel model, World world, VertexConsumerProvider multiBufferSource, int light, boolean left) {
        matrices.push();
        AccessoryRenderer.transformToFace(matrices, left ? model.leftLeg : model.rightLeg, Side.BOTTOM);
        matrices.multiply(new Quaternionf().rotateXYZ((float) -Math.PI / 2, 0, 0), 0, 0, 0);
        matrices.translate(0, 0, 0.125);
        matrices.scale(0.75f, 0.75f, 0.75f);
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, multiBufferSource, world, 0);
        matrices.pop();
    }

    @Override
    public <S extends LivingEntityRenderState> void render(ItemStack itemStack, SlotReference slotReference, MatrixStack matrixStack, EntityModel<S> entityModel, S s, VertexConsumerProvider vertexConsumerProvider, int i, float v) {
        if (!(entityModel instanceof PlayerEntityModel model)) return;

        ItemStack is = itemStack.copy();
        is.remove(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        is.remove(DataComponentTypes.ENCHANTMENTS);
        renderForLeg(is, matrixStack, model, slotReference.entity().getWorld(), vertexConsumerProvider, i, false);
        renderForLeg(is, matrixStack, model, slotReference.entity().getWorld(), vertexConsumerProvider, i, true);
    }
}
