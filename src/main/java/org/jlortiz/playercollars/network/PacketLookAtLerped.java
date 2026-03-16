package org.jlortiz.playercollars.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.RotationLerpHandler;

public record PacketLookAtLerped(double x, double y, double z) implements FabricPacket {
    public static final Identifier ID = Identifier.of(PlayerCollarsMod.MOD_ID, "look_at");
    public static final PacketType<PacketLookAtLerped> TYPE = PacketType.create(ID, PacketLookAtLerped::new);

    public PacketLookAtLerped(Entity entity) {
        this(entity.getX(), entity.getEyeY(), entity.getZ());
    }

    public PacketLookAtLerped(PacketByteBuf buf) {
        this(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(PacketLookAtLerped packet, ClientPlayerEntity player, PacketSender responseSender) {
        RotationLerpHandler.beginClickTurn(packet.vec());
    }

    public Vec3d vec() {
        return new Vec3d(this.x, this.y, this.z);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeDouble(this.x)
                .writeDouble(this.y)
                .writeDouble(this.z);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
