package org.jlortiz.playercollars.client;


import io.wispforest.accessories.api.client.AccessoriesRenderStateKeys;
import io.wispforest.accessories.api.client.AccessoryRenderState;
import io.wispforest.accessories.api.client.renderers.AccessoryRenderer;
import io.wispforest.accessories.api.client.rendering.Side;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.joml.Quaternionf;

public class FootPawRenderer implements AccessoryRenderer {

    private void renderForLeg(LivingEntity entity, ItemStack stack, PlayerEntityModel model, MatrixStack matrices, OrderedRenderCommandQueue collector, int light, boolean left) {
        matrices.push();
        AccessoryRenderer.transformToFace(matrices, left ? model.leftLeg : model.rightLeg, Side.BOTTOM);
        matrices.multiply(new Quaternionf().rotateXYZ((float) -Math.PI / 2, 0, 0), 0, 0, 0);
        matrices.translate(0, 0, 0.125);
        matrices.scale(0.75f, 0.75f, 0.75f);

        MinecraftClient.getInstance().gameRenderer.firstPersonRenderer
                .renderItem(entity, stack, ItemDisplayContext.FIXED, matrices, collector, light);

        matrices.pop();
    }

    @Override
    public <S extends LivingEntityRenderState> void render(AccessoryRenderState accessoryState, S entityState, EntityModel<S> model, MatrixStack matrices, OrderedRenderCommandQueue collector) {
        if (!(model instanceof PlayerEntityModel playerModel)) return;

        var light = entityState.getStateData(AccessoriesRenderStateKeys.LIGHT);
        var stack = accessoryState.getStateData(AccessoriesRenderStateKeys.ITEM_STACK);
        var entity = MinecraftClient.getInstance().world.getEntityById( // FixMe: This may only be an issue with this old build. Could change with AccessoriesRenderStateKeys.ENTITY_ID
                accessoryState.getStateData(AccessoriesRenderStateKeys.ENTITY_STATE).getEntityIdForState());

        if (!(entity instanceof PlayerEntity player)) return;

        stack.remove(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        stack.remove(DataComponentTypes.ENCHANTMENTS);

        renderForLeg(player, stack, playerModel, matrices, collector, light, true);
        renderForLeg(player, stack, playerModel, matrices, collector, light, false);
    }
}
