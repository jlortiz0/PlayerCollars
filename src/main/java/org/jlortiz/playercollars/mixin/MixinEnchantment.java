package org.jlortiz.playercollars.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public class MixinEnchantment {
    @ModifyReturnValue(
            method = "isAcceptableItem",
            at = @At("RETURN")
    )
    boolean isAcceptableItem(boolean original, ItemStack stack) {
        return original || PlayerCollarsMod.isAcceptableEnchant(stack.getItem(), (Enchantment) (Object) this);
    }
}
