package org.jlortiz.playercollars.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.Optional;

public final class PacketStampDeed implements FabricPacket {
    public static final PacketStampDeed INSTANCE = new PacketStampDeed();
    public static final Identifier ID = Identifier.of(PlayerCollarsMod.MOD_ID, "stamp_deed");
    public static final PacketType<PacketStampDeed> TYPE = PacketType.create(ID, PacketStampDeed::new);

    private PacketStampDeed(PacketByteBuf buf) {
    }

    private PacketStampDeed() {
    }

    public static void handle(PacketStampDeed packet, ServerPlayerEntity player, PacketSender responseSender) {
        ItemStack is = player.getMainHandStack();
        if (!is.isEmpty() && is.isOf(PlayerCollarsMod.DEED_OF_OWNERSHIP)) {
            var owner = NbtUtil.getDeedOwner(is);

            if (owner == null)
                return;

            owner = new OwnerComponent(
                    owner.uuid(), owner.name(), Optional.of(player.getUuid()), Optional.of(player.getName().getString())
            );

            is = PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED.getDefaultStack();
            NbtUtil.setDeedOwner(is, owner);
            player.equipStack(EquipmentSlot.MAINHAND, is);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
