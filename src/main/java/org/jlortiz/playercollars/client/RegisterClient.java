package org.jlortiz.playercollars.client;

import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.screen.PawsConfigScreen;
import org.jlortiz.playercollars.item.ClickerItem;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.item.FootPawsItem;
import org.jlortiz.playercollars.item.PawsItem;
import org.jlortiz.playercollars.network.PacketLookAtLerped;

@Environment(EnvType.CLIENT)
public class RegisterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> switch (tintIndex) {
            case 0 -> CollarItem.getColor(stack) | 0xff000000;
            case 1 -> CollarItem.getPawColor(stack) | 0xff000000;
            default -> -1;
        }, PlayerCollarsMod.COLLAR_ITEM);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 ? CollarItem.getColor(stack) | 0xff000000 : -1, PlayerCollarsMod.TAGLESS_COLLAR_ITEM);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 ? ClickerItem.getColor(stack) | 0xff000000 : -1, PlayerCollarsMod.CLICKER_ITEM);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 ? ((BedBlock) ((BedItem) stack.getItem()).getBlock()).getColor().getFireworkColor() | 0xff000000 : -1, PlayerCollarsMod.DOG_BED_ITEMS);

        ItemConvertible[] paws = new ItemConvertible[PlayerCollarsMod.PAWS_ITEMS.length + PlayerCollarsMod.FOOT_PAWS_ITEMS.length];
        System.arraycopy(PlayerCollarsMod.PAWS_ITEMS, 0, paws, 0, PlayerCollarsMod.PAWS_ITEMS.length);
        System.arraycopy(PlayerCollarsMod.FOOT_PAWS_ITEMS, 0, paws, PlayerCollarsMod.PAWS_ITEMS.length, PlayerCollarsMod.FOOT_PAWS_ITEMS.length);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> switch (tintIndex) {
            case 0 -> PawsItem.getColor(stack);
            case 1 -> PawsItem.getBeanColor(stack);
            default -> -1;
        }, paws);
        ModelPredicateProviderRegistry.register(PlayerCollarsMod.CLICKER_ITEM, Identifier.ofVanilla("cast"), (itemStack, clientWorld, livingEntity, seed) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1 : 0);

        CollarRenderer cr = new CollarRenderer();
        TrinketRendererRegistry.registerRenderer(PlayerCollarsMod.COLLAR_ITEM, cr);
        TrinketRendererRegistry.registerRenderer(PlayerCollarsMod.TAGLESS_COLLAR_ITEM, cr);
        PawRenderer pr = new PawRenderer();
        for (PawsItem x : PlayerCollarsMod.PAWS_ITEMS)
            TrinketRendererRegistry.registerRenderer(x, pr);
        FootPawRenderer fpr = new FootPawRenderer();
        for (FootPawsItem x : PlayerCollarsMod.FOOT_PAWS_ITEMS)
            TrinketRendererRegistry.registerRenderer(x, fpr);
        ClientPlayNetworking.registerGlobalReceiver(PacketLookAtLerped.ID, (payload, context) -> context.client().execute(() -> RotationLerpHandler.beginClickTurn(payload.vec())));
        WorldRenderEvents.END.register(RotationLerpHandler::turnTowardsClick);
        HandledScreens.register(PlayerCollarsMod.PAWS_BLOCK_CONFIG_SCREEN_HANDLER, PawsConfigScreen<Block>::new);
        HandledScreens.register(PlayerCollarsMod.PAWS_ITEM_CONFIG_SCREEN_HANDLER, PawsConfigScreen<Item>::new);
    }
}
