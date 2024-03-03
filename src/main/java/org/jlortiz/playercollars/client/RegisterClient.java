package org.jlortiz.playercollars.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.ClickerItem;
import org.jlortiz.playercollars.item.CollarItem;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class RegisterClient {
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
        MinecraftForge.EVENT_BUS.register(new RotationLerpHandler());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        final CollarItem item = PlayerCollarsMod.COLLAR_ITEM.get();
        event.register(((itemStack, i) -> switch (i) {
            case 0 -> item.getColor(itemStack);
            case 1 -> item.getTagColor(itemStack);
            case 2 -> item.getPawColor(itemStack);
            default -> -1;
        }
        ), item);

        final ClickerItem item2 = PlayerCollarsMod.CLICKER_ITEM.get();
        event.register((itemStack, i) -> i == 0 ? item2.getColor(itemStack) : -1, item2);
    }
}
