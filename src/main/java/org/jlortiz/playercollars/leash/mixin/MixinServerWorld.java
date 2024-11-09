package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.jlortiz.playercollars.leash.LeashProxyEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld {
    @Inject(method = "shouldCancelSpawn", at = @At("HEAD"), cancellable = true, require = 0)
    private void leashplayers$onShouldCancelSpawn(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (entity instanceof LeashProxyEntity) {
            info.setReturnValue(false);
            info.cancel();
        }
    }
}