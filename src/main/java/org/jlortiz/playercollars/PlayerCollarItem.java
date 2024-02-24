package org.jlortiz.playercollars;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.client.DyingStationScreen;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class PlayerCollarItem extends Item implements DyeableLeatherItem {
    public PlayerCollarItem() {
        super(new Item.Properties().stacksTo(1).tab(PlayerCollarsMod.TAB));
    }

    public enum TagType {
        GOLD(0xFDF55F, Tags.Items.INGOTS_GOLD),
        IRON(0xD8D8D8, Tags.Items.INGOTS_IRON),
        COPPER(0xE77C56, Tags.Items.INGOTS_COPPER),
        NETHERITE(0x5A575A, Tags.Items.INGOTS_NETHERITE),
        DIAMOND(0x4AEDD9, Tags.Items.GEMS_DIAMOND),
        STONE(0x6B6B6B, Tags.Items.STONE),
        WOOD(0x907549, ItemTags.PLANKS),
        AMETHYST(0xCFA0F3, Tags.Items.GEMS_AMETHYST),
        EMERALD(0x17DD62, Tags.Items.GEMS_EMERALD);
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
        return $$1 != null && $$1.contains("paw", 99) ? $$1.getInt("paw") : MaterialColor.COLOR_BLUE.col;
    }

    public void setPawColor(ItemStack itemStack, int col) {
        CompoundTag $$1 = itemStack.getOrCreateTagElement("display");
        $$1.putInt("paw", col);
    }

    public static ItemStack getInstance(TagType tag) {
        ItemStack is = new ItemStack(PlayerCollarsMod.COLLAR_ITEM.get());
        CompoundTag $$1 = is.getOrCreateTagElement("display");
        $$1.putInt("tagType", tag.ordinal());
        return is;
    }

    public static ItemStack getInstance(TagType tag, int paw) {
        ItemStack is = new ItemStack(PlayerCollarsMod.COLLAR_ITEM.get());
        CompoundTag $$1 = is.getOrCreateTagElement("display");
        $$1.putInt("paw", paw);
        $$1.putInt("tagType", tag.ordinal());
        return is;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        InteractionResultHolder<ItemStack> ir = super.use(p_41432_, p_41433_, p_41434_);
        if (ir.getResult() == InteractionResult.PASS && p_41433_.isCrouching() && p_41432_.isClientSide) {
            Minecraft.getInstance().setScreen(new DyingStationScreen(ir.getObject()));
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, ir.getObject());
        }
        return ir;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        p_41423_.add(Component.translatable("item.playercollars.collar_paw", Integer.toHexString(getPawColor(p_41421_))).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Component getName(ItemStack p_41458_) {
        CompoundTag $$1 = p_41458_.getTagElement("display");
        if ($$1 == null || !$$1.contains("tagType")) {
            return super.getName(p_41458_);
        }
        int $$2 = $$1.getInt("tagType");
        if ($$2 >= TagType.values().length) {
            return super.getName(p_41458_);
        }
        return Component.translatable("item.playercollars.tag." + TagType.values()[$$2].name()).append(" ").append(super.getName(p_41458_));
    }

    @Override
    public void fillItemCategory(CreativeModeTab p_41391_, NonNullList<ItemStack> p_41392_) {
        if (this.allowedIn(p_41391_)) {
            for (TagType t : TagType.values())
                p_41392_.add(getInstance(t));
        }
    }
}
