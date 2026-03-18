package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jlortiz.playercollars.leash.LeashProxyEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class MixinServerWorld {
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true, require = 0)
    private void leashplayers$onAddEntity(Entity entity, CallbackInfoReturnable<Boolean> info) {
        // ServerLevel.addEntity returns boolean; just ensure proxy always gets added
        if (entity instanceof LeashProxyEntity) {
            // Don't cancel — we *want* the proxy to spawn
        }
    }
}
