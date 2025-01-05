package org.jlortiz.playercollars;

import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record PacketLookAtLerped(double x, double y, double z) implements CustomPayload {
    public static final CustomPayload.Id<PacketLookAtLerped> ID = new CustomPayload.Id<>(Identifier.of(PlayerCollarsMod.MOD_ID, "look_at"));
    public static final PacketCodec<RegistryByteBuf, PacketLookAtLerped> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, PacketLookAtLerped::x,
            PacketCodecs.DOUBLE, PacketLookAtLerped::y,
            PacketCodecs.DOUBLE, PacketLookAtLerped::z,
            PacketLookAtLerped::new);

    public PacketLookAtLerped(Entity p_132783_) {
        this(p_132783_.getX(), p_132783_.getEyeY(), p_132783_.getZ());
    }

    public Vec3d vec() {
        return new Vec3d(x, y, z);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
