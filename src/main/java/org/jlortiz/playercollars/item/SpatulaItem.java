package org.jlortiz.playercollars.item;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class SpatulaItem extends Item {
    public SpatulaItem() {
        super(new Item.Settings().maxCount(1).maxDamage(8));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
            ActionResult res = useOnEntity(user.getStackInHand(hand), user, user, hand);
            if (res.isAccepted()) return TypedActionResult.success(user.getStackInHand(hand));
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        int count = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmorSlot()) continue;
            ItemStack is = entity.getEquippedStack(slot);
            if (EnchantmentHelper.hasAnyEnchantmentsWith(is, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) {
                count++;
                entity.dropStack(is);
                entity.equipStack(slot, ItemStack.EMPTY);
            }
        }

        count += TrinketsApi.getTrinketComponent(entity).map(TrinketComponent::getAllEquipped).map((x) -> {
            int count2 = 0;
            for (Pair<SlotReference, ItemStack> p : x) {
                if (EnchantmentHelper.hasAnyEnchantmentsWith(p.getRight(), EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) {
                    count2++;
                    entity.dropStack(p.getRight());
                    p.getLeft().inventory().removeStack(p.getLeft().index());
                }
            }
            return count2;
        }).orElse(0);

        if (count == 0) return ActionResult.PASS;
        stack.damage(count, user, LivingEntity.getSlotForHand(hand));
        entity.playSound(SoundEvents.ITEM_WOLF_ARMOR_BREAK);
        return ActionResult.SUCCESS;
    }
}
