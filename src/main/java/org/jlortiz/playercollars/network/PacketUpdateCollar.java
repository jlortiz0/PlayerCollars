package org.jlortiz.playercollars.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.function.Supplier;

public record PacketUpdateCollar(OwnerState os, int color, int tagColor) {

    public PacketUpdateCollar(ItemStack is, OwnerState os) {
        this(os, NbtUtil.getColor(is, 0xFFFFFF),
             is.getItem() instanceof CollarItem ci ? ci.getTagColor(is) : 0);
    }

    public static PacketUpdateCollar decode(FriendlyByteBuf buf) {
        return new PacketUpdateCollar(buf.readEnum(OwnerState.class), buf.readInt(), buf.readInt());
    }

    public static void encode(PacketUpdateCollar packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.os);
        buf.writeInt(packet.color);
        buf.writeInt(packet.tagColor);
    }

    public static void handle(PacketUpdateCollar packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            ItemStack is = player.getMainHandItem();
            if (!is.isEmpty() && is.getItem() instanceof CollarItem) {
                NbtUtil.setColor(is, packet.color);
                NbtUtil.setTagColor(is, packet.tagColor);
                if (packet.os == OwnerState.DEL) {
                    NbtUtil.setDeedOwner(is, null);
                } else if (packet.os == OwnerState.ADD) {
                    NbtUtil.setDeedOwner(is, new OwnerComponent(player.getUUID(), player.getName().getString()));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum OwnerState { NOP, DEL, ADD }
}
