package org.jlortiz.playercollars;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;

public class ClickerItem extends TieredItem implements DyeableLeatherItem {
    public ClickerItem() {
        super(Tiers.IRON, new Item.Properties().tab(PlayerCollarsMod.TAB).stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        p_41433_.startUsingItem(p_41434_);
        if (!p_41432_.isClientSide) {
            p_41432_.playSound(null, p_41433_, PlayerCollarsMod.CLICKER_ON, SoundSource.PLAYERS, 1, 1);
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
            p_41413_.playSound(null, p_41414_, PlayerCollarsMod.CLICKER_OFF, SoundSource.PLAYERS, 1, 1);
        }
    }

    public boolean isValidRepairItem(ItemStack p_43311_, ItemStack p_43312_) {
        return p_43312_.is(Tags.Items.INGOTS_IRON);
    }

    @Override
    public int getColor(ItemStack itemStack) {
        CompoundTag $$1 = itemStack.getTagElement("display");
        return $$1 != null && $$1.contains("color", 99) ? $$1.getInt("color") : 0xFFFFFF;
    }
}
