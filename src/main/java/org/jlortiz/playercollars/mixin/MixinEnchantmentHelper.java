package org.jlortiz.playercollars.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import org.jlortiz.playercollars.item.CollarItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {
    @ModifyExpressionValue(
            method = "getPossibleEntries",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentTarget;isAcceptableItem(Lnet/minecraft/item/Item;)Z")
    )
    private static boolean isAcceptableItem(boolean original, int power, ItemStack stack, boolean treasureAllowed, @Local Enchantment enchantment) {
        return original || ((stack.getItem() instanceof CollarItem) && CollarItem.isAcceptableEnchantment(enchantment));
    }
}
