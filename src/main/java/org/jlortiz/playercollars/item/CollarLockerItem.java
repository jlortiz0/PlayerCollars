package org.jlortiz.playercollars.item;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.List;
import java.util.Optional;

public class CollarLockerItem extends Item {
    public CollarLockerItem() {
        super(new Settings().maxCount(1));
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity targetEntity, Hand hand) {
        if (!(targetEntity instanceof PlayerEntity targetPlayer) || user.getWorld().isClient) return ActionResult.PASS;
        Optional<TrinketComponent> optComponent = TrinketsApi.getTrinketComponent(targetPlayer);
        if (optComponent.isEmpty()) return ActionResult.PASS;
        TrinketComponent component = optComponent.get();

        ItemStack collarStack = PlayerCollarsMod.filterStacksByOwner(component.getEquipped((y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG)), user.getUuid(), targetPlayer.getUuid());
        if (collarStack == null) {
            user.sendMessage(Text.translatable("item.playercollars.collar_locker.no_set_non_owner").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }
        var deedOwner = NbtUtil.getDeedOwner(collarStack);
        if (deedOwner == null) {
            user.sendMessage(Text.translatable("item.playercollars.collar_locker.no_set_non_deed").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }

        boolean shouldLock = !EnchantmentHelper.hasBindingCurse(collarStack);
        List<Pair<SlotReference, ItemStack>> ls = component.getEquipped(
                (y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG) || y.isIn(PlayerCollarsMod.PAWS_TAG) || y.isIn(PlayerCollarsMod.FOOT_PAWS_TAG)
        );

        for (Pair<SlotReference, ItemStack> p : ls) {
            ItemStack is = p.getRight();
            var enchantments = EnchantmentHelper.get(is);
            if (shouldLock)
                enchantments.put(Enchantments.BINDING_CURSE, 1);
            else
                enchantments.remove(Enchantments.BINDING_CURSE);
            EnchantmentHelper.set(enchantments, is);
        }
        targetPlayer.sendMessage(Text.translatable(shouldLock ? "item.playercollars.collar_locker.locked" : "item.playercollars.collar_locker.unlocked"), true);
        user.sendMessage(Text.translatable(shouldLock ? "item.playercollars.collar_locker.locked" : "item.playercollars.collar_locker.unlocked"), true);

        targetPlayer.getWorld()
                .playSound(null, targetEntity.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS);

        return ActionResult.SUCCESS;
    }
}
