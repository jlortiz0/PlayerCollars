package org.jlortiz.playercollars;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jlortiz.playercollars.item.CollarItem;

public class PacketUpdateCollar {
    private final int pawColor, color;
    private final OwnerState os;
    public PacketUpdateCollar(ItemStack is, OwnerState os) {
        CollarItem item = PlayerCollarsMod.COLLAR_ITEM;
        this.pawColor = item.getPawColor(is);
        this.color = item.getColor(is);
        this.os = os;
    }

    public PacketUpdateCollar(PacketByteBuf buf) {
        this.color = buf.readInt();
        this.pawColor = buf.readInt();
        this.os = buf.readEnumConstant(OwnerState.class);
    }

    public enum OwnerState {
        NOP, DEL, ADD
    }

    public void encode(PacketByteBuf buf) {
        buf.writeInt(this.color);
        buf.writeInt(this.pawColor);
        buf.writeEnumConstant(this.os);
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ItemStack is = player.getMainHandStack();
        if (!is.isEmpty() && is.getItem() instanceof CollarItem item) {
            PacketUpdateCollar packet = new PacketUpdateCollar(buf);
            item.setColor(is, packet.color);
            item.setPawColor(is, packet.pawColor);
            if (packet.os == OwnerState.DEL) {
                item.setOwner(is, null, null);
            } else if (packet.os == OwnerState.ADD) {
                item.setOwner(is, player.getUuid(), player.getName().getString());
            }
        }
    }
}
