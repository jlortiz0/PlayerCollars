package org.jlortiz.playercollars.client;

import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
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
import org.jlortiz.playercollars.item.FootPawsItem;
import org.jlortiz.playercollars.item.PawsItem;
import org.jlortiz.playercollars.network.PacketLookAtLerped;
import org.jlortiz.playercollars.util.NbtUtil;

@Environment(EnvType.CLIENT)
public class RegisterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> switch (tintIndex) {
            case 0 -> NbtUtil.getColor(stack) | 0xff000000;
            case 1 -> NbtUtil.getPawColor(stack) | 0xff000000;
            default -> -1;
        }, PlayerCollarsMod.COLLAR_ITEM);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            // noinspection CodeBlock2Expr
            return tintIndex == 0 ? NbtUtil.getColor(stack) | 0xff000000 : -1;
        }, PlayerCollarsMod.TAGLESS_COLLAR_ITEM);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            // noinspection CodeBlock2Expr
            return tintIndex == 0 ? NbtUtil.getColor(stack) | 0xff000000 : -1;
        }, PlayerCollarsMod.CLICKER_ITEM);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            BedItem item = (BedItem) stack.getItem();
            BedBlock block = (BedBlock) item.getBlock();
            return tintIndex == 0 ? block.getColor().getFireworkColor() | 0xff000000 : -1;
        }, PlayerCollarsMod.DOG_BED_ITEMS);

        ItemConvertible[] paws = new ItemConvertible[PlayerCollarsMod.PAWS_ITEMS.length + PlayerCollarsMod.FOOT_PAWS_ITEMS.length];
        System.arraycopy(PlayerCollarsMod.PAWS_ITEMS, 0, paws, 0, PlayerCollarsMod.PAWS_ITEMS.length);
        System.arraycopy(PlayerCollarsMod.FOOT_PAWS_ITEMS, 0, paws, PlayerCollarsMod.PAWS_ITEMS.length, PlayerCollarsMod.FOOT_PAWS_ITEMS.length);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> switch (tintIndex) {
            case 0 -> NbtUtil.getColor(stack);
            case 1 -> NbtUtil.getBeanColor(stack);
            default -> -1;
        }, paws);

        ModelPredicateProviderRegistry.register(PlayerCollarsMod.CLICKER_ITEM, new Identifier("cast"), (itemStack, clientWorld, livingEntity, seed) -> {
            // noinspection CodeBlock2Expr
            return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1 : 0;
        });

        CollarRenderer cr = new CollarRenderer();
        TrinketRendererRegistry.registerRenderer(PlayerCollarsMod.COLLAR_ITEM, cr);
        TrinketRendererRegistry.registerRenderer(PlayerCollarsMod.TAGLESS_COLLAR_ITEM, cr);
        PawRenderer pr = new PawRenderer();
        for (PawsItem x : PlayerCollarsMod.PAWS_ITEMS)
            TrinketRendererRegistry.registerRenderer(x, pr);
        FootPawRenderer fpr = new FootPawRenderer();
        for (FootPawsItem x : PlayerCollarsMod.FOOT_PAWS_ITEMS)
            TrinketRendererRegistry.registerRenderer(x, fpr);
        ClientPlayNetworking.registerGlobalReceiver(PacketLookAtLerped.TYPE, PacketLookAtLerped::handle);
        WorldRenderEvents.END.register(RotationLerpHandler::turnTowardsClick);
        HandledScreens.register(PlayerCollarsMod.PAWS_BLOCK_CONFIG_SCREEN_HANDLER, PawsConfigScreen<Block>::new);
        HandledScreens.register(PlayerCollarsMod.PAWS_ITEM_CONFIG_SCREEN_HANDLER, PawsConfigScreen<Item>::new);
    }
}
