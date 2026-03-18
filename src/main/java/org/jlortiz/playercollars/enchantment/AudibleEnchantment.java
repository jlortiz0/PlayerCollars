package org.jlortiz.playercollars.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.ClickerItem;

public class AudibleEnchantment extends Enchantment {
    public AudibleEnchantment() {
        super(Rarity.UNCOMMON, PlayerCollarsMod.CLICKER_ENCHANTABLE, new EquipmentSlot[]{});
    }

    @Override public int getMinCost(int level) { return level == 1 ? 2 : level == 2 ? 26 : 48; }
    @Override public int getMaxCost(int level) { return level == 1 ? 24 : level == 2 ? 47 : 72; }
    @Override public int getMaxLevel() { return 3; }


    @Override
    public boolean canApplyAtEnchantingTable(net.minecraft.world.item.ItemStack stack) {
        return stack.getItem() instanceof org.jlortiz.playercollars.item.ClickerItem;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }
}
