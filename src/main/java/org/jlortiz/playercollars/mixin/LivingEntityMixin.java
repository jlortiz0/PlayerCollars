package org.jlortiz.playercollars.mixin;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jlortiz.playercollars.block.DogBedBlock;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method="setPositionInBed", at = @At("HEAD"), cancellable = true, require=0)
    private void correctDogBedHeight(BlockPos pos, CallbackInfo ci) {
        BlockState state = getWorld().getBlockState(pos);
        if (state.getBlock() instanceof DogBedBlock) {
            Vec3d vec = pos.toBottomCenterPos();
            Vector3f off = state.get(BedBlock.FACING).getUnitVector().div(10);
            setPosition(vec.add(off.x, 0.35, off.z));
            ci.cancel();
        }
    }
}
