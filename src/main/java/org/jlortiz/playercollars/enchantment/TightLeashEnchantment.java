package org.jlortiz.playercollars.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.CollarItem;

public class TightLeashEnchantment extends Enchantment {
    public TightLeashEnchantment() {
        super(Rarity.COMMON, PlayerCollarsMod.COLLAR_ENCHANTABLE, new EquipmentSlot[]{});
    }

    @Override public int getMinCost(int level) { return level == 1 ? 2 : 30; }
    @Override public int getMaxCost(int level) { return level == 1 ? 28 : 55; }
    @Override public int getMaxLevel() { return 2; }


    @Override
    public boolean canApplyAtEnchantingTable(net.minecraft.world.item.ItemStack stack) {
        return stack.getItem() instanceof org.jlortiz.playercollars.item.CollarItem;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }
}
