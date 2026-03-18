package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Player.class, priority = 500)
public abstract class MixinPlayerEntity {
    @Inject(method = "createAttributes", at = @At("RETURN"))
    private static void playercollars$addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue().add(PlayerCollarsMod.ATTR_LEASH_DISTANCE.get());
    }

    // In Mojmap 1.20.1, Yarn's Player.interact() = Player.interactOn()
    @Inject(
        method = "interactOn(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
        at = @At("RETURN"),
        cancellable = true,
        require = 0
    )
    private void leashplayers$onInteract(Entity entity, InteractionHand hand,
                                          CallbackInfoReturnable<InteractionResult> info) {
        if (info.getReturnValue() != InteractionResult.PASS) return;
        // noinspection ConstantValue
        if (((Object) this) instanceof ServerPlayer player && entity instanceof LeashImpl impl) {
            info.setReturnValue(impl.leashplayers$interact(player, hand));
            info.cancel();
        }
    }
}
