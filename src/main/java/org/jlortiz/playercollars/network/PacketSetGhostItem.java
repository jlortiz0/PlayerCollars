package org.jlortiz.playercollars.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;

public record PacketSetGhostItem(int syncId, int slot, ItemStack item) implements CustomPayload {
    public static final Id<PacketSetGhostItem> ID = new Id<>(Identifier.of(PlayerCollarsMod.MOD_ID, "set_ghost_item"));
    public static final PacketCodec<RegistryByteBuf, PacketSetGhostItem> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, PacketSetGhostItem::syncId,
            PacketCodecs.INTEGER, PacketSetGhostItem::slot,
            ItemStack.PACKET_CODEC, PacketSetGhostItem::item,
            PacketSetGhostItem::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handle(ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ScreenHandler handler = context.player().currentScreenHandler;
            if (handler.syncId == syncId && handler instanceof GhostSlotContainer gsc && gsc.isGhostSlot(slot)) {
                handler.setStackInSlot(slot, handler.nextRevision(), item);
            }
        });
    }
}
