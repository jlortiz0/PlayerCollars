package org.jlortiz.playercollars;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
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
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
			DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);
	public static final RegistryObject<RecipeSerializer<CollarRecipe>> COLLAR_SERIALIZER =
			RECIPE_SERIALIZERS.register(CollarRecipe.Type.ID,() -> CollarRecipe.Serializer);
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MOD_ID);
	public static final RegistryObject<RecipeType<CollarRecipe>> COLLAR_TYPE =
			RECIPE_TYPES.register(CollarRecipe.Type.ID, () -> CollarRecipe.Type.INSTANCE);

	public PlayerCollarsMod() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(eventBus);
		RECIPE_SERIALIZERS.register(eventBus);
		RECIPE_TYPES.register(eventBus);
		eventBus.register(this);
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("necklace").cosmetic().build());
	}

	@SubscribeEvent
	public void registerItemColors(RegisterColorHandlersEvent.Item event) {
		event.register(((itemStack, i) -> switch (i) {
                case 0 -> COLLAR_ITEM.get().getColor(itemStack);
                case 1 -> COLLAR_ITEM.get().getTagColor(itemStack);
                case 2 -> COLLAR_ITEM.get().getPawColor(itemStack);
                default -> -1;
            }
		), COLLAR_ITEM.get());
	}
}
