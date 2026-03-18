package org.jlortiz.playercollars.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.util.NbtUtil;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;
import java.util.function.Supplier;

public record PacketOpenPawsConfig(UUID pawHolder, boolean heldItems) {

    public static PacketOpenPawsConfig decode(FriendlyByteBuf buf) {
        return new PacketOpenPawsConfig(buf.readUUID(), buf.readBoolean());
    }

    public static void encode(PacketOpenPawsConfig packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.pawHolder);
        buf.writeBoolean(packet.heldItems);
    }

    public static void handle(PacketOpenPawsConfig packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            Player pet = player.serverLevel().getPlayerByUUID(packet.pawHolder);
            if (pet == null) return;

            var curiosOpt = CuriosApi.getCuriosInventory(pet);
            if (!curiosOpt.isPresent()) return;

            var collarResults = curiosOpt.resolve().orElseThrow().findCurios(s -> s.is(PlayerCollarsMod.COLLAR_TAG));
            ItemStack collarStack = PlayerCollarsMod.filterStacksByOwner(collarResults, player.getUUID(), packet.pawHolder);
            if (collarStack == null) {
                player.displayClientMessage(Component.translatable("item.playercollars.paw_configurator.no_set_non_owner")
                        .withStyle(net.minecraft.ChatFormatting.RED), true);
                return;
            }

            var pawsResults = curiosOpt.resolve().orElseThrow().findCurios(s -> s.is(PlayerCollarsMod.PAWS_TAG));
            if (pawsResults.isEmpty()) {
                player.displayClientMessage(Component.translatable("item.playercollars.paw_configurator.no_paws")
                        .withStyle(net.minecraft.ChatFormatting.RED), true);
                return;
            }

            ItemStack[] pawsStacks = pawsResults.stream().map(sr -> sr.stack()).toArray(ItemStack[]::new);

            NetworkHooks.openScreen(player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable(packet.heldItems
                            ? "gui.playercollars.paw_configurator.item.title"
                            : "gui.playercollars.paw_configurator.block.title", pet.getName());
                }

                @Override
                public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                    var sc = packet.heldItems
                            ? new PawsConfigScreenHandler.PawsItemConfigScreenHandler(syncId, inv, NbtUtil.getHeldItems(pawsStacks[0]))
                            : new PawsConfigScreenHandler.PawsBlockConfigScreenHandler(syncId, inv, NbtUtil.getCanInteract(pawsStacks[0]));
                    sc.setPawsStack(pawsStacks);
                    return sc;
                }
            }, extraBuf -> {
                if (packet.heldItems)
                    NbtUtil.writeHeldItems(extraBuf, NbtUtil.getHeldItems(pawsStacks[0]));
                else
                    NbtUtil.writeCanInteract(extraBuf, NbtUtil.getCanInteract(pawsStacks[0]));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
