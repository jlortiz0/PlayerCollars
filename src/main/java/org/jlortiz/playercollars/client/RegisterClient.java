package org.jlortiz.playercollars.client;

import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.jlortiz.playercollars.PacketLookAtLerped;
import org.jlortiz.playercollars.PlayerCollarsMod;

@Environment(EnvType.CLIENT)
public class RegisterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TrinketRendererRegistry.registerRenderer(PlayerCollarsMod.COLLAR_ITEM, new CollarRenderer());
        ClientPlayNetworking.registerGlobalReceiver(PacketLookAtLerped.ID, (payload, context) -> context.client().execute(() -> RotationLerpHandler.beginClickTurn(payload.vec())));
        WorldRenderEvents.END.register(RotationLerpHandler::turnTowardsClick);
    }
}
