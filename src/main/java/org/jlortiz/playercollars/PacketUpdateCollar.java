package org.jlortiz.playercollars;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateCollar {
    private final int pawColor, color;
    public PacketUpdateCollar(ItemStack is) {
        PlayerCollarItem item = PlayerCollarsMod.COLLAR_ITEM.get();
        this.pawColor = item.getPawColor(is);
        this.color = item.getColor(is);
    }

    public PacketUpdateCollar(FriendlyByteBuf buf) {
        this.color = buf.readInt();
        this.pawColor = buf.readInt();
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.color);
        buf.writeInt(this.pawColor);
    }
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Player p = context.get().getSender();
            ItemStack is = p.getMainHandItem();
            if (!is.isEmpty() && is.getItem() instanceof PlayerCollarItem item) {
                item.setColor(is, color);
                item.setPawColor(is, pawColor);
            }
        });
        context.get().setPacketHandled(true);
    }
}
