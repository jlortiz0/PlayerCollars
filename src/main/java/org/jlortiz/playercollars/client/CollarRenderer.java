package org.jlortiz.playercollars.client;

import io.wispforest.accessories.api.client.AccessoriesRenderStateKeys;
import io.wispforest.accessories.api.client.AccessoryRenderState;
import io.wispforest.accessories.api.client.renderers.AccessoryRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemDisplayContext;
import org.joml.Quaternionf;

public class CollarRenderer implements AccessoryRenderer {

    @Override
    public <S extends LivingEntityRenderState> void render(AccessoryRenderState accessoryState, S entityState, EntityModel<S> model, MatrixStack matrices, OrderedRenderCommandQueue collector) {
        var light = entityState.getStateData(AccessoriesRenderStateKeys.LIGHT);
        var stack = accessoryState.getStateData(AccessoriesRenderStateKeys.ITEM_STACK);
        var entity = MinecraftClient.getInstance().world.getEntityById( // FixMe: This may only be an issue with this old build. Could change with AccessoriesRenderStateKeys.ENTITY_ID
                accessoryState.getStateData(AccessoriesRenderStateKeys.ENTITY_STATE).getEntityIdForState());

        if (!(entity instanceof PlayerEntity player)) return;

        stack.remove(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        stack.remove(DataComponentTypes.ENCHANTMENTS);

        ModelPart body = ((PlayerEntityModel) model).body;
        boolean hasChestplate = !player.getEquippedStack(EquipmentSlot.CHEST).isEmpty();

        matrices.translate(body.originX * 0.0625f, body.originY * 0.0625f, body.originZ * 0.0625f);
        matrices.multiply(new Quaternionf().rotateXYZ(body.pitch, body.yaw, body.roll + (float) Math.PI));
        matrices.scale((hasChestplate ? 0.7f : 0.85f) * body.xScale, 0.85f * body.yScale, (hasChestplate ? 1.1f : 0.85f) * body.zScale);
        matrices.translate(0, hasChestplate ? 0.475 : 0.4125, -0.005);


        MinecraftClient.getInstance().gameRenderer.firstPersonRenderer
                .renderItem(player, stack, ItemDisplayContext.HEAD, matrices, collector, light);
    }
}
