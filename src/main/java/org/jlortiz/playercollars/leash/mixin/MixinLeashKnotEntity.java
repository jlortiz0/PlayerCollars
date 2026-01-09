package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.BlockAttachedEntity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeashKnotEntity.class)
public abstract class MixinLeashKnotEntity extends BlockAttachedEntity {
    private MixinLeashKnotEntity(EntityType<? extends BlockAttachedEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/LeashKnotEntity;discard()V"), cancellable = true)
    private void preventBreakKnot(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        World world = getWorld();
        if (!world.isClient() && PlayerCollarsMod.blockLeashKnotBreak((ServerWorld) world, player, (LeashKnotEntity) (Object) this)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
