package org.jlortiz.playercollars;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import org.jlortiz.playercollars.client.RotationLerpHandler;

public class PacketLookAtLerped extends LookAtS2CPacket {
    public PacketLookAtLerped(Entity p_132783_) {
        super(EntityAnchorArgumentType.EntityAnchor.EYES, p_132783_.getX(), p_132783_.getEyeY(), p_132783_.getZ());
    }

    public PacketLookAtLerped(PacketByteBuf p_179146_) {
        super(p_179146_);
    }

    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        RotationLerpHandler.beginClickTurn(new PacketLookAtLerped(buf).getTargetPosition(null));
    }
}
