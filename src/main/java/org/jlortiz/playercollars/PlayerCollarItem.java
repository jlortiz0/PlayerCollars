package org.jlortiz.playercollars;

import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ForgeItemTagsProvider;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

public class PlayerCollarItem extends Item implements DyeableLeatherItem {
    public PlayerCollarItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public enum TagType {
        GOLD(0xFDF55F, Tags.Items.INGOTS_GOLD),
        IRON(0xD8D8D8, Tags.Items.INGOTS_IRON),
        COPPER(0xE77C56, Tags.Items.INGOTS_COPPER),
        NETHERITE(0x5A575A, Tags.Items.INGOTS_NETHERITE);
        public final int color;
        public final TagKey<Item> item;
        TagType(int color, TagKey<Item> item) {
            this.color = color;
            this.item = item;
        }

        public static Ingredient getIngredient() {
            return Ingredient.fromValues(Stream.of(Arrays.stream(TagType.values()).map((i) -> new Ingredient.TagValue(i.item))).flatMap(Function.identity()));
        }
    }

    @Override
    public int getColor(ItemStack itemStack) {
        CompoundTag $$1 = itemStack.getTagElement("display");
        return $$1 != null && $$1.contains("color", 99) ? $$1.getInt("color") : MaterialColor.COLOR_RED.col;
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
        return $$1 != null && $$1.contains("paw", 99) ? $$1.getInt("paw") : MaterialColor.COLOR_BROWN.col;
    }

    public static ItemStack getInstance(TagType tag, int paw) {
        ItemStack is = new ItemStack(PlayerCollarsMod.COLLAR_ITEM.get());
        CompoundTag $$1 = is.getOrCreateTagElement("display");
        $$1.putInt("paw", paw);
        $$1.putInt("tagType", tag.ordinal());
        return is;
    }
}
