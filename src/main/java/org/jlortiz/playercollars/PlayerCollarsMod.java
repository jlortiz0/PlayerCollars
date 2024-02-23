package org.jlortiz.playercollars;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.SlotTypeMessage;

@Mod(PlayerCollarsMod.MOD_ID)
public class PlayerCollarsMod {
	public static final String MOD_ID = "playercollars";
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	public static final RegistryObject<PlayerCollarItem> COLLAR_ITEM = ITEMS.register("collar", PlayerCollarItem::new);

	public PlayerCollarsMod() {
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("necklace").cosmetic().build());
	}

	@SubscribeEvent
	public void registerItemColors(RegisterColorHandlersEvent.Item event) {
		event.register(((itemStack, i) -> i == 0 ? COLLAR_ITEM.get().getColor(itemStack) : -1), COLLAR_ITEM.get());
	}
}
