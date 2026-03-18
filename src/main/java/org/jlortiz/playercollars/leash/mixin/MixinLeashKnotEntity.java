package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeashFenceKnotEntity.class)
public abstract class MixinLeashKnotEntity extends HangingEntity {
    protected MixinLeashKnotEntity(EntityType<? extends HangingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(
        method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/LeashFenceKnotEntity;discard()V"),
        cancellable = true
    )
    private void preventBreakKnot(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Level world = level();
        // noinspection ConstantValue
        if (!world.isClientSide() && PlayerCollarsMod.blockLeashKnotBreak(world, player,
                (LeashFenceKnotEntity) (Object) this)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
