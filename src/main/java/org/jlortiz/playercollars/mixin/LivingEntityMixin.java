package org.jlortiz.playercollars.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jlortiz.playercollars.block.DogBedBlock;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    protected LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "setPosToBed", at = @At("HEAD"), cancellable = true, require = 0)
    private void correctDogBedHeight(BlockPos pos, CallbackInfo ci) {
        BlockState state = level().getBlockState(pos);
        if (state.getBlock() instanceof DogBedBlock) {
            Vec3 vec = Vec3.atCenterOf(pos);
            Vector3f off = state.getValue(net.minecraft.world.level.block.BedBlock.FACING)
                    .step().div(10);
            setPos(vec.x + off.x, vec.y - 0.15, vec.z + off.z);
            ci.cancel();
        }
    }
}
