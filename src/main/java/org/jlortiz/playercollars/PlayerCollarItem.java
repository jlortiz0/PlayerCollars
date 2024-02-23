package org.jlortiz.playercollars;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PlayerCollarItem extends Item implements DyeableLeatherItem {
    public PlayerCollarItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public enum TagType {
        GOLD(0xFDF55F),
        IRON(0xD8D8D8),
        COPPER(0xE77C56);
        public final int color;
        TagType(int color) {
            this.color = color;
        }
    }

    @Override
    public int getColor(ItemStack itemStack) {
        CompoundTag $$1 = itemStack.getTagElement("display");
        return $$1 != null && $$1.contains("color", 99) ? $$1.getInt("color") : 0xFF0000;
    }

    public int getTagColor(ItemStack itemStack) {
        CompoundTag $$1 = itemStack.getTagElement("display");
        if ($$1 == null || !$$1.contains("tagType")) {
            return TagType.GOLD.color;
        }
        int $$2 = $$1.getInt("tagType");
        return $$2 >= TagType.values().length ? 0 : TagType.values()[$$2].color;
    }

    public int getPawColor(ItemStack itemStack) {
        CompoundTag $$1 = itemStack.getTagElement("display");
        return $$1 != null && $$1.contains("paw", 99) ? $$1.getInt("paw") : 0xFF0000;
    }

    public static ItemStack getInstance(TagType tag, int paw) {
        ItemStack is = new ItemStack(PlayerCollarsMod.COLLAR_ITEM.get());
        CompoundTag $$1 = is.getOrCreateTagElement("display");
        $$1.putInt("paw", paw);
        $$1.putInt("tagType", tag.ordinal());
        return is;
    }
}
