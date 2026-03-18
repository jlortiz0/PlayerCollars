package org.jlortiz.playercollars.enchantment;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.CollarItem;

public class SpikedEnchantment extends Enchantment {
    public SpikedEnchantment() {
        super(Rarity.VERY_RARE, PlayerCollarsMod.COLLAR_ENCHANTABLE, new EquipmentSlot[]{});
    }

    @Override public int getMinCost(int level) { return level == 1 ? 2 : level == 2 ? 24 : 44; }
    @Override public int getMaxCost(int level) { return level == 1 ? 22 : level == 2 ? 43 : 68; }
    @Override public int getMaxLevel() { return 3; }


    // Called from MixinEnchantmentHelper when collar wearer is hurt
    @Override
    public void doPostHurt(LivingEntity user, Entity attacker, int level) {
        // 15% chance per level, same as vanilla Thorns, but NO durability damage
        if (user.level().isClientSide) return;
        if (user.getRandom().nextFloat() < 0.15f * level) {
            if (attacker instanceof LivingEntity living) {
                DamageSource src = user.damageSources().thorns(user);
                living.hurt(src, 1.0f + user.getRandom().nextInt(4));
            }
        }
    }

    @Override
    public boolean canApplyAtEnchantingTable(net.minecraft.world.item.ItemStack stack) {
        return stack.getItem() instanceof org.jlortiz.playercollars.item.CollarItem;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }
}
