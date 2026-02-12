package org.jlortiz.playercollars.mixin;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.PawRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerEntityRenderer.class)
public class PlayerRendererMixin {
    @Inject(method = "renderArm", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void playercollars$renderGloveTrinket(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo ci, PlayerEntityModel<AbstractClientPlayerEntity> model) {
        TrinketsApi.getTrinketComponent(player).map((x) -> x.getEquipped((y) -> y.isIn(PlayerCollarsMod.PAWS_TAG)))
                .ifPresent((ls) -> {
                    for (Pair<SlotReference, ItemStack> p : ls) {
                        PawRenderer.renderOnFirstPerson(p.getRight(), matrices, model, player.getWorld(), vertexConsumers, light, arm == model.leftArm);
                    }
                });
    }
}
