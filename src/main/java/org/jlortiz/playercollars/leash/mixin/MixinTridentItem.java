package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.world.World;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TridentItem.class)
public class MixinTridentItem {
    /**
     * Prevent Riptide boosting when the player is leashed.
     * This lets the normal "use" (throwing) still proceed, but cancels the riptide branch
     * in onStoppedUsing by returning false when the trident has Riptide (f > 0.0F).
     */
    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfoReturnable<Boolean> cir) {
        if (!(user instanceof PlayerEntity player)) return;

        if (player instanceof LeashImpl leash && leash.leashplayers$getProxyLeashHolder() != null) {
            float f = EnchantmentHelper.getTridentSpinAttackStrength(stack, player);
            if (f > 0.0F) {
                cir.setReturnValue(false);
            }
        }
    }
}
