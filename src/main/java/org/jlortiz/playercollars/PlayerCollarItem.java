package org.jlortiz.playercollars;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PlayerCollarItem extends Item implements DyeableLeatherItem {
    public PlayerCollarItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public int getColor(ItemStack itemStack) {
        CompoundTag $$1 = itemStack.getTagElement("display");
        return $$1 != null && $$1.contains("color", 99) ? $$1.getInt("color") : 0xFF0000;
    }
}
