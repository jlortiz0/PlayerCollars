package org.jlortiz.playercollars.util;

import com.mojang.datafixers.util.Either;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class NbtUtil {
    private static final Logger logger = LoggerFactory.getLogger(NbtUtil.class);

    private NbtUtil() {
    }

    public static @Nullable OwnerComponent getDeedOwner(ItemStack is) {
        NbtCompound ownerCompound = is.getSubNbt("owner");

        if (ownerCompound == null)
            return null;

        return PlayerCollarsMod.OWNER_COMPONENT_CODEC.parse(NbtOps.INSTANCE, ownerCompound)
                .resultOrPartial(logger::error)
                .orElse(null);
    }

    public static void setDeedOwner(ItemStack is, OwnerComponent ownerComponent) {
        if (ownerComponent == null) {
            is.removeSubNbt("owner");
            return;
        }

        var ownerCompound = PlayerCollarsMod.OWNER_COMPONENT_CODEC.encodeStart(NbtOps.INSTANCE, ownerComponent)
                .resultOrPartial(logger::error)
                .orElse(null);

        is.setSubNbt("owner", ownerCompound);
    }

    public static int getColor(ItemStack itemStack, int defaultColor) {
        NbtCompound diplayCompound = itemStack.getSubNbt("display");

        return diplayCompound != null && diplayCompound.contains("color", NbtElement.NUMBER_TYPE) ? diplayCompound.getInt("color") : defaultColor;
    }

    public static void setColor(ItemStack itemStack, int color) {
        NbtCompound displayCoumpound = itemStack.getOrCreateSubNbt("display");
        displayCoumpound.putInt("color", color);
    }

    public static int getTagColor(ItemStack itemStack, int defaultColor) {
        NbtCompound displayCompound = itemStack.getSubNbt("display");
        return displayCompound != null && displayCompound.contains("tag", NbtElement.NUMBER_TYPE) ? displayCompound.getInt("tag") : defaultColor;
    }

    public static void setTagColor(ItemStack itemStack, int color) {
        NbtCompound displayCoumpound = itemStack.getOrCreateSubNbt("display");
        displayCoumpound.putInt("tag", color);
    }

    public static List<Either<TagKey<Item>, RegistryKey<Item>>> getHeldItems(ItemStack itemStack) {
        var nbtCompound = itemStack.getNbt();
        if (nbtCompound == null || !nbtCompound.contains("held_items", NbtElement.LIST_TYPE))
            return List.of();

        var heldItemsNbt = nbtCompound.get("held_items");

        return PlayerCollarsMod.HELD_ITEMS_COMPONENT_CODEC.parse(NbtOps.INSTANCE, heldItemsNbt)
                .result()
                .orElseGet(List::of);
    }

    public static void writeHeldItems(PacketByteBuf buf, List<Either<TagKey<Item>, RegistryKey<Item>>> heldItems) {
        var heldItemsSubNbt = PlayerCollarsMod.HELD_ITEMS_COMPONENT_CODEC.encodeStart(NbtOps.INSTANCE, heldItems)
                .result()
                .orElseGet(NbtList::new);

        var heldItemsNbt = new NbtCompound();
        heldItemsNbt.put("held_items", heldItemsSubNbt);

        buf.writeNbt(heldItemsNbt);
    }

    public static List<Either<TagKey<Item>, RegistryKey<Item>>> readHeldItems(PacketByteBuf buf) {
        NbtCompound heldItemsNbt = buf.readNbt();

        if (heldItemsNbt == null)
            return List.of();

        var heldItemsSubNbt = heldItemsNbt.get("held_items");

        if (heldItemsSubNbt == null)
            return List.of();

        return PlayerCollarsMod.HELD_ITEMS_COMPONENT_CODEC.parse(NbtOps.INSTANCE, heldItemsSubNbt)
                .result()
                .orElseGet(List::of);
    }

    public static void setHeldItems(ItemStack itemStack, List<Either<TagKey<Item>, RegistryKey<Item>>> heldItems) {
        var heldItemsNbt = PlayerCollarsMod.HELD_ITEMS_COMPONENT_CODEC.encodeStart(NbtOps.INSTANCE, heldItems)
                .resultOrPartial(logger::error)
                .orElseGet(NbtList::new);

        itemStack.setSubNbt("held_items", heldItemsNbt);
    }

    public static List<Either<TagKey<Block>, RegistryKey<Block>>> getCanInteract(ItemStack itemStack) {
        var nbtCompound = itemStack.getNbt();
        if (nbtCompound == null || !nbtCompound.contains("can_interact", NbtElement.LIST_TYPE))
            return List.of();

        var heldItemsNbt = nbtCompound.get("can_interact");

        return PlayerCollarsMod.CAN_INTERACT_COMPONENT_CODEC.parse(NbtOps.INSTANCE, heldItemsNbt)
                .result()
                .orElseGet(List::of);
    }

    public static void writeCanInteract(PacketByteBuf buf, List<Either<TagKey<Block>, RegistryKey<Block>>> canInteract) {
        var canInteractSubNbt = PlayerCollarsMod.CAN_INTERACT_COMPONENT_CODEC.encodeStart(NbtOps.INSTANCE, canInteract)
                .result()
                .orElseGet(NbtList::new);

        var canInteractNbt = new NbtCompound();
        canInteractNbt.put("can_interact", canInteractSubNbt);

        buf.writeNbt(canInteractNbt);
    }

    public static List<Either<TagKey<Block>, RegistryKey<Block>>> readCanInteract(PacketByteBuf buf) {
        NbtCompound heldItemsNbt = buf.readNbt();

        if (heldItemsNbt == null)
            return List.of();

        var heldItemsSubNbt = heldItemsNbt.get("can_interact");

        if (heldItemsSubNbt == null)
            return List.of();

        return PlayerCollarsMod.CAN_INTERACT_COMPONENT_CODEC.parse(NbtOps.INSTANCE, heldItemsSubNbt)
                .result()
                .orElseGet(List::of);
    }

    public static void setCanInteract(ItemStack itemStack, List<Either<TagKey<Block>, RegistryKey<Block>>> heldItems) {
        var heldItemsNbt = PlayerCollarsMod.CAN_INTERACT_COMPONENT_CODEC.encodeStart(NbtOps.INSTANCE, heldItems)
                .resultOrPartial(logger::error)
                .orElseGet(NbtList::new);

        itemStack.setSubNbt("can_interact", heldItemsNbt);
    }
}
