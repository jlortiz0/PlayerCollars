package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerEntity.class, priority = 500)
public abstract class MixinPlayerEntity {
    @Inject(method = "createPlayerAttributes", at = @At("RETURN"))
    private static void playercollars$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue().add(PlayerCollarsMod.ATTR_LEASH_DISTANCE);
    }

    // Ideally this should be in MixinServerPlayerEntity, but I'm *very* wary about overriding methods in the player
    @Inject(method = "interact", at = @At("RETURN"), cancellable = true)
    private void leashplayers$onInteract(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        if (info.getReturnValue() != ActionResult.PASS) return;
        // noinspection ConstantValue
        if (((Object) this) instanceof ServerPlayerEntity player && entity instanceof LeashImpl impl) {
            info.setReturnValue(impl.leashplayers$interact(player, hand));
            info.cancel();
        }
    }
}
