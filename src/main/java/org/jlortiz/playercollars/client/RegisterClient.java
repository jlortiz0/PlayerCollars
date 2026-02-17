package org.jlortiz.playercollars.client;

import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.screen.PawsConfigScreen;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.network.PacketLookAtLerped;

@Environment(EnvType.CLIENT)
public class RegisterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AccessoriesRendererRegistry.registerRenderer(CollarItem.REGISTRY_KEY.getValue(), CollarRenderer::new);
        Identifier pawsRenderer = Identifier.of(PlayerCollarsMod.MOD_ID, "paws");
        AccessoriesRendererRegistry.registerRenderer(pawsRenderer, PawRenderer::new);
        Identifier footPawsRenderer = Identifier.of(PlayerCollarsMod.MOD_ID, "foot_paws");
        AccessoriesRendererRegistry.registerRenderer(footPawsRenderer, FootPawRenderer::new);

        AccessoriesRendererRegistry.bindItemToRenderer(PlayerCollarsMod.COLLAR_ITEM, CollarItem.REGISTRY_KEY.getValue());
        AccessoriesRendererRegistry.bindItemToRenderer(PlayerCollarsMod.TAGLESS_COLLAR_ITEM, CollarItem.REGISTRY_KEY.getValue());
        for (Item i : PlayerCollarsMod.PAWS_ITEMS)
            AccessoriesRendererRegistry.bindItemToRenderer(i, pawsRenderer);
        for (Item i : PlayerCollarsMod.FOOT_PAWS_ITEMS)
            AccessoriesRendererRegistry.bindItemToRenderer(i, footPawsRenderer);
        ClientPlayNetworking.registerGlobalReceiver(PacketLookAtLerped.ID, (payload, context) ->
                context.client().execute(() -> RotationLerpHandler.beginClickTurn(payload.vec())));
        WorldRenderEvents.END_MAIN.register(RotationLerpHandler::turnTowardsClick);
        HandledScreens.register(PlayerCollarsMod.PAWS_BLOCK_CONFIG_SCREEN_HANDLER, PawsConfigScreen<Block>::new);
        HandledScreens.register(PlayerCollarsMod.PAWS_ITEM_CONFIG_SCREEN_HANDLER, PawsConfigScreen<Item>::new);
    }
}
