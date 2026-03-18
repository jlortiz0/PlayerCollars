package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentItem.class)
public class MixinTridentItem {
    @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true)
    private void onStoppedUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (!(user instanceof Player player)) return;
        if (player instanceof LeashImpl leash && leash.leashplayers$getProxyLeashHolder() != null) {
            if (EnchantmentHelper.getRiptide(stack) > 0) ci.cancel();
        }
    }
}
