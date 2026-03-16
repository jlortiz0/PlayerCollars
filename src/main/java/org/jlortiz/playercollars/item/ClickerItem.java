package org.jlortiz.playercollars.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.LureEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.network.PacketLookAtLerped;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.List;
import java.util.UUID;

public class ClickerItem extends Item implements DyeableItem {
    private static final UUID LURE_CLICKER_DISTANCE_ATTRIBUTE = UUID.fromString("c60e705d-bd9b-40bd-af81-28c7ad15d8d3");
    public ClickerItem() {
        super(new Item.Settings().maxCount(1));
    }

    public static boolean isAcceptableEnchantment(Enchantment enchantment) {
        return enchantment instanceof LureEnchantment;
    }

    public boolean getForceTurning(ItemStack stack) {
        return stack.getNbt() != null && stack.getNbt().getBoolean("force_turning");
    }

    public void setForceTurning(ItemStack stack, boolean forceTurning) {
        stack.getOrCreateNbt().putBoolean("force_turning", forceTurning);
    }

    @Override
    public int getColor(ItemStack stack) {
        return NbtUtil.getColor(stack, 0xFFFFFF);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantability() {
        return 40;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        player.setCurrentHand(hand);
        if (!world.isClient) {
            ItemStack stack = player.getStackInHand(hand);
            var forceTurning = getForceTurning(stack);

            if (player.isSneaking()) {
                setForceTurning(stack, !forceTurning);
                player.sendMessage(Text.translatable(forceTurning ? "item.playercollars.clicker.turn_disable" : "item.playercollars.clicker.turn_enable"), true);
                return TypedActionResult.consume(stack);
            }

            world.playSoundFromEntity(null, player, PlayerCollarsMod.CLICKER_ON, SoundCategory.PLAYERS, 1, 1);

            if (forceTurning) {
                var clickerDistance = player.getAttributeValue(PlayerCollarsMod.ATTR_CLICKER_DISTANCE);
                System.out.println("clicker distance = " + clickerDistance);
                var targets = ((ServerWorld) world).getPlayers((p) -> !p.isPartOf(player) && p.isInRange(player, clickerDistance));

                PacketLookAtLerped packet = new PacketLookAtLerped(player);
                for (ServerPlayerEntity target : targets) {
                    System.out.println("maybe sending look packet for " + target.getEntityName());
                    TrinketsApi.getTrinketComponent(target)
                            .map((x) -> x.getEquipped((y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG)))
                            .map((x) -> PlayerCollarsMod.filterStacksByOwner(x, player.getUuid(), target.getUuid()))
                            .ifPresent((x) -> ServerPlayNetworking.send(target, packet));
                }
            }
        }
        return TypedActionResult.fail(player.getStackInHand(hand));
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!world.isClient) {
            world.playSoundFromEntity(null, user, PlayerCollarsMod.CLICKER_OFF, SoundCategory.PLAYERS, 1, 1);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (getForceTurning(stack))
            tooltip.add(Text.translatable("item.playercollars.clicker.turn"));
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND)
            return ImmutableMultimap.of();

        var lure = EnchantmentHelper.getLure(stack);
        if (lure == 0)
            return ImmutableMultimap.of();

        EntityAttributeModifier attribute = new EntityAttributeModifier(
                LURE_CLICKER_DISTANCE_ATTRIBUTE,
                getTranslationKey(),
                lure * 4,
                EntityAttributeModifier.Operation.ADDITION
        );

        return ImmutableMultimap.of(PlayerCollarsMod.ATTR_CLICKER_DISTANCE, attribute);
    }
}
