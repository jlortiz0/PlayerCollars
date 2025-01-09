package org.jlortiz.playercollars.item;

import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jlortiz.playercollars.PacketLookAtLerped;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.List;

public class ClickerItem extends Item {
    public ClickerItem() {
        super(new Item.Settings().maxCount(1));
    }

    @Override
    public boolean isEnchantable(ItemStack p_41456_) {
        return true;
    }

    @Override
    public int getEnchantability() {
        return 40;
    }

    @Override
    public TypedActionResult<ItemStack> use(World p_41432_, PlayerEntity p_41433_, Hand p_41434_) {
        p_41433_.setCurrentHand(p_41434_);
        if (!p_41432_.isClient) {
            int level = EnchantmentHelper.getLevel(Enchantments.LURE, p_41433_.getStackInHand(p_41434_));
            if (level > 0) {
                final int trueLevel = 4 << level;
                List<ServerPlayerEntity> plrs = ((ServerWorld) p_41432_).getPlayers((p) -> !p.isPartOf(p_41433_) && p.isInRange(p_41433_, trueLevel));
                PacketLookAtLerped packet = new PacketLookAtLerped(p_41433_);
                for (ServerPlayerEntity p : plrs) {
                    TrinketsApi.getTrinketComponent(p).map((x) -> x.getEquipped(PlayerCollarsMod.COLLAR_ITEM))
                            .map((x) -> PlayerCollarsMod.filterStacksByOwner(x, p_41433_.getUuid()))
                            .ifPresent((x) -> ServerPlayNetworking.send(p, packet));
                }
            }
            p_41432_.playSoundFromEntity(null, p_41433_, PlayerCollarsMod.CLICKER_ON, SoundCategory.PLAYERS, 1, 1);
        }
        return TypedActionResult.fail(p_41433_.getStackInHand(p_41434_));
    }

    @Override
    public int getMaxUseTime(ItemStack p_41454_) {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onStoppedUsing(ItemStack p_41412_, World p_41413_, LivingEntity p_41414_, int p_41415_) {
        if (!p_41413_.isClient) {
            p_41413_.playSoundFromEntity(null, p_41414_, PlayerCollarsMod.CLICKER_OFF, SoundCategory.PLAYERS, 1, 1);
        }
    }

    public int getColor(ItemStack itemStack) {
        DyedColorComponent $$1 = itemStack.get(DataComponentTypes.DYED_COLOR);
        return $$1 != null ? $$1.rgb() : -1;
    }
}
