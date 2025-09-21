package org.jlortiz.playercollars.item;

import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.List;

public class CollarLockerItem extends Item {
    public static final RegistryKey<Item> REGISTRY_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "collar_locker"));
    public CollarLockerItem() {
        super(new Settings().maxCount(1).registryKey(REGISTRY_KEY));
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof PlayerEntity player) || user.getWorld().isClient) return ActionResult.PASS;
        AccessoriesCapability cap = AccessoriesCapability.get(player);
        if (cap == null) return ActionResult.PASS;

        ItemStack collarStack = PlayerCollarsMod.filterStacksByOwner(cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.COLLAR_TAG)), user.getUuid(), player.getUuid());
        if (collarStack == null) {
            user.sendMessage(Text.translatable("item.playercollars.collar_locker.no_set_non_owner").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }

        RegistryEntry<Enchantment> binding = ((ServerPlayerEntity) user).getServerWorld().getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.BINDING_CURSE);
        boolean shouldLock = !EnchantmentHelper.hasAnyEnchantmentsWith(collarStack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE);
        List<SlotEntryReference> ls = cap.getEquipped(
                (y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG) ||
                        y.isIn(PlayerCollarsMod.PAWS_TAG) ||
                        y.isIn(PlayerCollarsMod.FOOT_PAWS_TAG)
        );

        for (SlotEntryReference p : ls) {
            ItemStack is = p.stack();
            if (!is.hasEnchantments()) {
                if (shouldLock) {
                    ItemEnchantmentsComponent.Builder ench = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
                    ench.add(binding, 1);
                    EnchantmentHelper.set(is, ench.build());
                }
                continue;
            }
            EnchantmentHelper.apply(is, (ench) -> {
                if (shouldLock) ench.add(binding, 0);
                else ench.remove((e) -> e.value().effects().contains(EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE));
            });
        }
        player.sendMessage(Text.translatable(shouldLock ? "item.playercollars.collar_locker.locked" : "item.playercollars.collar_locker.unlocked"), true);
        player.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_ARMOR_UNEQUIP_WOLF, SoundCategory.PLAYERS);

        return ActionResult.SUCCESS;
    }
}
