package org.jlortiz.playercollars.item;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class RewardTreatPouchItem extends Item {
    public RewardTreatPouchItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!user.getWorld().isClient && entity instanceof PlayerEntity pet) {
            AccessoriesCapability cap = AccessoriesCapability.get(pet);
            if (cap == null) return ActionResult.PASS;

            // Make sure the person giving the treat is the actual owner!
            ItemStack collar = PlayerCollarsMod.filterStacksByOwner(cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.COLLAR_TAG)), user.getUuid(), pet.getUuid());

            if (collar != null) {
                // 🐾 Give the pet 3 full bars of hunger (6 points) and a little saturation!
                pet.getHungerManager().add(6, 0.6f);

                // ✨ Give them a little pop of XP!
                pet.addExperience(5);

                // ⚡ Give them the "Zoomies" (Speed and Jump Boost) for 10 seconds (200 ticks)!
                pet.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 1));
                pet.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 200, 1));

                // 💖 Happy sparkle particles and an adorable eating sound!
                ((ServerWorld) user.getWorld()).spawnParticles(ParticleTypes.HAPPY_VILLAGER, pet.getX(), pet.getY() + 1.0, pet.getZ(), 7, 0.3, 0.3, 0.3, 0.0);
                user.getWorld().playSound(null, pet.getBlockPos(), SoundEvents.ENTITY_GENERIC_EAT.value(), SoundCategory.PLAYERS, 1.0f, 1.2f);

                // Consume a treat from the pouch (damages the item by 1)
                stack.damage(1, user, PlayerEntity.getSlotForHand(hand));

                return ActionResult.SUCCESS;
            } else {
                user.sendMessage(Text.literal("You can only give treats to your own pet!").formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }
}