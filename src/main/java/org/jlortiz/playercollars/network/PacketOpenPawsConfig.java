package org.jlortiz.playercollars.network;

import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record PacketOpenPawsConfig(UUID pawHolder, PawsScreenType screenType) implements CustomPayload {
    public static final CustomPayload.Id<PacketOpenPawsConfig> ID = new CustomPayload.Id<>(Identifier.of(PlayerCollarsMod.MOD_ID, "paws_config"));
    public static final PacketCodec<RegistryByteBuf, PacketOpenPawsConfig> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, PacketOpenPawsConfig::pawHolder,
            PacketCodecs.indexed(i -> PawsScreenType.values()[i], PawsScreenType::ordinal), PacketOpenPawsConfig::screenType,
            PacketOpenPawsConfig::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handle(ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            PlayerEntity pet = context.player().getWorld().getPlayerByUuid(pawHolder);
            if (pet == null) return;
            AccessoriesCapability cap = AccessoriesCapability.get(pet);
            if (cap == null) return;

            if (!PlayerCollarsMod.getOwnershipLevel(pet, context.player()).isOwned()) {
                context.player().sendMessage(Text.translatable("item.playercollars.paw_configurator.no_set_non_owner").formatted(Formatting.RED), true);
                return;
            }

            List<SlotEntryReference> pawsStack = cap.getEquipped((y) -> y.isIn(PlayerCollarsMod.PAWS_TAG));
            if (pawsStack.isEmpty()) {
                context.player().sendMessage(Text.translatable("item.playercollars.paw_configurator.no_paws").formatted(Formatting.RED), true);
                return;
            }

            context.player().openHandledScreen(new ExtendedScreenHandlerFactory<>() {
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                    ItemStack[] ps = new ItemStack[pawsStack.size()];
                    for (int i = 0; i < pawsStack.size(); i++)
                        ps[i] = pawsStack.get(i).stack();

                    PawsConfigScreenHandler<?> sc = switch (screenType) {
                        case ITEM_HOLD ->
                                new PawsConfigScreenHandler.PawsItemConfigScreenHandler(syncId, playerInventory,
                                        ps[0].get(PlayerCollarsMod.HELD_ITEMS_COMPONENT_TYPE));
                        case BLOCK_BREAK ->
                                new PawsConfigScreenHandler.PawsBlockBreakConfigScreenHandler(syncId, playerInventory,
                                        ps[0].get(PlayerCollarsMod.CAN_BREAK_COMPONENT_TYPE));
                        case BLOCK_INTERACT ->
                                new PawsConfigScreenHandler.PawsBlockConfigScreenHandler(syncId, playerInventory,
                                        ps[0].get(PlayerCollarsMod.CAN_INTERACT_COMPONENT_TYPE));
                    };
                    sc.setPawsStack(ps);
                    return sc;
                }

                @Override
                public Text getDisplayName() {
                    return Text.translatable(switch (screenType) {
                        case ITEM_HOLD -> "gui.playercollars.paw_configurator.item.title";
                        case BLOCK_BREAK -> "gui.playercollars.paw_configurator.block_break.title";
                        case BLOCK_INTERACT -> "gui.playercollars.paw_configurator.block.title";
                    }, pet.getName());
                }

                @Override
                public Object getScreenOpeningData(ServerPlayerEntity player) {
                    return Optional.ofNullable(pawsStack.get(0).stack().get(switch (screenType) {
                        case ITEM_HOLD -> PlayerCollarsMod.HELD_ITEMS_COMPONENT_TYPE;
                        case BLOCK_INTERACT -> PlayerCollarsMod.CAN_INTERACT_COMPONENT_TYPE;
                        case BLOCK_BREAK -> PlayerCollarsMod.CAN_BREAK_COMPONENT_TYPE;
                    })).orElse(List.of());
                }
            });
        });
    }

    public enum PawsScreenType {
        BLOCK_BREAK,
        BLOCK_INTERACT,
        ITEM_HOLD,
    }
}
