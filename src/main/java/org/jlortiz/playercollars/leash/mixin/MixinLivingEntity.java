package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jlortiz.playercollars.item.DogBedBlock;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Inject(method="setPositionInBed", at = @At("HEAD"), cancellable = true)
    private void injected(BlockPos pos, CallbackInfo ci) {
        BlockState state = ((LivingEntity) (Object) this).getWorld().getBlockState(pos);
        if (state.getBlock() instanceof DogBedBlock) {
            Vec3d vec = pos.toBottomCenterPos();
            Vector3f off = state.get(BedBlock.FACING).getUnitVector().div(10);
            ((LivingEntity) (Object) this).setPosition(vec.add(off.x, 0.35, off.z));
            ci.cancel();
        }
    }
}
