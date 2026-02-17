package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
    // Why is this in Entity and not LeashKnotEntity???
    @Inject(method = "snipAllHeldLeashes", at = @At(value = "HEAD"), cancellable = true)
    private void preventBreakKnot(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (PlayerCollarsMod.blockLeashKnotBreak(player, (Entity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}