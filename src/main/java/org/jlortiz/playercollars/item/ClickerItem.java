package org.jlortiz.playercollars.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jlortiz.playercollars.enchantment.AudibleEnchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.FishingSpeedEnchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.network.ModPackets;
import org.jlortiz.playercollars.network.PacketLookAtLerped;
import org.jlortiz.playercollars.util.NbtUtil;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.UUID;

public class ClickerItem extends Item implements DyeableLeatherItem {
    private static final UUID LURE_CLICKER_DISTANCE_ATTRIBUTE = UUID.fromString("c60e705d-bd9b-40bd-af81-28c7ad15d8d3");

    public ClickerItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static boolean isAcceptableEnchantment(Enchantment enchantment) {
        return enchantment instanceof AudibleEnchantment;
    }

    public boolean getForceTurning(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("force_turning");
    }

    public void setForceTurning(ItemStack stack, boolean val) {
        stack.getOrCreateTag().putBoolean("force_turning", val);
    }

    @Override
    public int getColor(ItemStack stack) { return NbtUtil.getColor(stack, 0xFFFFFF); }

    @Override
    public void setColor(ItemStack stack, int color) { NbtUtil.setColor(stack, color); }

    @Override
    public boolean isEnchantable(ItemStack stack) { return true; }

    @Override
    public int getEnchantmentValue() { return 40; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        if (!world.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            boolean forceTurning = getForceTurning(stack);
            if (player.isCrouching()) {
                setForceTurning(stack, !forceTurning);
                player.displayClientMessage(Component.translatable(
                        forceTurning ? "item.playercollars.clicker.turn_disable" : "item.playercollars.clicker.turn_enable"), true);
                return InteractionResultHolder.consume(stack);
            }
            world.playSound(null, player, PlayerCollarsMod.CLICKER_ON.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1, 1);
            if (forceTurning) {
                double clickerDistance = player.getAttributeValue(PlayerCollarsMod.ATTR_CLICKER_DISTANCE.get());
                var targets = ((ServerLevel) world).getPlayers(p -> !p.is(player) && p.distanceTo(player) <= clickerDistance);
                PacketLookAtLerped packet = new PacketLookAtLerped(player);
                for (ServerPlayer target : targets) {
                    CuriosApi.getCuriosInventory(target).ifPresent(h -> {
                        var collars = h.findCurios(s -> s.is(PlayerCollarsMod.COLLAR_TAG));
                        if (PlayerCollarsMod.filterStacksByOwner(collars, player.getUUID(), target.getUUID()) != null) {
                            ModPackets.CHANNEL.sendTo(packet, target.connection.connection,
                                    net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
                        }
                    });
                }
            }
        }
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack) { return Integer.MAX_VALUE; }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingTicks) {
        if (!world.isClientSide)
            world.playSound(null, user, PlayerCollarsMod.CLICKER_OFF.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1, 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        if (getForceTurning(stack))
            tooltip.add(Component.translatable("item.playercollars.clicker.turn"));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) return ImmutableMultimap.of();
        int lure = EnchantmentHelper.getItemEnchantmentLevel(org.jlortiz.playercollars.PlayerCollarsMod.AUDIBLE_ENCHANTMENT.get(), stack);
        if (lure == 0) return ImmutableMultimap.of();
        return ImmutableMultimap.of(PlayerCollarsMod.ATTR_CLICKER_DISTANCE.get(),
                new AttributeModifier(LURE_CLICKER_DISTANCE_ATTRIBUTE, getDescriptionId(), lure * 4, AttributeModifier.Operation.ADDITION));
    }
}
