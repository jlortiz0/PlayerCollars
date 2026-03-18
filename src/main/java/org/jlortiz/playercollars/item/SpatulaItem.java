package org.jlortiz.playercollars.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class SpatulaItem extends Item {
    public SpatulaItem() {
        super(new Item.Properties().stacksTo(1).durability(8));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (user.isCrouching()) {
            if (interactLivingEntity(user.getItemInHand(hand), user, user, hand).consumesAction())
                return InteractionResultHolder.success(user.getItemInHand(hand));
        }
        return super.use(world, user, hand);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        int count = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) continue;
            ItemStack is = entity.getItemBySlot(slot);
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, is) > 0) {
                count++;
                entity.spawnAtLocation(is);
                entity.setItemSlot(slot, ItemStack.EMPTY);
            }
        }

        final int[] extraCount = {0};
        CuriosApi.getCuriosInventory(entity).ifPresent(h -> {
            for (SlotResult sr : h.findCurios(s -> EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, s) > 0)) {
                extraCount[0]++;
                entity.spawnAtLocation(sr.stack());
                // Clear the slot via the slot context
                h.setEquippedCurio(sr.slotContext().identifier(), sr.slotContext().index(), ItemStack.EMPTY);
            }
        });
        count += extraCount[0];

        if (count == 0) return InteractionResult.PASS;
        stack.hurtAndBreak(count, user, p -> p.broadcastBreakEvent(hand));
        return InteractionResult.SUCCESS;
    }
}
