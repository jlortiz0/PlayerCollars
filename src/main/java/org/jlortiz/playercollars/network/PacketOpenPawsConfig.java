package org.jlortiz.playercollars.network;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record PacketOpenPawsConfig(UUID pawHolder, boolean heldItems) implements FabricPacket {
    public static final Identifier ID = Identifier.of(PlayerCollarsMod.MOD_ID, "paws_config");
    public static final PacketType<PacketOpenPawsConfig> TYPE = PacketType.create(ID, PacketOpenPawsConfig::new);

    public PacketOpenPawsConfig(PacketByteBuf buf) {
        this(buf.readUuid(), buf.readBoolean());
    }

    public static void handle(PacketOpenPawsConfig packet, ServerPlayerEntity player, PacketSender responseSender) {
        PlayerEntity pet = player.getWorld().getPlayerByUuid(packet.pawHolder);
        if (pet == null) return;
        Optional<TrinketComponent> optComponent = TrinketsApi.getTrinketComponent(pet);
        if (optComponent.isEmpty()) return;
        TrinketComponent component = optComponent.get();

        ItemStack collarStack = PlayerCollarsMod.filterStacksByOwner(component.getEquipped((y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG)), player.getUuid(), packet.pawHolder);
        if (collarStack == null) {
            player.sendMessage(Text.translatable("item.playercollars.paw_configurator.no_set_non_owner").formatted(Formatting.RED), true);
            return;
        }

        List<Pair<SlotReference, ItemStack>> pawsStack = component.getEquipped((y) -> y.isIn(PlayerCollarsMod.PAWS_TAG));
        if (pawsStack.isEmpty()) {
            player.sendMessage(Text.translatable("item.playercollars.paw_configurator.no_paws").formatted(Formatting.RED), true);
            return;
        }

        player.openHandledScreen(new ExtendedScreenHandlerFactory() {
            @Override
            public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                ItemStack[] ps = new ItemStack[pawsStack.size()];
                for (int i = 0; i < pawsStack.size(); i++)
                    ps[i] = pawsStack.get(i).getRight();

                var sc = packet.heldItems ?
                        new PawsConfigScreenHandler.PawsItemConfigScreenHandler(syncId, playerInventory, NbtUtil.getHeldItems(ps[0])) :
                        new PawsConfigScreenHandler.PawsBlockConfigScreenHandler(syncId, playerInventory, NbtUtil.getCanInteract(ps[0]));
                sc.setPawsStack(ps);
                return sc;
            }

            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                ItemStack stack = pawsStack.get(0).getRight();

                if (packet.heldItems)
                    NbtUtil.writeHeldItems(buf, NbtUtil.getHeldItems(stack));
                else
                    NbtUtil.writeCanInteract(buf, NbtUtil.getCanInteract(stack));
            }

            @Override
            public boolean shouldCloseCurrentScreen() {
                return false;
            }

            @Override
            public Text getDisplayName() {
                return Text.translatable(packet.heldItems ? "gui.playercollars.paw_configurator.item.title" :
                        "gui.playercollars.paw_configurator.block.title", pet.getName());
            }
        });
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.pawHolder);
        buf.writeBoolean(this.heldItems);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
