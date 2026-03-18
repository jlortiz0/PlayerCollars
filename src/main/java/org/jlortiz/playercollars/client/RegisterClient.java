package org.jlortiz.playercollars.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.level.block.BedBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.screen.PawsConfigScreen;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.item.FootPawsItem;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod.EventBusSubscriber(modid = PlayerCollarsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegisterClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Register Curios renderers
        CollarRenderer cr = new CollarRenderer();
        CuriosRendererRegistry.register(PlayerCollarsMod.COLLAR_ITEM.get(), () -> cr);
        CuriosRendererRegistry.register(PlayerCollarsMod.TAGLESS_COLLAR_ITEM.get(), () -> cr);

        PawRenderer pr = new PawRenderer();
        for (var pawItem : PlayerCollarsMod.PAWS_ITEMS)
            CuriosRendererRegistry.register(pawItem.get(), () -> pr);

        FootPawRenderer fpr = new FootPawRenderer();
        for (var footItem : PlayerCollarsMod.FOOT_PAWS_ITEMS)
            CuriosRendererRegistry.register(footItem.get(), () -> fpr);

        // Register menu screens
        event.enqueueWork(() -> {
            MenuScreens.register(PlayerCollarsMod.PAWS_BLOCK_CONFIG_MENU.get(), PawsConfigScreen.BlockScreen::new);
            MenuScreens.register(PlayerCollarsMod.PAWS_ITEM_CONFIG_MENU.get(), PawsConfigScreen.ItemScreen::new);
        });

        // Item model predicates
        event.enqueueWork(() -> {
            ItemProperties.register(PlayerCollarsMod.CLICKER_ITEM.get(),
                    new ResourceLocation("cast"),
                    (stack, world, entity, seed) ->
                            entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1f : 0f);
        });
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        // Collar (2 tint layers: body color + tag color)
        event.register((stack, tintIndex) -> switch (tintIndex) {
            case 0 -> ((net.minecraft.world.item.DyeableLeatherItem) stack.getItem()).getColor(stack) | 0xFF000000;
            case 1 -> ((CollarItem) stack.getItem()).getTagColor(stack) | 0xFF000000;
            default -> -1;
        }, PlayerCollarsMod.COLLAR_ITEM.get());

        // Tagless collar
        event.register((stack, tintIndex) ->
                tintIndex == 0 ? ((net.minecraft.world.item.DyeableLeatherItem) stack.getItem()).getColor(stack) | 0xFF000000 : -1,
                PlayerCollarsMod.TAGLESS_COLLAR_ITEM.get());

        // Clicker
        event.register((stack, tintIndex) ->
                tintIndex == 0 ? ((net.minecraft.world.item.DyeableLeatherItem) stack.getItem()).getColor(stack) | 0xFF000000 : -1,
                PlayerCollarsMod.CLICKER_ITEM.get());

        // Dog beds
        for (var bedItemRO : PlayerCollarsMod.DOG_BED_ITEMS) {
            event.register((stack, tintIndex) -> {
                BedItem item = (BedItem) stack.getItem();
                BedBlock block = (BedBlock) item.getBlock();
                return tintIndex == 0 ? block.getColor().getFireworkColor() | 0xFF000000 : -1;
            }, bedItemRO.get());
        }

        // Paws + foot paws
        for (var p : PlayerCollarsMod.PAWS_ITEMS) {
            event.register((stack, tintIndex) -> switch (tintIndex) {
                case 0 -> ((FootPawsItem) stack.getItem()).getColor();
                case 1 -> ((FootPawsItem) stack.getItem()).getPawColor();
                default -> -1;
            }, p.get());
        }
        for (var p : PlayerCollarsMod.FOOT_PAWS_ITEMS) {
            event.register((stack, tintIndex) -> switch (tintIndex) {
                case 0 -> ((FootPawsItem) stack.getItem()).getColor();
                case 1 -> ((FootPawsItem) stack.getItem()).getPawColor();
                default -> -1;
            }, p.get());
        }
    }
}
