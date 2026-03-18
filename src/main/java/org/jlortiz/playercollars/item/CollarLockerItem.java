package org.jlortiz.playercollars.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.util.NbtUtil;
import top.theillusivec4.curios.api.CuriosApi;

public class CollarLockerItem extends Item {
    public CollarLockerItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity targetEntity, InteractionHand hand) {
        if (!(targetEntity instanceof Player targetPlayer) || user.level().isClientSide)
            return InteractionResult.PASS;

        var curiosOpt = CuriosApi.getCuriosInventory(targetPlayer);
        if (!curiosOpt.isPresent()) return InteractionResult.PASS;

        var collarResults = curiosOpt.resolve().orElseThrow().findCurios(s -> s.is(PlayerCollarsMod.COLLAR_TAG));
        ItemStack collarStack = PlayerCollarsMod.filterStacksByOwner(collarResults, user.getUUID(), targetPlayer.getUUID());
        if (collarStack == null) {
            user.displayClientMessage(Component.translatable("item.playercollars.collar_locker.no_set_non_owner")
                    .withStyle(net.minecraft.ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        var deedOwner = NbtUtil.getDeedOwner(collarStack);
        if (deedOwner == null || deedOwner.owned().isEmpty()) {
            user.displayClientMessage(Component.translatable("item.playercollars.collar_locker.no_set_non_deed")
                    .withStyle(net.minecraft.ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        boolean shouldLock = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, collarStack) == 0;
        var allEquipped = curiosOpt.resolve().orElseThrow().findCurios(s ->
                s.is(PlayerCollarsMod.COLLAR_TAG) || s.is(PlayerCollarsMod.PAWS_TAG) || s.is(PlayerCollarsMod.FOOT_PAWS_TAG));

        for (var sr : allEquipped) {
            ItemStack is = sr.stack();
            var enchants = EnchantmentHelper.getEnchantments(is);
            if (shouldLock) enchants.put(Enchantments.BINDING_CURSE, 1);
            else enchants.remove(Enchantments.BINDING_CURSE);
            EnchantmentHelper.setEnchantments(enchants, is);
        }
        var text = Component.translatable(shouldLock ? "item.playercollars.collar_locker.locked" : "item.playercollars.collar_locker.unlocked");
        targetPlayer.displayClientMessage(text, true);
        user.displayClientMessage(text, true);
        targetPlayer.level().playSound(null, targetEntity.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS);
        return InteractionResult.SUCCESS;
    }
}
