package org.jlortiz.playercollars.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.Optional;
import java.util.function.Supplier;

public final class PacketStampDeed {
    public static final PacketStampDeed INSTANCE = new PacketStampDeed();

    public static PacketStampDeed decode(FriendlyByteBuf buf) { return INSTANCE; }
    public static void encode(PacketStampDeed packet, FriendlyByteBuf buf) {}

    public static void handle(PacketStampDeed packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            ItemStack is = player.getMainHandItem();
            if (!is.isEmpty() && is.is(PlayerCollarsMod.DEED_OF_OWNERSHIP.get())) {
                var owner = NbtUtil.getDeedOwner(is);
                if (owner == null) return;
                owner = new OwnerComponent(owner.uuid(), owner.name(),
                        Optional.of(player.getUUID()), Optional.of(player.getName().getString()));
                is = PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED.get().getDefaultInstance();
                NbtUtil.setDeedOwner(is, owner);
                player.setItemSlot(EquipmentSlot.MAINHAND, is);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
