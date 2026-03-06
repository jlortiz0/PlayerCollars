package org.jlortiz.playercollars.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {
    @ModifyExpressionValue(
            method = "getPossibleEntries",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentTarget;isAcceptableItem(Lnet/minecraft/item/Item;)Z")
    )
    private static boolean isAcceptableItem(boolean original, int power, ItemStack stack, boolean treasureAllowed, @Local Enchantment enchantment) {
        return original || PlayerCollarsMod.isAcceptableEnchant(stack.getItem(), enchantment);
    }

    @Inject(
            method = "onUserDamaged",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/EnchantmentHelper;forEachEnchantment(Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;Lnet/minecraft/item/ItemStack;)V"
            )
    )
    private static void applyCollarThorns(LivingEntity user, Entity attacker, CallbackInfo ci, @Local EnchantmentHelper.Consumer consumer) {
        var stacks = TrinketsApi.getTrinketComponent(user)
                .map((x) -> x.getEquipped((y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG)))
                .stream()
                .flatMap(Collection::stream)
                .map(Pair::getRight)
                .toList();

        EnchantmentHelper.forEachEnchantment(consumer, stacks);
    }
}
