package org.jlortiz.playercollars.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.enchantment.BindingCurseEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.LoyaltyEnchantment;
import net.minecraft.enchantment.ThornsEnchantment;
import net.minecraft.item.Item;
import org.jlortiz.playercollars.item.CollarItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public class MixinEnchantment {
    @WrapOperation(
            method = "isAcceptableItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentTarget;isAcceptableItem(Lnet/minecraft/item/Item;)Z")
    )
    boolean isAcceptableItem(EnchantmentTarget instance, Item item, Operation<Boolean> original) {
        var enchantment = (Enchantment) (Object) this;
        if (item instanceof CollarItem) {
            if (enchantment instanceof BindingCurseEnchantment || enchantment instanceof LoyaltyEnchantment || enchantment instanceof ThornsEnchantment)
                return true;
        }

        return original.call(instance, item);
    }
}
