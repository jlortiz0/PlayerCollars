package org.jlortiz.playercollars.item;

import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class SpatulaItem extends Item {
    public static final RegistryKey<Item> REGISTRY_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "golden_spatula"));
    public SpatulaItem() {
        super(new Item.Settings().maxCount(1).maxDamage(8).registryKey(REGISTRY_KEY));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
            ActionResult res = useOnEntity(user.getStackInHand(hand), user, user, hand);
            if (res.isAccepted()) return ActionResult.SUCCESS;
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        ServerWorld world = null;
        if (!entity.getWorld().isClient)
            world = (ServerWorld) entity.getWorld();

        int count = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmorSlot()) continue;
            ItemStack is = entity.getEquippedStack(slot);
            if (EnchantmentHelper.hasAnyEnchantmentsWith(is, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) {
                count++;
                if (world != null)
                    entity.dropStack(world, is);
                entity.equipStack(slot, ItemStack.EMPTY);
            }
        }

        AccessoriesCapability cap = AccessoriesCapability.get(entity);
        if (cap != null) {
            for (SlotEntryReference p : cap.getAllEquipped()) {
                if (EnchantmentHelper.hasAnyEnchantmentsWith(p.stack(), EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) {
                    count++;
                    if (world != null)
                        entity.dropStack(world, p.stack());
                    p.reference().setStack(ItemStack.EMPTY);
                }
            }
        }

        if (count == 0) return ActionResult.PASS;
        stack.damage(count, user, LivingEntity.getSlotForHand(hand));
        entity.playSound(SoundEvents.ITEM_WOLF_ARMOR_BREAK);
        return ActionResult.SUCCESS;
    }
}
