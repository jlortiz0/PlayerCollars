package org.jlortiz.playercollars.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.jlortiz.playercollars.client.RotationLerpHandler;

import java.util.function.Supplier;

public record PacketLookAtLerped(double x, double y, double z) {

    public PacketLookAtLerped(Entity entity) {
        this(entity.getX(), entity.getEyeY(), entity.getZ());
    }

    public static PacketLookAtLerped decode(FriendlyByteBuf buf) {
        return new PacketLookAtLerped(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void encode(PacketLookAtLerped packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.x).writeDouble(packet.y).writeDouble(packet.z);
    }

    public static void handle(PacketLookAtLerped packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> RotationLerpHandler.beginClickTurn(packet.vec()));
        ctx.get().setPacketHandled(true);
    }

    public Vec3 vec() {
        return new Vec3(this.x, this.y, this.z);
    }
}
