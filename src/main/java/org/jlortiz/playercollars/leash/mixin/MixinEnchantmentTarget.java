package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(EnchantmentHelper.class)
public abstract class MixinEnchantmentTarget {
    @Redirect(method="generateEnchantments", at= @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getPossibleEntries(ILnet/minecraft/item/ItemStack;Z)Ljava/util/List;"))
    private static List<EnchantmentLevelEntry> allowEnchantingOurItems(int power, ItemStack stack, boolean treasureAllowed) {
        if (stack.getItem() == PlayerCollarsMod.COLLAR_ITEM) {
            List<EnchantmentLevelEntry> ls = new ArrayList<>();
            ls.add(new EnchantmentLevelEntry(Enchantments.MENDING, 1));
            ls.add(new EnchantmentLevelEntry(Enchantments.BINDING_CURSE, 1));
            Enchantment enchantment = Enchantments.THORNS;
            for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
                if (power >= enchantment.getMinPower(i) && power <= enchantment.getMaxPower(i)) {
                    ls.add(new EnchantmentLevelEntry(enchantment, i));
                    break;
                }
            }
            enchantment = Enchantments.LOYALTY;
            for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
                if (power >= enchantment.getMinPower(i) && power <= enchantment.getMaxPower(i)) {
                    ls.add(new EnchantmentLevelEntry(enchantment, i));
                    break;
                }
            }
            return ls;
        } else if (stack.getItem() == PlayerCollarsMod.CLICKER_ITEM) {
            Enchantment enchantment = Enchantments.LURE;
            List<EnchantmentLevelEntry> output = new ArrayList<>();
            for (int i = enchantment.getMaxLevel(); i > 0; --i) {
                if (i == 1 || (power >= enchantment.getMinPower(i) / 2 && power <= enchantment.getMaxPower(i) / 2)) {
                    output.add(new EnchantmentLevelEntry(enchantment, i));
                    return output;
                }
            }
            return output;
        }
        return EnchantmentHelper.getPossibleEntries(power, stack, treasureAllowed);
    }
}
