package org.jlortiz.playercollars.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.util.NbtUtil;

public record PacketUpdateCollar(OwnerState os, int color, int pawColor) implements FabricPacket {
    public static final Identifier ID = Identifier.of(PlayerCollarsMod.MOD_ID, "update_collar");
    public static final PacketType<PacketUpdateCollar> TYPE = PacketType.create(ID, PacketUpdateCollar::new);

    public PacketUpdateCollar(ItemStack is, OwnerState os) {
        this(os, NbtUtil.getColor(is), NbtUtil.getPawColor(is));
    }

    public PacketUpdateCollar(PacketByteBuf buf) {
        this(buf.readEnumConstant(OwnerState.class), buf.readInt(), buf.readInt());
    }

    public static void handle(PacketUpdateCollar packet, ServerPlayerEntity player, PacketSender responseSender) {
        ItemStack is = player.getMainHandStack();
        if (!is.isEmpty() && is.isOf(PlayerCollarsMod.COLLAR_ITEM)) {
            NbtUtil.setColor(is, packet.color);
            NbtUtil.setPawColor(is, packet.pawColor);
            if (packet.os == OwnerState.DEL) {
                NbtUtil.setOwner(is, null, null);
            } else if (packet.os == OwnerState.ADD) {
                NbtUtil.setOwner(is, player.getUuid(), player.getName().getString());
            }
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(this.os);
        buf.writeInt(this.color);
        buf.writeInt(this.pawColor);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public enum OwnerState {
        NOP, DEL, ADD;

        public static OwnerState fromInt(int ind) {
            return OwnerState.values()[ind];
        }
    }
}
