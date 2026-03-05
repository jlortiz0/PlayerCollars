package org.jlortiz.playercollars.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import org.jlortiz.playercollars.item.CollarItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public class MixinEnchantment {
    @ModifyReturnValue(
            method = "isAcceptableItem",
            at = @At("RETURN")
    )
    boolean isAcceptableItem(boolean original, ItemStack stack) {
        // noinspection ConstantValue
        return original || (stack.getItem() instanceof CollarItem && CollarItem.isAcceptableEnchantment((Enchantment) (Object) this));
    }
}
