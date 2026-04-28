package org.jlortiz.playercollars.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class GroomingBrushItem extends Item {
    public GroomingBrushItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!user.getWorld().isClient && entity instanceof PlayerEntity pet) {
            ((ServerWorld) user.getWorld()).spawnParticles(
                    ParticleTypes.HEART,
                    pet.getX(), pet.getY() + 1.0, pet.getZ(),
                    5, 0.3, 0.3, 0.3, 0.0
            );


            user.getWorld().playSound(null, pet.getBlockPos(), SoundEvents.ENTITY_WOLF_PANT, SoundCategory.PLAYERS, 1.0f, 1.2f);

            return ActionResult.SUCCESS;
        }
        return ActionResult.CONSUME;
    }
}