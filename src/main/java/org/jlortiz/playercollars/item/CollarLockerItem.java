package org.jlortiz.playercollars.item;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.List;
import java.util.Optional;

public class CollarLockerItem extends Item {
    public CollarLockerItem() {
        super(new Settings().maxCount(1));
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof PlayerEntity player) || user.getWorld().isClient) return ActionResult.PASS;
        Optional<TrinketComponent> optComponent = TrinketsApi.getTrinketComponent(player);
        if (optComponent.isEmpty()) return ActionResult.PASS;
        TrinketComponent component = optComponent.get();

        ItemStack collarStack = PlayerCollarsMod.filterStacksByOwner(component.getEquipped((y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG)), user.getUuid(), player.getUuid());
        if (collarStack == null) {
            user.sendMessage(Text.translatable("item.playercollars.collar_locker.no_set_non_owner").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }
        if (collarStack.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE).owned().isEmpty()) {
            user.sendMessage(Text.translatable("item.playercollars.collar_locker.no_set_non_deed").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }

        RegistryEntry<Enchantment> binding = ((ServerPlayerEntity) user).getServerWorld().getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT).entryOf(Enchantments.BINDING_CURSE);
        boolean shouldLock = !EnchantmentHelper.hasAnyEnchantmentsWith(collarStack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE);
        List<Pair<SlotReference, ItemStack>> ls = component.getEquipped(
                (y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG) ||
                        y.isIn(PlayerCollarsMod.PAWS_TAG) ||
                        y.isIn(PlayerCollarsMod.FOOT_PAWS_TAG)
        );

        for (Pair<SlotReference, ItemStack> p : ls) {
            ItemStack is = p.getRight();
            if (!is.hasEnchantments()) {
                if (shouldLock) {
                    ItemEnchantmentsComponent.Builder ench = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
                    ench.add(binding, 1);
                    EnchantmentHelper.set(is, ench.build());
                }
                continue;
            }
            EnchantmentHelper.apply(is, (ench) -> {
                if (shouldLock) ench.add(binding, 1);
                else ench.remove((e) -> e.value().effects().contains(EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE));
            });
        }
        player.sendMessage(Text.translatable(shouldLock ? "item.playercollars.collar_locker.locked" : "item.playercollars.collar_locker.unlocked"), true);
        user.sendMessage(Text.translatable(shouldLock ? "item.playercollars.collar_locker.locked" : "item.playercollars.collar_locker.unlocked"), true);
        player.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), shouldLock ? SoundEvents.ITEM_ARMOR_EQUIP_WOLF.value() : SoundEvents.ITEM_ARMOR_UNEQUIP_WOLF, SoundCategory.PLAYERS);

        return ActionResult.SUCCESS;
    }
}
