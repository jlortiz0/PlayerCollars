package org.jlortiz.playercollars;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketUpdateCollar {
    private final int pawColor, color;
    private final OwnerState os;
    public PacketUpdateCollar(ItemStack is, OwnerState os) {
        PlayerCollarItem item = PlayerCollarsMod.COLLAR_ITEM.get();
        this.pawColor = item.getPawColor(is);
        this.color = item.getColor(is);
        this.os = os;
    }

    public PacketUpdateCollar(FriendlyByteBuf buf) {
        this.color = buf.readInt();
        this.pawColor = buf.readInt();
        this.os = buf.readEnum(OwnerState.class);
    }

    public enum OwnerState {
        NOP, DEL, ADD
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.color);
        buf.writeInt(this.pawColor);
        buf.writeEnum(this.os);
    }
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Player p = context.get().getSender();
            ItemStack is = p.getMainHandItem();
            if (!is.isEmpty() && is.getItem() instanceof PlayerCollarItem item) {
                item.setColor(is, color);
                item.setPawColor(is, pawColor);
                if (os == OwnerState.DEL) {
                    item.setOwner(is, null, null);
                } else if (os == OwnerState.ADD) {
                    item.setOwner(is, p.getUUID(), p.getName().getString());
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
