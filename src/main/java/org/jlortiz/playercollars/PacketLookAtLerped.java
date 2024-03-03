package org.jlortiz.playercollars;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import org.jlortiz.playercollars.client.RotationLerpHandler;

import java.util.function.Supplier;

public class PacketLookAtLerped extends ClientboundPlayerLookAtPacket {
    public PacketLookAtLerped(Entity p_132783_) {
        super(EntityAnchorArgument.Anchor.EYES, p_132783_.getX(), p_132783_.getEyeY(), p_132783_.getZ());
    }

    public PacketLookAtLerped(FriendlyByteBuf p_179146_) {
        super(p_179146_);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            RotationLerpHandler.beginClickTurn(this.getPosition(null));
        });
        context.get().setPacketHandled(true);
    }
}
