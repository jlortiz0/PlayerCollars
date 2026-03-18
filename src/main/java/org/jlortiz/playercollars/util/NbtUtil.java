package org.jlortiz.playercollars.util;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class NbtUtil {
    private static final Logger logger = LoggerFactory.getLogger(NbtUtil.class);

    private NbtUtil() {}

    public static @Nullable OwnerComponent getDeedOwner(ItemStack is) {
        if (!is.hasTag()) return null;
        CompoundTag ownerCompound = is.getTagElement("owner");
        if (ownerCompound == null) return null;
        return PlayerCollarsMod.OWNER_COMPONENT_CODEC.parse(NbtOps.INSTANCE, ownerCompound)
                .resultOrPartial(logger::error).orElse(null);
    }

    public static void setDeedOwner(ItemStack is, OwnerComponent ownerComponent) {
        if (ownerComponent == null) {
            if (is.hasTag()) is.removeTagKey("owner");
            return;
        }
        var ownerCompound = PlayerCollarsMod.OWNER_COMPONENT_CODEC.encodeStart(NbtOps.INSTANCE, ownerComponent)
                .resultOrPartial(logger::error).orElse(null);
        if (ownerCompound != null) is.addTagElement("owner", ownerCompound);
    }

    public static int getColor(ItemStack stack, int defaultColor) {
        CompoundTag display = stack.getTagElement("display");
        return display != null && display.contains("color", Tag.TAG_INT) ? display.getInt("color") : defaultColor;
    }

    public static void setColor(ItemStack stack, int color) {
        stack.getOrCreateTagElement("display").putInt("color", color);
    }

    public static int getTagColor(ItemStack stack, int defaultColor) {
        CompoundTag display = stack.getTagElement("display");
        return display != null && display.contains("tag", Tag.TAG_INT) ? display.getInt("tag") : defaultColor;
    }

    public static void setTagColor(ItemStack stack, int color) {
        stack.getOrCreateTagElement("display").putInt("tag", color);
    }

    public static List<Either<TagKey<Item>, ResourceKey<Item>>> getHeldItems(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains("held_items", Tag.TAG_LIST)) return List.of();
        var nbt = stack.getTag().get("held_items");
        return PlayerCollarsMod.HELD_ITEMS_COMPONENT_CODEC.parse(NbtOps.INSTANCE, nbt)
                .result().orElseGet(List::of);
    }

    public static void setHeldItems(ItemStack stack, List<Either<TagKey<Item>, ResourceKey<Item>>> items) {
        var nbt = PlayerCollarsMod.HELD_ITEMS_COMPONENT_CODEC.encodeStart(NbtOps.INSTANCE, items)
                .resultOrPartial(logger::error).orElseGet(ListTag::new);
        stack.getOrCreateTag().put("held_items", nbt);
    }

    public static void writeHeldItems(FriendlyByteBuf buf, List<Either<TagKey<Item>, ResourceKey<Item>>> items) {
        var sub = PlayerCollarsMod.HELD_ITEMS_COMPONENT_CODEC.encodeStart(NbtOps.INSTANCE, items)
                .result().orElseGet(ListTag::new);
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("held_items", sub);
        buf.writeNbt(wrapper);
    }

    public static List<Either<TagKey<Item>, ResourceKey<Item>>> readHeldItems(FriendlyByteBuf buf) {
        CompoundTag wrapper = buf.readNbt();
        if (wrapper == null || !wrapper.contains("held_items")) return List.of();
        return PlayerCollarsMod.HELD_ITEMS_COMPONENT_CODEC.parse(NbtOps.INSTANCE, wrapper.get("held_items"))
                .result().orElseGet(List::of);
    }

    public static List<Either<TagKey<Block>, ResourceKey<Block>>> getCanInteract(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains("can_interact", Tag.TAG_LIST)) return List.of();
        var nbt = stack.getTag().get("can_interact");
        return PlayerCollarsMod.CAN_INTERACT_COMPONENT_CODEC.parse(NbtOps.INSTANCE, nbt)
                .result().orElseGet(List::of);
    }

    public static void setCanInteract(ItemStack stack, List<Either<TagKey<Block>, ResourceKey<Block>>> items) {
        var nbt = PlayerCollarsMod.CAN_INTERACT_COMPONENT_CODEC.encodeStart(NbtOps.INSTANCE, items)
                .resultOrPartial(logger::error).orElseGet(ListTag::new);
        stack.getOrCreateTag().put("can_interact", nbt);
    }

    public static void writeCanInteract(FriendlyByteBuf buf, List<Either<TagKey<Block>, ResourceKey<Block>>> items) {
        var sub = PlayerCollarsMod.CAN_INTERACT_COMPONENT_CODEC.encodeStart(NbtOps.INSTANCE, items)
                .result().orElseGet(ListTag::new);
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("can_interact", sub);
        buf.writeNbt(wrapper);
    }

    public static List<Either<TagKey<Block>, ResourceKey<Block>>> readCanInteract(FriendlyByteBuf buf) {
        CompoundTag wrapper = buf.readNbt();
        if (wrapper == null || !wrapper.contains("can_interact")) return List.of();
        return PlayerCollarsMod.CAN_INTERACT_COMPONENT_CODEC.parse(NbtOps.INSTANCE, wrapper.get("can_interact"))
                .result().orElseGet(List::of);
    }
}
