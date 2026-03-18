package org.jlortiz.playercollars.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class ModPackets {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PlayerCollarsMod.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, PacketUpdateCollar.class,
                PacketUpdateCollar::encode, PacketUpdateCollar::decode,
                PacketUpdateCollar::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, PacketStampDeed.class,
                PacketStampDeed::encode, PacketStampDeed::decode,
                PacketStampDeed::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, PacketOpenPawsConfig.class,
                PacketOpenPawsConfig::encode, PacketOpenPawsConfig::decode,
                PacketOpenPawsConfig::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, PacketLookAtLerped.class,
                PacketLookAtLerped::encode, PacketLookAtLerped::decode,
                PacketLookAtLerped::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
