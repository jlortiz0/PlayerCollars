package org.jlortiz.playercollars.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class RegisterLayer {
    @SubscribeEvent
    public static void onModelBakeEvent(ModelEvent.BakingCompleted event) {
        final ModelResourceLocation loc = new ModelResourceLocation(new ResourceLocation(PlayerCollarsMod.MOD_ID, "collar"), "inventory");
        final CollarRenderer cr = new CollarRenderer(event.getModels().get(loc));
        CuriosRendererRegistry.register(PlayerCollarsMod.COLLAR_ITEM.get(), () -> cr);
    }

    @SubscribeEvent
    public static void propertyOverrideRegistry(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(PlayerCollarsMod.CLICKER_ITEM.get(), new ResourceLocation("cast"),
                (p_174650_, p_174651_, p_174652_, p_174653_) -> p_174652_ != null && p_174652_.getUseItem() == p_174650_ ? p_174652_.getTicksUsingItem() : 0));
    }
}
