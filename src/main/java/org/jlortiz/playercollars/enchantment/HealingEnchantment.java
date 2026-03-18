package org.jlortiz.playercollars.enchantment;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.util.NbtUtil;

public class HealingEnchantment extends Enchantment {
    public HealingEnchantment() {
        super(Rarity.RARE, PlayerCollarsMod.COLLAR_ENCHANTABLE, new EquipmentSlot[]{});
    }

    @Override public int getMinCost(int level) { return 46; }
    @Override public int getMaxCost(int level) { return 70; }
    @Override public int getMaxLevel() { return 1; }


    // Called every tick from CollarItem.buildCurio's curioTick
    public static void tick(LivingEntity entity, net.minecraft.world.item.ItemStack collarStack, int level) {
        if (entity.level().isClientSide) return;
        var owner = NbtUtil.getDeedOwner(collarStack);
        if (owner == null) return;
        Player own = entity.level().getPlayerByUUID(owner.uuid());
        if (own != null && own.distanceToSqr(entity) < 256) { // 16 blocks
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, level - 1, false, false, false));
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
