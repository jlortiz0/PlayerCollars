package org.jlortiz.playercollars.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PacketLookAtLerped;
import org.jlortiz.playercollars.PlayerCollarsMod;

@Environment(EnvType.CLIENT)
public class RegisterClient implements ClientModInitializer {
//    @SubscribeEvent
//    public static void onModelBakeEvent(ModelEvent.BakingCompleted event) {
//        final ModelResourceLocation loc = new ModelResourceLocation(new ResourceLocation(PlayerCollarsMod.MOD_ID, "collar"), "inventory");
//        final CollarRenderer cr = new CollarRenderer(event.getModels().get(loc));
//        CuriosRendererRegistry.register(PlayerCollarsMod.COLLAR_ITEM.get(), () -> cr);
//    }
//
//    @SubscribeEvent
//    public static void propertyOverrideRegistry(FMLClientSetupEvent event) {
//        event.enqueueWork(() -> ItemProperties.register(PlayerCollarsMod.CLICKER_ITEM.get(), new ResourceLocation("cast"),
//                (p_174650_, p_174651_, p_174652_, p_174653_) -> p_174652_ != null && p_174652_.getUseItem() == p_174650_ ? p_174652_.getTicksUsingItem() : 0));
//    }

    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> switch (tintIndex) {
            case 0 -> PlayerCollarsMod.COLLAR_ITEM.getColor(stack);
            case 1 -> PlayerCollarsMod.COLLAR_ITEM.getTagColor(stack);
            case 2 -> PlayerCollarsMod.COLLAR_ITEM.getPawColor(stack);
            default -> -1;
        }, PlayerCollarsMod.COLLAR_ITEM);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 ? PlayerCollarsMod.CLICKER_ITEM.getColor(stack) : -1, PlayerCollarsMod.CLICKER_ITEM);
        ModelPredicateProviderRegistry.register(PlayerCollarsMod.CLICKER_ITEM, new Identifier("cast"), (itemStack, clientWorld, livingEntity, seed) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1 : 0);

        ModelLoadingPlugin.register(new CollarModelLoadingPlugin());
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(PlayerCollarsMod.MOD_ID, "look_at"), PacketLookAtLerped::handle);
        ClientTickEvents.END_CLIENT_TICK.register(RotationLerpHandler::turnTowardsClick);
    }
}
