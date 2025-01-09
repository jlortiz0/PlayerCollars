package org.jlortiz.playercollars;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.MapColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.item.CollarItem;

public record PacketUpdateCollar(OwnerState os, int pawColor, int color) implements CustomPayload {
    public static final CustomPayload.Id<PacketUpdateCollar> ID = new CustomPayload.Id<>(Identifier.of(PlayerCollarsMod.MOD_ID, "update_collar"));
    public static final PacketCodec<RegistryByteBuf, PacketUpdateCollar> CODEC = PacketCodec.tuple(
            PacketCodecs.indexed(OwnerState::fromInt, OwnerState::ordinal), PacketUpdateCollar::os,
            PacketCodecs.INTEGER, PacketUpdateCollar::pawColor,
            PacketCodecs.INTEGER, PacketUpdateCollar::color,
            PacketUpdateCollar::new);
    public PacketUpdateCollar(ItemStack is, OwnerState os) {
        this(os, PlayerCollarsMod.COLLAR_ITEM.getPawColor(is), PlayerCollarsMod.COLLAR_ITEM.getColor(is));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public enum OwnerState {
        NOP, DEL, ADD;

        public static OwnerState fromInt(int ind) {
            return OwnerState.values()[ind];
        }
    }

    public void handle(ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ItemStack is = context.player().getMainHandStack();
            if (!is.isEmpty() && is.getItem() instanceof CollarItem) {
                is.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, true));
                is.set(DataComponentTypes.MAP_COLOR, new MapColorComponent(pawColor));
                if (os == OwnerState.DEL) {
                    is.remove(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
                } else if (os == OwnerState.ADD) {
                    is.set(PlayerCollarsMod.OWNER_COMPONENT_TYPE, new OwnerComponent(context.player().getUuid(), context.player().getName().getString()));
                }
            }
        });
    }
}
