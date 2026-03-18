package org.jlortiz.playercollars.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.PawRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(
        method = "renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V",
        at = @At("TAIL"),
        require = 0
    )
    private void playercollars$renderRightGlove(PoseStack matrices, MultiBufferSource buffers,
                                                  int light, AbstractClientPlayer player,
                                                  CallbackInfo ci) {
        renderPawForArm((PlayerRenderer)(Object)this, matrices, buffers, light, player, false);
    }

    @Inject(
        method = "renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V",
        at = @At("TAIL"),
        require = 0
    )
    private void playercollars$renderLeftGlove(PoseStack matrices, MultiBufferSource buffers,
                                                 int light, AbstractClientPlayer player,
                                                 CallbackInfo ci) {
        renderPawForArm((PlayerRenderer)(Object)this, matrices, buffers, light, player, true);
    }

    @SuppressWarnings("unchecked")
    private static void renderPawForArm(PlayerRenderer renderer, PoseStack matrices,
                                         MultiBufferSource buffers, int light,
                                         AbstractClientPlayer player, boolean left) {
        PlayerModel<AbstractClientPlayer> model = (PlayerModel<AbstractClientPlayer>) renderer.getModel();
        CuriosApi.getCuriosInventory(player).ifPresent(h ->
                h.findCurios(s -> s.is(PlayerCollarsMod.PAWS_TAG)).forEach(sr ->
                        PawRenderer.renderOnFirstPerson(sr.stack(), matrices, model,
                                player.level(), buffers, light, left)));
    }
}
