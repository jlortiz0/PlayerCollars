package org.jlortiz.playercollars.item;

import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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

import java.util.List;

public class ClickerItem extends Item {
    public ClickerItem() {
        super(new Item.Settings().maxCount(1));
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
            ItemStack is = player.getStackInHand(hand);
            if (player.isSneaking()) {
                if (is.getNbt() != null && is.getNbt().getBoolean("force_turning")) {
                    is.getOrCreateNbt().putBoolean("force_turning", false);
                    player.sendMessage(Text.translatable("item.playercollars.clicker.turn_disable"), true);
                } else {
                    is.getOrCreateNbt().putBoolean("force_turning", true);
                    player.sendMessage(Text.translatable("item.playercollars.clicker.turn_enable"), true);
                }
                return TypedActionResult.consume(is);
            }

            if (is.getNbt() != null && is.getNbt().getBoolean("force_turning")) {
                List<ServerPlayerEntity> targets = ((ServerWorld) world).getPlayers((p) -> {
                    // noinspection CodeBlock2Expr
                    return !p.isPartOf(player) && p.isInRange(player, p.getAttributeValue(PlayerCollarsMod.ATTR_CLICKER_DISTANCE));
                });

                PacketLookAtLerped packet = new PacketLookAtLerped(player);
                for (ServerPlayerEntity target : targets) {
                    TrinketsApi.getTrinketComponent(target)
                            .map((x) -> x.getEquipped((y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG)))
                            .map((x) -> PlayerCollarsMod.filterStacksByOwner(x, player.getUuid(), target.getUuid()))
                            .ifPresent((x) -> ServerPlayNetworking.send(target, packet));
                }
            }
            world.playSoundFromEntity(null, player, PlayerCollarsMod.CLICKER_ON, SoundCategory.PLAYERS, 1, 1);
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
        if (stack.getNbt() != null && stack.getNbt().getBoolean("force_turning"))
            tooltip.add(Text.translatable("item.playercollars.clicker.turn"));
    }
}
