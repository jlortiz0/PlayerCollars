package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeashKnotEntity.class)
public abstract class MixinLeashKnotEntity {
    @Inject(method = "interact", at = @At(value = "HEAD"), cancellable = true)
    private void preventBreakKnot(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        Entity holder = ((LeashImpl) player).leashplayers$getProxyLeashHolder();
        if (holder.equals(this)) {
            player.sendMessage(Text.translatable("message.playercollars.no_break_fence").formatted(Formatting.RED), true);
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
