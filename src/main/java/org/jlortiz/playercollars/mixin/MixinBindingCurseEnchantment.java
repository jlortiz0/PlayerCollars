package org.jlortiz.playercollars.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.enchantment.BindingCurseEnchantment;
import net.minecraft.item.ItemStack;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BindingCurseEnchantment.class)
public class MixinBindingCurseEnchantment {
    @WrapOperation(
            method = "isAcceptableItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z")
    )
    boolean isAcceptableItem(BindingCurseEnchantment instance, ItemStack stack, Operation<Boolean> original) {
        if (stack.isOf(PlayerCollarsMod.COLLAR_ITEM))
            return true;
        else
            return original.call(instance, stack);
    }
}
