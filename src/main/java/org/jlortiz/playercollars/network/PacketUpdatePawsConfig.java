package org.jlortiz.playercollars.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.PlayerCollarsMod;

public record PacketUpdatePawsConfig(int syncId, ListId listId, NbtElement listContents) implements CustomPayload {
    public static final Id<PacketUpdatePawsConfig> ID = new Id<>(Identifier.of(PlayerCollarsMod.MOD_ID, "update_paws_config_display"));
    public static final PacketCodec<RegistryByteBuf, PacketUpdatePawsConfig> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, PacketUpdatePawsConfig::syncId,
            PacketCodecs.indexed(i -> ListId.values()[i], ListId::ordinal), PacketUpdatePawsConfig::listId,
            PacketCodecs.NBT_ELEMENT, PacketUpdatePawsConfig::listContents,
            PacketUpdatePawsConfig::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handle(@NotNull ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            PawsConfigScreenHandler<?> pawsConfigHandler = getPawsConfigHandler(context);
            if (pawsConfigHandler == null) return;

            pawsConfigHandler.syncListFromServer(listId, listContents);
        });
    }

    private @Nullable PawsConfigScreenHandler<?> getPawsConfigHandler(@NotNull ClientPlayNetworking.Context context) {
        ScreenHandler handler = context.player().currentScreenHandler;
        if (handler.syncId == syncId && handler instanceof PawsConfigScreenHandler<?> pawsHandler) {
            return pawsHandler;
        }
        return null;
    }

    public enum ListId {
        BACKING,
        DISPLAY,
    }
}
