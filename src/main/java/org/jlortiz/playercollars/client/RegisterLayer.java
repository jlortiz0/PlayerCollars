package org.jlortiz.playercollars.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
}
