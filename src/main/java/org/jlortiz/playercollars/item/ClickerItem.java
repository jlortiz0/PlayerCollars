package org.jlortiz.playercollars.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.jlortiz.playercollars.PacketLookAtLerped;
import org.jlortiz.playercollars.PlayerCollarsMod;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ClickerItem extends Item implements DyeableLeatherItem {
    public ClickerItem() {
        super(new Item.Properties().stacksTo(1).tab(PlayerCollarsMod.TAB));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.FISHING_SPEED;
    }

    @Override
    public boolean isEnchantable(ItemStack p_41456_) {
        return true;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 40;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        p_41433_.startUsingItem(p_41434_);
        if (!p_41432_.isClientSide) {
            int level = p_41433_.getItemInHand(p_41434_).getEnchantmentLevel(Enchantments.FISHING_SPEED);
            if (level > 0) {
                final int trueLevel = 4 << level;
                List<ServerPlayer> plrs = ((ServerLevel) p_41432_).getPlayers((p) -> !p.is(p_41433_) && p.closerThan(p_41433_, trueLevel));
                for (ServerPlayer p : plrs) {
                    CuriosApi.getCuriosHelper().getCuriosHandler(p).ifPresent((handler) ->
                        handler.getStacksHandler("necklace").ifPresent((slot) -> {
                            ItemStack is = PlayerCollarsMod.filterStacksByOwner(slot.getStacks(), p_41433_.getUUID());
                            if (is == null) {
                                is = PlayerCollarsMod.filterStacksByOwner(slot.getCosmeticStacks(), p_41433_.getUUID());
                            }
                            if (is != null) {
                                PacketLookAtLerped packet = new PacketLookAtLerped(p_41433_);
                                PlayerCollarsMod.NETWORK.send(PacketDistributor.PLAYER.with(() -> p), packet);
                            }
                    }));
                }
            }
            p_41432_.playSound(null, p_41433_, PlayerCollarsMod.CLICKER_ON.get(), SoundSource.PLAYERS, 1, 1);
        }
        return InteractionResultHolder.fail(p_41433_.getItemInHand(p_41434_));
    }

    @Override
    public int getUseDuration(ItemStack p_41454_) {
        return Integer.MAX_VALUE;
    }

    @Override
    public void releaseUsing(ItemStack p_41412_, Level p_41413_, LivingEntity p_41414_, int p_41415_) {
        if (!p_41413_.isClientSide) {
            p_41413_.playSound(null, p_41414_, PlayerCollarsMod.CLICKER_OFF.get(), SoundSource.PLAYERS, 1, 1);
        }
    }

    @Override
    public int getColor(ItemStack itemStack) {
        CompoundTag $$1 = itemStack.getTagElement("display");
        return $$1 != null && $$1.contains("color", 99) ? $$1.getInt("color") : 0xFFFFFF;
    }
}
