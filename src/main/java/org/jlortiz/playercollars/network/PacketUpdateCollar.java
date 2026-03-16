package org.jlortiz.playercollars.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.util.NbtUtil;

public record PacketUpdateCollar(OwnerState os, int color, int tagColor) implements FabricPacket {
    public static final Identifier ID = Identifier.of(PlayerCollarsMod.MOD_ID, "update_collar");
    public static final PacketType<PacketUpdateCollar> TYPE = PacketType.create(ID, PacketUpdateCollar::new);

    public PacketUpdateCollar(ItemStack is, OwnerState os) {
        this(os, ((DyeableItem) is.getItem()).getColor(is), ((CollarItem) is.getItem()).getTagColor(is));
    }

    public PacketUpdateCollar(PacketByteBuf buf) {
        this(buf.readEnumConstant(OwnerState.class), buf.readInt(), buf.readInt());
    }

    public static void handle(PacketUpdateCollar packet, ServerPlayerEntity player, PacketSender responseSender) {
        ItemStack is = player.getMainHandStack();
        if (!is.isEmpty() && is.getItem() instanceof CollarItem) {
            NbtUtil.setColor(is, packet.color);
            NbtUtil.setTagColor(is, packet.tagColor);
            if (packet.os == OwnerState.DEL) {
                NbtUtil.setDeedOwner(is, null);
            } else if (packet.os == OwnerState.ADD) {
                NbtUtil.setDeedOwner(is, new OwnerComponent(player.getUuid(), player.getName().getString()));
            }
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(this.os);
        buf.writeInt(this.color);
        buf.writeInt(this.tagColor);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public enum OwnerState {
        NOP, DEL, ADD
    }
}
